package com.example.parking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parking.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val errorQueue: MutableList<Pair<String, View>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val fullName = standardizeFullName(binding.signupStudentName.text.toString()) // Convert to standard format
            val erpId = binding.signupErpId.text.toString()
            val vehicleNumber = binding.signupVehicleNumber.text.toString()

            // Clear previous errors
            errorQueue.clear()

            // Validate and enqueue errors individually
            if (!isValidEmail(email)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_email), binding.signupEmail))
            }

            if (!isValidPassword(password)) {
                return@setOnClickListener // Error is handled in isValidPassword
            }

            if (!isValidFullName(fullName)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_full_name), binding.signupStudentName))
            }

            if (!isValidErpId(erpId)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_erp_id), binding.signupErpId))
            }

            if (!isValidVehicleNumber(vehicleNumber)) {
                errorQueue.add(Pair(getString(R.string.error_invalid_vehicle_number), binding.signupVehicleNumber))
            }

            // Display errors sequentially
            displayNextError()
        }
    }

    // Individual validation functions

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{6,}$")

        if (password.length < 6) {
            showError(getString(R.string.error_password_length), binding.signupPassword)
            return false
        }

        if (!passwordPattern.matches(password)) {
            showError(getString(R.string.error_password_complexity), binding.signupPassword)
            return false
        }

        return true
    }

    private fun isValidFullName(fullName: String): Boolean {
        return fullName.isNotEmpty() && fullName.split(" ").size >= 2 // Assuming a minimum of two parts in a full name
    }

    private fun isValidErpId(erpId: String): Boolean {
        val erpIdPattern = Regex("^[a-zA-Z]{2}-\\d{5}$")
        return erpId.isNotEmpty() && erpIdPattern.matches(erpId)
    }

    private fun isValidVehicleNumber(vehicleNumber: String): Boolean {
        val vehicleNumberPattern = Regex("^[A-Z]{2}\\d{2}[A-Z]{2}\\d{4}$")
        return vehicleNumber.isNotEmpty() && vehicleNumberPattern.matches(vehicleNumber)
    }

    private fun standardizeFullName(fullName: String): String {
        return fullName.split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }

    private fun saveUserDataToDatabase(
        userId: String?,
        fullName: String,
        erpId: String,
        vehicleNumber: String
    ) {
        userId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            val user = User(it, fullName, erpId, vehicleNumber)
            databaseReference.child(it).setValue(user)
        }
    }

    private fun redirectToLogin() {
        Toast.makeText(this, getString(R.string.signup_successful), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String, view: View) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun displayNextError() {
        if (errorQueue.isNotEmpty()) {
            val (errorMessage, view) = errorQueue.removeAt(0)
            showError(errorMessage, view)
        } else {
            // If no more errors, proceed with account creation
            createAccount()
        }
    }

    private fun createAccount() {
        // Validate once again before creating the account
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPassword.text.toString()

        if (isValidEmail(email) && isValidPassword(password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid
                        val capitalizedFullName = standardizeFullName(binding.signupStudentName.text.toString())
                        saveUserDataToDatabase(
                            userId,
                            capitalizedFullName,  // Capitalize full name before saving
                            binding.signupErpId.text.toString(),
                            binding.signupVehicleNumber.text.toString()
                        )
                        redirectToLogin()
                    } else {
                        // Extract and display the Firebase authentication error message
                        val errorMessage = task.exception?.message ?: getString(R.string.signup_unsuccessful)
                        showError(errorMessage, binding.signupButton)
                    }
                }
        }
    }
}