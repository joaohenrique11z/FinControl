package com.example.fincontrol

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.model.Tip

class TipsFragment : Fragment(R.layout.fragment_tips) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTips = view.findViewById<RecyclerView>(R.id.rv_tips)
        
        val mockTips = listOf(
            Tip("Reserva de Emergência", "Guarde o equivalente a 6 meses dos seus custos fixos."),
            Tip("Regra 50/30/20", "50% para necessidades, 30% para desejos e 20% para investimentos."),
            Tip("Pague-se primeiro", "Ao receber o salário, invista imediatamente antes de gastar."),
            Tip("Evite dívidas ruins", "Cuidado com os juros do rotativo do cartão de crédito.")
        )
        
        rvTips.layoutManager = LinearLayoutManager(requireContext())
        rvTips.adapter = TipAdapter(mockTips)
    }
}