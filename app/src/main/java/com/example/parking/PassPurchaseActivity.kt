package com.example.parking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

class PassPurchaseActivity : AppCompatActivity() {

    private lateinit var dailyPassButton: Button
    private lateinit var weeklyPassButton: Button
    private lateinit var monthlyPassButton: Button

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = firebaseAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_purchase)

        dailyPassButton = findViewById(R.id.Dailypassbutton)
        weeklyPassButton = findViewById(R.id.Weeklypassbutton)
        monthlyPassButton = findViewById(R.id.Monthlypassbutton)

        // Set click listeners for pass purchase buttons
        dailyPassButton.setOnClickListener { purchasePass(1) } // Pass Type 1 for Daily Pass
        weeklyPassButton.setOnClickListener { purchasePass(2) } // Pass Type 2 for Weekly Pass
        monthlyPassButton.setOnClickListener { purchasePass(3) } // Pass Type 3 for Monthly Pass
    }

    private fun purchasePass(passType: Int) {
        val uid = currentUser?.uid
        if (uid != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(uid)

            // Check if the user already has an active pass
            databaseReference.child("purchases").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // User does not have an active pass, proceed with purchase
                        val purchaseData = mapOf(
                            "passType" to passType,
                            "purchaseDate" to ServerValue.TIMESTAMP
                            // Add other relevant fields as needed
                        )

                        // Write purchase data to the "purchases" node
                        databaseReference.child("purchases").setValue(purchaseData)
                            .addOnSuccessListener {
                                // Purchase successful
                                // Store the same purchase data in the "transactionHistory" node
                                databaseReference.child("transactionHistory").push().setValue(purchaseData)

                                // You can redirect to com.example.parking.MainActivity or handle other actions
                                Toast.makeText(this@PassPurchaseActivity, "Purchase Successful", Toast.LENGTH_SHORT).show()

                                // Redirect to com.example.parking.MainActivity
                                startActivity(Intent(this@PassPurchaseActivity, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                // Handle purchase failure
                                Toast.makeText(this@PassPurchaseActivity, "Purchase Failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // User already has an active pass, display a message or handle accordingly
                        Toast.makeText(this@PassPurchaseActivity, "You already have an active pass", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
