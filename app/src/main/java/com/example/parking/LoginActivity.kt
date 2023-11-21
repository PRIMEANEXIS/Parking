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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (isValidCredentials(email, password)) {
                signInWithEmailAndPassword(email, password)
            }
        }

        binding.signupText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun isValidCredentials(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showError(getString(R.string.error_empty_email), binding.loginEmail)
            return false
        }

        if (!isValidEmail(email)) {
            showError(getString(R.string.error_invalid_email), binding.loginEmail)
            return false
        }

        if (password.isEmpty()) {
            showError(getString(R.string.error_empty_password), binding.loginPassword)
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    onLoginSuccess()
                } else {
                    val errorMessage = task.exception?.message ?: getString(R.string.login_failed)
                    showError(errorMessage, binding.loginButton)
                }
            }
    }

    private fun onLoginSuccess() {
        Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String, view: View) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
}
