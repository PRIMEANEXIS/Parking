package com.example.parking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parking.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val errorQueue: MutableList<Pair<String, View>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            // Clear previous errors
            errorQueue.clear()

            // Validate and enqueue errors individually
            if (!isValidEmail(email)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_email), binding.loginEmail))
            }

            if (!isValidPassword(password)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_password), binding.loginPassword))
            }

            // Display errors sequentially
            displayNextError()

            if (errorQueue.isEmpty()) {
                // Proceed with login only if there are no validation errors
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Display the Firebase authentication error message
                            val errorMessage = task.exception?.message ?: getString(R.string.login_failed)
                            showError(errorMessage, binding.loginButton)
                        }
                    }
            }
        }

        binding.signupText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    // Individual validation functions

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Password should at least be 6 characters
        if (password.length < 6) {
            showError(getString(R.string.error_password_length), binding.loginPassword)
            return false
        }

        // Password should contain at least One Capital letter
        if (!password.any { it.isUpperCase() }) {
            showError(getString(R.string.error_password_uppercase), binding.loginPassword)
            return false
        }

        // Password should contain at least One Small letter
        if (!password.any { it.isLowerCase() }) {
            showError(getString(R.string.error_password_lowercase), binding.loginPassword)
            return false
        }

        // Password should contain at least One Special Symbol
        val specialSymbols = "!@#$%^&*()_+-=[]{}|;':,.<>/?"
        if (!password.any { specialSymbols.contains(it) }) {
            showError(getString(R.string.error_password_special_symbol), binding.loginPassword)
            return false
        }

        return true
    }

    private fun showError(message: String, view: View) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun displayNextError() {
        if (errorQueue.isNotEmpty()) {
            val (errorMessage, view) = errorQueue.removeAt(0)
            showError(errorMessage, view)
        }
    }
}
