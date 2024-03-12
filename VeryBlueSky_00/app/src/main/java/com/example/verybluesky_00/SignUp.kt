package com.example.verybluesky_00

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        val helpButton = findViewById<View>(R.id.help_button)


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //help
        helpButton.setOnClickListener {
            val intent = Intent(this, Help ::class.java)
            startActivity(intent)
        }

    }

    fun onSignUpButtonClick(view: View) {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if ((!email.contains("admin")) && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""

                            // Add user data to the "users" collection in Firestore
                            val user = hashMapOf(
                                "email" to email,
                                "password" to password
                            )

                            firestore.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    showToast("Sign-up successful!")
                                    val intent = Intent(this, LogIn::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    showToast("Error adding user data: ${e.message}")
                                }
                        } else {
                            showToast("Sign-up failed: ${task.exception?.message}")
                        }
                    }
            } else {
                showToast("Passwords don't match")
            }
        } else {
            showToast("Those Fields are not allowed / can't sign up as admin")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
