package com.example.verybluesky_00

import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookFlight : AppCompatActivity() {

    private lateinit var departureDatePickerDialog: DatePickerDialog
    private lateinit var returnDatePickerDialog: DatePickerDialog
    private lateinit var departureDatePickerButton: Button
    private lateinit var returnDatePickerButton: Button

    private var isDepartureDatePickerPressed = false
    private var isReturnDatePickerPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_flight)

        // user session 1
        val BookUser = intent.getStringExtra("UserInputEmail")
        val BookPass = intent.getStringExtra("UserInputPass")

        departureDatePickerButton = findViewById(R.id.date_from_fill)
        departureDatePickerButton.text = getTodaysDate()

        returnDatePickerButton = findViewById(R.id.date_to_fill)
        val returnDate = getTodaysDatePlusOneDay()
        returnDatePickerButton.text = returnDate
        val helpButton = findViewById<View>(R.id.help_button)

        initDatePicker()

        departureDatePickerButton.setOnClickListener {
            departureDatePickerDialog.show()
            if (isDepartureDatePickerPressed) {
                isDepartureDatePickerPressed = false
                departureDatePickerButton.setTextColor(Color.parseColor("#161B33")) // Change back to default text color
            }
        }

        returnDatePickerButton.setOnClickListener {
            returnDatePickerDialog.show()
            if (isReturnDatePickerPressed) {
                isReturnDatePickerPressed = false
                returnDatePickerButton.setTextColor(Color.parseColor("#161B33")) // Change back to default text color
            }
        }

        val searchButton = findViewById<Button>(R.id.flightsearchButton)
        searchButton.setOnClickListener {
            if (validateFields() && validateRange() && validateDepartureDate()) {
                val intent = Intent(this, FilteredFlightsActivity::class.java)

                // user session 2
                intent.putExtra("UserInputEmail", BookUser)
                intent.putExtra("UserInputPass", BookPass)

                intent.putExtra("from", findViewById<EditText>(R.id.from_fill).text.toString())
                intent.putExtra("to", findViewById<EditText>(R.id.to_fill).text.toString())
                intent.putExtra("rangeFrom", findViewById<EditText>(R.id.range_from_fill).text.toString())
                intent.putExtra("rangeTo", findViewById<EditText>(R.id.range_to_fill).text.toString())
                intent.putExtra("departureDate", departureDatePickerButton.text.toString())
                intent.putExtra("departureDate2", returnDatePickerButton.text.toString())

                startActivity(intent)
            }
        }

        helpButton.setOnClickListener {
            val intent = Intent(this, Help::class.java)
            startActivity(intent)
        }
    }

    private fun getTodaysDate(): String {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return dateFormat.format(cal.time)
    }

    private fun getTodaysDatePlusOneDay(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return dateFormat.format(cal.time)
    }

    private fun initDatePicker() {
        val departureDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val formattedMonth = month + 1
            val date = makeDateString(day, formattedMonth, year)
            departureDatePickerButton.text = date
            isDepartureDatePickerPressed = true // Set the flag to true
            departureDatePickerButton.setTextColor(Color.parseColor("#161B33")) // Change the text color to BLACK
        }

        val returnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val formattedMonth = month + 1
            val date = makeDateString(day, formattedMonth, year)
            returnDatePickerButton.text = date
            isReturnDatePickerPressed = true // Set the flag to true
            returnDatePickerButton.setTextColor(Color.parseColor("#161B33")) // Change the text color to BLACK
        }

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val style = AlertDialog.THEME_HOLO_LIGHT

        departureDatePickerDialog = DatePickerDialog(this, style, departureDateSetListener, year, month, day)
        returnDatePickerDialog = DatePickerDialog(this, style, returnDateSetListener, year, month, day)
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return String.format("%02d/%02d/%04d", month, day, year)
    }

    private fun validateRange(): Boolean {
        val rangeFrom = findViewById<EditText>(R.id.range_from_fill).text.toString()
        val rangeTo = findViewById<EditText>(R.id.range_to_fill).text.toString()

        if (rangeFrom.isBlank() && rangeTo.isBlank()) {
            showToast("No Range Entered")
            return true
        }

        val rangeFromValue = rangeFrom.toDoubleOrNull()
        val rangeToValue = rangeTo.toDoubleOrNull()

        if (rangeFromValue == null || rangeToValue == null || rangeFromValue >= rangeToValue) {
            showToast("Please enter a valid range")
            return false
        }

        return true
    }

    private fun validateFields(): Boolean {
        val from = findViewById<EditText>(R.id.from_fill).text.toString()
        val to = findViewById<EditText>(R.id.to_fill).text.toString()

        if (from.isEmpty() || to.isEmpty()) {
            showToast("Please fill in all fields")
            return false
        }

        return true
    }

    private fun validateDepartureDate(): Boolean {
        val currentDate = Calendar.getInstance()
        val departureDateStr = departureDatePickerButton.text.toString()
        val departureDate = parseDate(departureDateStr)

        if (departureDate.before(currentDate)) {
            showToast("Departure date cannot be before today.")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun parseDate(dateStr: String): Calendar {
        val parts = dateStr.split("/")
        if (parts.size == 3) {
            val year = parts[2].toInt()
            val month = parts[0].toInt() - 1 // Subtract 1 because Calendar months are zero-based
            val day = parts[1].toInt()
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            return cal
        }
        return Calendar.getInstance() // Return current date by default if parsing fails
    }
}