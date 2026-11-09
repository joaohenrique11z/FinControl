package com.example.fincontrol

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.model.Transaction

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentTransactions = view.findViewById<RecyclerView>(R.id.rv_recent_transactions)
        
        val mockTransactions = listOf(
            Transaction(1, "Mercado", "Alimentação", 150.0, true),
            Transaction(2, "Salário", "Renda", 5000.0, false),
            Transaction(3, "Uber", "Transporte", 35.50, true),
            Transaction(4, "Aluguel", "Moradia", 1200.0, true),
            Transaction(5, "Rendimento", "Investimento", 120.0, false),
            Transaction(6, "Restaurante", "Alimentação", 85.0, true)
        )
        
        rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvRecentTransactions.adapter = TransactionAdapter(mockTransactions)
    }
}