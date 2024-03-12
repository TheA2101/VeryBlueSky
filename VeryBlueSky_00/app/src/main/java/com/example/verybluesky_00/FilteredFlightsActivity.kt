package com.example.verybluesky_00

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

class FilteredFlightsActivity : AppCompatActivity() {

    private lateinit var filterUser: String
    private lateinit var filterPass: String
    private lateinit var tfliteInterpreter: Interpreter
    // Define constants for feature indices and sizes
    // Define variables for feature indices and sizes
    private val NUM_FEATURES = 3  // Number of features in the input vector
    private val DEST_FEATURE_INDEX = 0  // Index for destination feature
    private val ORIGIN_FEATURE_INDEX = 1  // Index for origin feature
    private val DEP_DATE_FEATURE_INDEX = 2  // Index for departure date feature




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered_flights)

        // user sesh 1
        filterUser = intent.getStringExtra("UserInputEmail")!!
        filterPass = intent.getStringExtra("UserInputPass")!!

        val tasksLayout = findViewById<LinearLayout>(R.id.tasksLayout)


        val from = intent.getStringExtra("from")
        val to = intent.getStringExtra("to")
        val rangeFrom = intent.getStringExtra("rangeFrom")?.toLongOrNull()
        val rangeTo = intent.getStringExtra("rangeTo")?.toLongOrNull()
        val departureDate = intent.getStringExtra("departureDate")!!
        val departureDate2 = intent.getStringExtra("departureDate2")!!

        val departureDateTimestamp = parseDate(departureDate)
        val departureDate2Timestamp = parseDate(departureDate2)

        val db = FirebaseFirestore.getInstance()
        val flightRecordsCollection = db.collection("Future flights")

        var noFlightsFound = true

        flightRecordsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val depDate = document.getString("departure date")!!
                    val depTime = document.getLong("departure time").toString()
                    val dest = document.getString("destination")
                    val origin = document.getString("origin")
                    val price = document.getString("price")?.toLongOrNull()

                    val depDateTimestamp = parseDate(depDate)

                    if ((depDateTimestamp in departureDateTimestamp..departureDate2Timestamp) &&
                        ((origin == from) && (dest == to)) &&
                        (price != null && rangeFrom != null && rangeTo != null && price in rangeFrom..rangeTo)
                    ) {
                        noFlightsFound = false

                        val flightInfo = "Departure Date: $depDate\n" +
                                "Departure Time: $depTime\n" +
                                "Destination: $dest\n" +
                                "Origin: $origin\n" +
                                "Price: $price"

                        val button = createFlightButton(this, flightInfo)
                        tasksLayout.addView(button)
                    }
                }

                if (noFlightsFound) {
                    showToast("No available flights match the criteria")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching flight records: ${exception.message}")
            }

        val sortButton = findViewById<Button>(R.id.sortButton)
        sortButton.setOnClickListener {
            // Sort the flights by price
            sortFlightsByPrice()
        }
        tfliteInterpreter = loadModelInterpreter("flight_delay_model.tflite")

        val aiButton = findViewById<Button>(R.id.ai_Button)
        aiButton.setOnClickListener {
            fetchAndPredictFlights()
        }

    }
    //-----------------------
    private fun fetchAndPredictFlights() {
        val flightRecordsCollection = FirebaseFirestore.getInstance().collection("Future flights")

        flightRecordsCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val depDate = document.getString("departure date")!!
                    val dest = document.getString("destination")!!
                    val origin = document.getString("origin")!!


                    val inputFeatureVector = prepareInputFeatureVector(depDate, dest, origin)

                    //crashes here
                    val prediction = performInference(tfliteInterpreter, inputFeatureVector)
                    Toast.makeText(this,"before delay chance",Toast.LENGTH_SHORT).show()

                    val delayChanceText = "Delay Chance: ${(prediction * 100).roundToInt()}%"
                    Toast.makeText(this,"$delayChanceText",Toast.LENGTH_SHORT).show()
                    Toast.makeText(this,"after funcs",Toast.LENGTH_SHORT).show()
                    // updateButtonWithDelayChance(depDate, dest, origin, delayChanceText)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching flight records: ${exception.message}")
            }
    }

    private fun prepareInputFeatureVector(depDate: String, dest: String, origin: String): FloatArray {
        val encodedDest = mapDestToOneHot(dest)
        val encodedOrigin = mapOriginToOneHot(origin)
        val depDateFloat = convertDepDateToFloat(depDate)

        val inputFeatureVector = FloatArray(NUM_FEATURES) { 0.0f }
        inputFeatureVector[DEST_FEATURE_INDEX] = encodedDest
        inputFeatureVector[ORIGIN_FEATURE_INDEX] = encodedOrigin
        inputFeatureVector[DEP_DATE_FEATURE_INDEX] = depDateFloat

        return inputFeatureVector
    }
    private fun mapDestToOneHot(dest: String): Float {
        val destinations = arrayOf("dest1", "dest2", "dest3") // Replace with your actual destination categories
        val destIndex = destinations.indexOf(dest)

        // Create a one-hot encoded representation
        val oneHotEncoded = FloatArray(destinations.size) { 0.0f }
        if (destIndex != -1) {
            oneHotEncoded[destIndex] = 1.0f
        }

        return oneHotEncoded[0] // Return the first value of the one-hot encoded vector
    }

    private fun mapOriginToOneHot(origin: String): Float {
        val origins = arrayOf("origin1", "origin2", "origin3") // Replace with your actual origin categories
        val originIndex = origins.indexOf(origin)

        // Create a one-hot encoded representation
        val oneHotEncoded = FloatArray(origins.size) { 0.0f }
        if (originIndex != -1) {
            oneHotEncoded[originIndex] = 1.0f
        }

        return oneHotEncoded[0] // Return the first value of the one-hot encoded vector
    }
    private fun convertDepDateToFloat(depDate: String): Float {
        val referenceDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).parse("01/01/2022") // Replace with your reference date
        val depDateParsed = SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(depDate)

        val daysSinceReference = (depDateParsed.time - referenceDate.time) / (1000 * 60 * 60 * 24)

        return daysSinceReference.toFloat()
    }

    private fun performInference(interpreter: Interpreter, inputFeatureVector: FloatArray): Float {
        val output = FloatArray(1)
        interpreter.run(inputFeatureVector, output)
        return output[0]
    }

    private fun updateButtonWithDelayChance(depDate: String, dest: String, origin: String, delayChanceText: String) {
        val buttonId = generateButtonId(depDate, dest, origin)
        val button = findViewById<Button>(buttonId)
        button?.let {
            val buttonText = it.text.toString()
            val updatedText = "$buttonText\n$delayChanceText"
            it.text = updatedText
        }
    }

    private fun generateButtonId(depDate: String, dest: String, origin: String): Int {
        return ("$depDate-$dest-$origin").hashCode()
    }

    private fun loadModelInterpreter(modelPath: String): Interpreter {
        val assetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(mappedByteBuffer)
    }

    //-----------------------



    private fun createFlightButton(context: AppCompatActivity, text: String): Button {
        val button = Button(context)
        button.text = text
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 16)
        button.layoutParams = layoutParams

        button.setOnClickListener {
            val intent = Intent(context, FlightDetail::class.java)
            //user sesh 2
            intent.putExtra("UserInputEmail", filterUser)
            intent.putExtra("UserInputPass", filterPass)
            intent.putExtra("flightInfo", text)
            startActivity(intent)
        }

        return button
    }

    private fun parseDate(dateString: String): Long {
        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val date = formatter.parse(dateString)
        return date?.time ?: 0
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sortFlightsByPrice() {
        // Sort the flights by price
        val tasksLayout = findViewById<LinearLayout>(R.id.tasksLayout)
        val flightButtons = mutableListOf<Button>()

        for (i in 0 until tasksLayout.childCount) {
            val childView = tasksLayout.getChildAt(i)
            if (childView is Button) {
                flightButtons.add(childView)
            }
        }

        flightButtons.sortBy { button ->
            val flightInfo = button.text.toString()
            // Parse the price from the flight info string
            val priceString = flightInfo.substringAfter("Price: ").replace("$", "")
            val price = priceString.toDoubleOrNull() ?: 0.0
            price
        }

        // Remove existing buttons and add sorted buttons back to the layout
        tasksLayout.removeAllViews()
        for (flightButton in flightButtons) {
            tasksLayout.addView(flightButton)
        }
    }
}