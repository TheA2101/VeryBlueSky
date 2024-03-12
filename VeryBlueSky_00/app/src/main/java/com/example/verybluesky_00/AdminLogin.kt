package com.example.verybluesky_00


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminLogin : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var admin_code: EditText
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        admin_code = findViewById(R.id.editTextAdminCode)
        firebaseAuth = FirebaseAuth.getInstance()

        val loginAdmin = findViewById<Button>(R.id.loginButton)



        loginAdmin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val adminCode = admin_code.text.toString()
            loginAsAdmin(email,password,adminCode)
        }

    }

    private fun loginAsAdmin(email: String, password: String, adminC: String) {
        if (!(email.contains("admin") && adminC == "admin123")) {
            showToast("Only admin credentials are allowed.")
            return
        }

        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        usersCollection
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Successful login
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminHome::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Incorrect email or password
                    Toast.makeText(this, "Login failed! Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Error while querying Firestore
                Toast.makeText(this, "Error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }








    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}