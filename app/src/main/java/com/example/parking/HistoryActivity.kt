package com.example.parking

import TransactionHistoryAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private val TAG = "HistoryActivity" // Add a tag for log messages

    private val database = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val uid = currentUser?.uid

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up RecyclerView adapter
        adapter = TransactionHistoryAdapter(ArrayList())
        recyclerView.adapter = adapter

        // Fetch and display transaction history
        fetchTransactionHistory()
    }

    private fun fetchTransactionHistory() {
        uid?.let { userUid ->
            // Check if the user is authenticated
            if (currentUser != null) {
                val userRef = database.reference.child("users").child(userUid)
                val historyRef = userRef.child("transactionHistory")

                val historyList = ArrayList<TransactionHistoryItem>()

                historyRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        historyList.clear()
                        for (historySnapshot in snapshot.children) {
                            val passType = historySnapshot.child("passType").getValue(Int::class.java)
                            val purchaseDate =
                                historySnapshot.child("purchaseDate").getValue(Long::class.java)

                            // Create a TransactionHistoryItem and add it to the list
                            val historyItem =
                                TransactionHistoryItem(passType ?: 0, purchaseDate ?: 0)
                            historyList.add(historyItem)
                        }

                        // Update data in the existing adapter
                        adapter.updateData(historyList)

                        // Debugging: Print the size of the updated list
                        Log.d(TAG, "Updated historyList size: ${historyList.size}")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Debugging: Print an error message
                        Log.e(TAG, "Error fetching transaction history: ${error.message}")
                    }
                })
            } else {
                // User is not authenticated, handle accordingly (redirect to login, etc.)
                Log.w(TAG, "User is not authenticated. Redirecting to login screen.")
            }
        }
    }
}
