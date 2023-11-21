import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parking.R
import com.example.parking.TransactionHistoryItem
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryAdapter(private val historyList: MutableList<TransactionHistoryItem>) :
    RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    private val TAG = "TransactionHistoryAdapter"

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val passTypeTextView: TextView = itemView.findViewById(R.id.textViewPassType)
        val purchaseDateTextView: TextView = itemView.findViewById(R.id.textViewPurchaseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyList[position]

        // Set data to the views
        holder.passTypeTextView.text = "Pass Type: ${getPassTypeName(historyItem.passType)}"
        holder.purchaseDateTextView.text = "Purchase Date: ${formatDate(historyItem.purchaseDate)}"

        // Debugging: Print information about the current item being bound
        Log.d(TAG, "onBindViewHolder - Item at position $position bound.")
    }

    override fun getItemCount(): Int {
        // Debugging: Print the size of the historyList
        Log.d(TAG, "getItemCount - historyList size: ${historyList.size}")
        return historyList.size
    }

    fun updateData(newData: List<TransactionHistoryItem>) {
        // Debugging: Print information about the updateData operation
        Log.d(TAG, "updateData - Updating data with new list size: ${newData.size}")

        historyList.clear()
        historyList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun getPassTypeName(passType: Int): String {
        // Debugging: Print information about passType mapping
        Log.d(TAG, "getPassTypeName - Mapping passType $passType to pass type name")

        return when (passType) {
            1 -> "Daily Pass"
            2 -> "Weekly Pass"
            3 -> "Monthly Pass"
            else -> "Unknown Pass Type"
        }
    }

    private fun formatDate(timestamp: Long): String {
        // Debugging: Print information about date formatting
        Log.d(TAG, "formatDate - Formatting timestamp $timestamp to a readable date")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
