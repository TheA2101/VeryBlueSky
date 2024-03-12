package com.example.verybluesky_00

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var homeUser: String
    private lateinit var homePass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // user session
        homeUser = intent.getStringExtra("UserInputEmail")!!
        homePass = intent.getStringExtra("UserInputPass")!!

        Toast.makeText(this, "$homeUser Logged in", Toast.LENGTH_SHORT).show()

        // Fetch and display cheapest flight
        fetchAndDisplayCheapestFlight()

        // Fetch and display nearest upcoming tracked flight
        fetchAndDisplayNearestFlight()

        val bookFlightButton = findViewById<View>(R.id.book_flight_button)
        val trackFlightButton = findViewById<View>(R.id.track_flight_button)
        val helpButton = findViewById<View>(R.id.help_button)
        val prevButton = findViewById<View>(R.id.previous_flights_button)

        //book
        bookFlightButton.setOnClickListener {
            val intent = Intent(this, BookFlight::class.java)
            // Send user and pass with it
            //user sesh 1
            intent.putExtra("UserInputEmail", homeUser)
            intent.putExtra("UserInputPass", homePass)
            startActivity(intent)
        }
        //track
        trackFlightButton.setOnClickListener{
            val intent = Intent(this, TrackFlights::class.java)
            intent.putExtra("UserInputEmail", homeUser)
            intent.putExtra("UserInputPass", homePass)
            startActivity(intent)
        }

        // Help
        helpButton.setOnClickListener {
            val intent = Intent(this, Help::class.java)
            startActivity(intent)
        }

        //Prev
        prevButton.setOnClickListener {
            val intent = Intent(this, PrevFlights::class.java)
            intent.putExtra("UserInputEmail", homeUser)
            intent.putExtra("UserInputPass", homePass)
            startActivity(intent)
        }
    }

    private fun fetchAndDisplayCheapestFlight() {
        val db = FirebaseFirestore.getInstance()
        val flightRecordsCollection = db.collection("Future flights")

        var cheapestPrice = Double.MAX_VALUE
        var cheapestFlightInfo = ""

        flightRecordsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val price = document.getString("price")?.toDoubleOrNull()
                    val depDate = document.getString("departure date")!!
                    val depTime = document.getLong("departure time").toString()
                    val dest = document.getString("destination")
                    val origin = document.getString("origin")

                    if (price != null && price < cheapestPrice) {
                        cheapestPrice = price

                        cheapestFlightInfo = "Departure Date: $depDate\n" +
                                "Departure Time: $depTime\n" +
                                "Destination: $dest\n" +
                                "Origin: $origin\n" +
                                "Price: $price"
                    }
                }

                // Display cheapest flight info in the TextView
                val flightInfoTextView = findViewById<TextView>(R.id.flightInfoTextView)
                flightInfoTextView.text = cheapestFlightInfo

                // Add click listener to the TextView if you want to navigate to FlightDetail
                flightInfoTextView.setOnClickListener {
                    val intent = Intent(this, FlightDetail::class.java)
                    // User session information
                    intent.putExtra("UserInputEmail", homeUser)
                    intent.putExtra("UserInputPass", homePass)
                    intent.putExtra("flightInfo", cheapestFlightInfo)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun parseDate(dateString: String): Long {
        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val date = formatter.parse(dateString)
        return date?.time ?: 0
    }


    private fun fetchAndDisplayNearestFlight() {
        val db = FirebaseFirestore.getInstance()
        val trackRecordsCollection = db.collection("Track Flights")

        val currentDate = Calendar.getInstance().timeInMillis

        var nearestDateDifference = Long.MAX_VALUE
        var nearestFlightInfo = ""

        trackRecordsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val depDate = document.getString("departure date") ?: continue
                    val depDateTimestamp = parseDate(depDate)

                    val dateDifference = depDateTimestamp - currentDate

                    if (dateDifference >= 0 && dateDifference < nearestDateDifference) {
                        nearestDateDifference = dateDifference

                        val destination = document.getString("destination")
                        val origin = document.getString("origin")
                        val price = document.getString("price")

                        nearestFlightInfo = "Departure Date: $depDate\n" +
                                "Destination: $destination\n" +
                                "Origin: $origin\n" +
                                "Price: $price"
                    }
                }

                // Display nearest flight info in the trackInfoTextView
                val trackInfoTextView = findViewById<TextView>(R.id.trackInfoTextView)
                trackInfoTextView.text = nearestFlightInfo

                // Add click listener to the TextView if you want to navigate to FlightDetail
                trackInfoTextView.setOnClickListener {
                    val intent = Intent(this, TrackFlights::class.java)
                    // User session information
                    intent.putExtra("UserInputEmail", homeUser)
                    intent.putExtra("UserInputPass", homePass)
                    intent.putExtra("flightInfo", nearestFlightInfo)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }
}