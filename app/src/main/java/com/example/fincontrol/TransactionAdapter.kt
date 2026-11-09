package com.example.fincontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.model.Transaction
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDesc: TextView = view.findViewById(R.id.tv_transaction_desc)
        val tvCategory: TextView = view.findViewById(R.id.tv_transaction_category)
        val tvValue: TextView = view.findViewById(R.id.tv_transaction_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvDesc.text = transaction.description
        holder.tvCategory.text = transaction.category

        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val formattedValue = format.format(transaction.value)

        if (transaction.isExpense) {
            holder.tvValue.text = "- $formattedValue"
            holder.tvValue.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorExpense))
        } else {
            holder.tvValue.text = "+ $formattedValue"
            holder.tvValue.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorIncome))
        }
    }

    override fun getItemCount() = transactions.size
}
