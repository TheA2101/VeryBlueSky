package com.example.verybluesky_00


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminHome : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val addFlightButton: Button = findViewById(R.id.add_flight)
        val removeFlightButton: Button = findViewById(R.id.remove_flight) // New button
        val originEditText: EditText = findViewById(R.id.editTextOrigin)
        val destinationEditText: EditText = findViewById(R.id.editTextDestination)
        val departureDateEditText: EditText = findViewById(R.id.editTextDepartureDate)
        val departureTimeEditText: EditText = findViewById(R.id.editTextDepartureTime)
        val priceEditText: EditText = findViewById(R.id.editTextPrice)

        addFlightButton.setOnClickListener {
            val origin = originEditText.text.toString()
            val destination = destinationEditText.text.toString()
            val departureDate = departureDateEditText.text.toString()
            val departureTime = departureTimeEditText.text.toString().toDoubleOrNull()
            val price = priceEditText.text.toString()

            if (origin.isNotEmpty() && destination.isNotEmpty() && departureDate.isNotEmpty() && departureTime != null && price.isNotEmpty()) {
                val flightData = hashMapOf(
                    "origin" to origin,
                    "destination" to destination,
                    "departure date" to departureDate,
                    "departure time" to departureTime,
                    "price" to price
                )

                firestore.collection("Future flights")
                    .add(flightData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Flight added successfully", Toast.LENGTH_SHORT).show()
                        clearEditTextFields()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add flight", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        removeFlightButton.setOnClickListener {
            val origin = originEditText.text.toString()
            val destination = destinationEditText.text.toString()
            val departureDate = departureDateEditText.text.toString()
            val departureTime = departureTimeEditText.text.toString().toDoubleOrNull()
            val price = priceEditText.text.toString()

            if (origin.isNotEmpty() && destination.isNotEmpty() && departureDate.isNotEmpty() && departureTime != null && price.isNotEmpty()) {
                // Check if flight exists and remove it
                val query = firestore.collection("Future flights")
                    .whereEqualTo("origin", origin)
                    .whereEqualTo("destination", destination)
                    .whereEqualTo("departure date", departureDate)
                    .whereEqualTo("departure time", departureTime)
                    .whereEqualTo("price", price)

                query.get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                firestore.collection("Future flights").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Flight removed successfully", Toast.LENGTH_SHORT).show()
                                        clearEditTextFields()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to remove flight", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Flight not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to remove flight", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearEditTextFields() {
        val originEditText: EditText = findViewById(R.id.editTextOrigin)
        val destinationEditText: EditText = findViewById(R.id.editTextDestination)
        val departureDateEditText: EditText = findViewById(R.id.editTextDepartureDate)
        val departureTimeEditText: EditText = findViewById(R.id.editTextDepartureTime)
        val priceEditText: EditText = findViewById(R.id.editTextPrice)

        originEditText.text.clear()
        destinationEditText.text.clear()
        departureDateEditText.text.clear()
        departureTimeEditText.text.clear()
        priceEditText.text.clear()
    }
}
