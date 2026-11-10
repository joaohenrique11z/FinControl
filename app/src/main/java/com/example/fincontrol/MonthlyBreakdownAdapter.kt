package com.example.fincontrol

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.model.DetalheMes
import com.example.fincontrol.util.FinanceCalculator

/**
 * Adaptador para a tabela de detalhamento mensal do Simulador.
 */
class MonthlyBreakdownAdapter(private var itens: List<DetalheMes>) :
    RecyclerView.Adapter<MonthlyBreakdownAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonth: TextView = view.findViewById(R.id.tv_row_month)
        val tvExpense: TextView = view.findViewById(R.id.tv_row_expense)
        val tvInstallment: TextView = view.findViewById(R.id.tv_row_installment)
        val tvTotal: TextView = view.findViewById(R.id.tv_row_total)
        val tvStatus: TextView = view.findViewById(R.id.tv_row_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_breakdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.tvMonth.text = item.nomeMes
        holder.tvExpense.text = FinanceCalculator.formatarMoeda(item.gastoRegistrado)
        holder.tvInstallment.text = if (item.parcelaSimulada > 0) {
            FinanceCalculator.formatarMoeda(item.parcelaSimulada)
        } else {
            "—"
        }
        holder.tvTotal.text = FinanceCalculator.formatarMoeda(item.totalProjetado)

        if (item.estaAlerta) {
            holder.tvTotal.setTextColor(Color.parseColor("#EA580C"))
            holder.tvStatus.text = "ALERTA"
            holder.tvStatus.setTextColor(Color.parseColor("#C2410C"))

            val badgeBg = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(Color.parseColor("#FFEDD5"))
            }
            holder.tvStatus.background = badgeBg
        } else {
            holder.tvTotal.setTextColor(Color.parseColor("#17211D"))
            holder.tvStatus.text = "OK"
            holder.tvStatus.setTextColor(Color.parseColor("#047857"))

            val badgeBg = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(Color.parseColor("#D1FAE5"))
            }
            holder.tvStatus.background = badgeBg
        }
    }

    override fun getItemCount() = itens.size

    fun atualizarLista(novaLista: List<DetalheMes>) {
        this.itens = novaLista
        notifyDataSetChanged()
    }
}
