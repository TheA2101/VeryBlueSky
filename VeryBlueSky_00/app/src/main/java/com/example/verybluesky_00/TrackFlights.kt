package com.example.verybluesky_00

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TrackFlights : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_flights)

        val userEmail = intent.getStringExtra("UserInputEmail")
        val currentDate = getCurrentDateFormatted()
        val trackLayout = findViewById<LinearLayout>(R.id.trackLayout)

        val trackRecordsCollection = db.collection("Track Flights")

        trackRecordsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val departureDate = document.getString("departure date")
                    val email = document.getString("email")

                    if (email == userEmail && departureDate != null) {
                        if (departureDate >= currentDate) { // Compare strings directly
                            val flightInfo = createFlightInfoText(document)
                            val button = createFlightButton(this, flightInfo)
                            trackLayout.addView(button)
                        } else {
                            val flightData = document.data ?: hashMapOf()
                            moveFlightToPreviousFlights(flightData)
                            document.reference.delete()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error retrieving flight data: $e")
            }
    }

    private fun getCurrentDateFormatted(): String {
        // Format current date as "MM-DD-YYYY"
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        return dateFormat.format(calendar.time)
    }

    private fun createFlightInfoText(document: DocumentSnapshot): String {
        val departureDate = document.getString("departure date")
        val destination = document.getString("destination")
        val origin = document.getString("origin")
        val price = document.getString("price")
        return "Departure Date: $departureDate\n" +
                "Destination: $destination\n" +
                "Origin: $origin\n" +
                "Price: \$$price"
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

    private fun moveFlightToPreviousFlights(flightData: Map<String, Any>) {
        val previousFlightsCollection = db.collection("Previous Flights")
        previousFlightsCollection.add(flightData)
    }

    private fun showToast(message: String) {
        // Implement your showToast function
    }
}
