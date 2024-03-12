package com.example.verybluesky_00

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        firebaseAuth = FirebaseAuth.getInstance()

        val loginUser = findViewById<Button>(R.id.loginButton)
        val helpButton = findViewById<View>(R.id.help_button)
        val adminButton = findViewById<Button>(R.id.adminLoginButton)



        loginUser.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            login(email, password)
            // in login start user sesh
        }
        //help
        helpButton.setOnClickListener {
            val intent = Intent(this, Help ::class.java)
            startActivity(intent)
        }


        //login as admin
        adminButton.setOnClickListener {
            // Open AdminLogin.kt when the "Login" button is clicked
            val intent = Intent(this, AdminLogin::class.java)
            startActivity(intent)
        }


    }

    private fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Login successful!")
                    // Handle successful login, navigate to another activity
                    val intent = Intent(this, Home::class.java)

                    //if they are confirmed start sesh with them
                    val LoginSendEmail = email
                    val LoginSendPass = password
                    //user sesh 0
                    intent.putExtra("UserInputEmail", LoginSendEmail)
                    intent.putExtra("UserInputPass", LoginSendPass)

                    startActivity(intent)
                    finish()
                } else {
                    showToast("Login failed! Please try again.")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}