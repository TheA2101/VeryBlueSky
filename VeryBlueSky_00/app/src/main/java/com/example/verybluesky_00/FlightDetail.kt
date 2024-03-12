package com.example.verybluesky_00

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verybluesky_00.databinding.ActivityFlightDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class FlightDetail : AppCompatActivity() {
    private lateinit var binding: ActivityFlightDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlightDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val FlightUser = intent.getStringExtra("UserInputEmail")
        val FlightPass = intent.getStringExtra("UserInputPass")

        val flightInfo = intent.getStringExtra("flightInfo")
        binding.flightInfoTextView.text = flightInfo

        binding.bookButton.setOnClickListener {
            val enteredEmail = binding.emailEditText.text.toString()
            val enteredPassword = binding.passwordEditText.text.toString()

            if (enteredEmail == FlightUser && enteredPassword == FlightPass) {
                showToast("Email and password match!")
                if (flightInfo != null) {
                    saveFlightInfoToDatabase(flightInfo, enteredEmail)
                    finish()
                }
            } else {
                showToast("Email or Password don't match!")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveFlightInfoToDatabase(flightInfo: String, userEmail: String) {
        val flightData = flightInfo.split("\n") // Split flight info into lines
        val departureDate = flightData[0].substringAfter(":").trim()
        val departureTime = flightData[1].substringAfter(":").trim().toLongOrNull()
        val destination = flightData[2].substringAfter(":").trim()
        val origin = flightData[3].substringAfter(":").trim()
        val price = flightData[4].substringAfter(":").trim()

        // Create a new document in "Track Flights" collection with flight info
        val flightDocument = hashMapOf(
            "departure date" to departureDate,
            "departure time" to departureTime,
            "destination" to destination,
            "email" to userEmail,
            "origin" to origin,
            "price" to price
        )

        db.collection("Track Flights")
            .add(flightDocument)
            .addOnSuccessListener { documentReference ->
                showToast("Flight Booked!")
            }
            .addOnFailureListener { e ->
                showToast("Error Booking: $e")
            }
    }
}
