package com.example.verybluesky_00

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PrevFlights : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prev_flights)

        val userEmail = intent.getStringExtra("UserInputEmail")

        val trackLayout = findViewById<LinearLayout>(R.id.trackLayout)

        val prevFlightsCollection = db.collection("Previous Flights")

        // Retrieve flight data for the logged-in user from the "Previous Flights" collection
        prevFlightsCollection.get()
            .addOnSuccessListener { documents ->
                val flightList = mutableListOf<String>()
                for (document in documents) {
                    val departureDate = document.getString("departure date")
                    val destination = document.getString("destination")
                    val origin = document.getString("origin")
                    val price = document.getString("price")
                    val email = document.getString("email")

                    if (email == userEmail) {

                        // Format flight information as a string
                        val flightInfo = "Departure Date: $departureDate\n" +
                                "Destination: $destination\n" +
                                "Origin: $origin\n" +
                                "Price: \$$price"

                        val button = createFlightButton(this, flightInfo)
                        trackLayout.addView(button)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle errors
                showToast("Error retrieving flight data: $e")
            }
    }

    private fun createFlightButton(context: AppCompatActivity, text: String): Button {
        val button = Button(context)
        button.text = text
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 16)
        button.layoutParams = layoutParams
        return button
    }

    private fun showToast(message: String) {
        // Implement your showToast function
    }
}
