package com.example.verybluesky_00


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val button1 = findViewById<Button>(R.id.btnLogin)
        val button2 = findViewById<Button>(R.id.btnSignUp)
        val helpButton = findViewById<View>(R.id.help_button)



        button1.setOnClickListener {
            // Open Login.kt when the "Login" button is clicked
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }


       button2.setOnClickListener {
            // Open SignUpActivity when the "Sign Up" button is clicked
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
        //help
        helpButton.setOnClickListener {
            val intent = Intent(this, Help ::class.java)
            startActivity(intent)
        }
    }
}
