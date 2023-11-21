package com.example.parking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parking.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val textParkEasy = findViewById<TextView>(R.id.text_park_easy)
        textParkEasy.setOnClickListener {
            // Handle click event here
            navigateToPaymentActivity()
        }
        // Set listener for item selection in the bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_buy_pass -> {
                    // Handle Buy Pass selection
                    // Example: switchFragment(BuyPassFragment())
                    startActivity(Intent(this, PassPurchaseActivity::class.java))
                    true
                }
                R.id.navigation_history -> {
                    // Handle History selection
                    // Example: switchFragment(HistoryFragment())
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Inflate the user profile layout into the container
        val container: ViewGroup = findViewById(R.id.container)
        val userProfileView = LayoutInflater.from(this).inflate(R.layout.item_user_profile, container, false)
        container.addView(userProfileView)

        // Set user profile information here (use findViewById to get TextViews and set values)
        currentUser?.let { user ->
            // Reference to the "users" node in Firebase Realtime Database
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

            // Read data from Firebase
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User data exists, retrieve and update TextView elements
                        val studentName = dataSnapshot.child("studentName").getValue(String::class.java)
                        val erpId = dataSnapshot.child("erpId").getValue(String::class.java)
                        val vehicleNumber = dataSnapshot.child("vehicleNumber").getValue(String::class.java)

                        // Update the TextView elements in the user profile layout
                        val textFullName: TextView = userProfileView.findViewById(R.id.textFullName)
                        val textErpId: TextView = userProfileView.findViewById(R.id.textErpId)
                        val textVehicleNumber: TextView = userProfileView.findViewById(R.id.textVehicleNumber)

                        textFullName.text = "Full Name: $studentName"
                        textErpId.text = "ERP ID: $erpId"
                        textVehicleNumber.text = "Vehicle Number: $vehicleNumber"

                        // Pass Status
                        val hasPass = dataSnapshot.child("purchases").exists()
                        val textPassStatus: TextView = userProfileView.findViewById(R.id.textPassStatus)
                        textPassStatus.text = "Pass Status: ${if (hasPass) "Active" else "Inactive"}"

                        if (hasPass) {
                            // Pass Type
                            val passType = dataSnapshot.child("purchases").child("passType").getValue(Int::class.java)
                            val textPassType: TextView = userProfileView.findViewById(R.id.textPassType)
                            textPassType.text = "Pass Type: ${getPassTypeName(passType)}"

                            // Pass Purchase Date
                            val purchaseDate = dataSnapshot.child("purchases").child("purchaseDate").getValue(Long::class.java)
                            val textPurchaseDate: TextView = userProfileView.findViewById(R.id.textPurchaseDate)
                            textPurchaseDate.text = "Purchase Date: ${formatDate(purchaseDate)}"

                            // Pass Expiry Date
                            val expiryDate = calculateExpiryDate(purchaseDate, passType)
                            val textExpiryDate: TextView = userProfileView.findViewById(R.id.textExpiryDate)
                            textExpiryDate.text = "Expiry Date: ${formatDate(expiryDate)}"
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    handleDatabaseError(this@MainActivity, databaseError)
                }
            })
        }
    }

    private fun getPassTypeName(passType: Int?): String {
        // Logic to map pass type integer to pass type name
        // Modify this according to your pass type mapping
        return when (passType) {
            1 -> "Daily Pass"
            2 -> "Weekly Pass"
            3 -> "Monthly Pass"
            else -> "Unknown Pass Type"
        }
    }

    private fun formatDate(timestamp: Long?): String {
        // Logic to format timestamp to a readable date
        // Modify this according to your date formatting needs
        return timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(it))
        } ?: "N/A"
    }

    private fun calculateExpiryDate(purchaseDate: Long?, passType: Int?): Long? {
        // Logic to calculate expiry date based on pass type
        // Modify this according to your expiry date calculation
        return purchaseDate?.let {
            when (passType) {
                1 -> it + TimeUnit.DAYS.toMillis(1)  // Daily Pass
                2 -> it + TimeUnit.DAYS.toMillis(7)  // Weekly Pass
                3 -> it + TimeUnit.DAYS.toMillis(30) // Monthly Pass
                else -> null
            }
        }
    }

    private fun navigateToPaymentActivity() {
        val intent = Intent(this, PaymentActivity::class.java)
        startActivity(intent)
        // You may also finish() this activity if you want to close it when navigating to PaymentActivity
        // finish()
    }

    private fun handleDatabaseError(context: Context, databaseError: DatabaseError) {
        // Extract the error message from the DatabaseError
        val errorMessage = databaseError.message

        // Display a toast with the error message
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

        // Redirect the user to LoginActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
