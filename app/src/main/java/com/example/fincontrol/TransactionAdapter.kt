package com.example.fincontrol

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.model.Transacao
import com.example.fincontrol.util.FinanceCalculator

/**
 * Adaptador de RecyclerView para a lista de transações recentes do mês.
 *
 * Suporta clique em cada item para abrir o modal de edição de lançamentos.
 */
class TransactionAdapter(
    private var transactions: List<Transacao>,
    var onItemClickListener: ((Transacao) -> Unit)? = null
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containerIcon: FrameLayout = view.findViewById(R.id.container_category_icon)
        val tvIcon: TextView = view.findViewById(R.id.tv_category_icon)
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
        val item = transactions[position]
        holder.tvDesc.text = item.descricao
        holder.tvCategory.text = item.categoria
        holder.tvValue.text = "−${FinanceCalculator.formatarMoeda(item.valor)}"

        // Cores e ícones de acordo com a categoria (incluindo novas categorias expandidas)
        val (corBg, corTexto, emoji) = when (item.categoria) {
            "Moradia" -> Triple("#E0F2FE", "#0284C7", "🏠")
            "Alimentação" -> Triple("#FEF3C7", "#D97706", "🍽️")
            "Lazer" -> Triple("#EDE9FE", "#7C3AED", "🍿")
            "Fixo" -> Triple("#DBEAFE", "#2563EB", "📄")
            "Viagem" -> Triple("#E0E7FF", "#4F46E5", "✈️")
            "Imprevisto" -> Triple("#FFEDD5", "#EA580C", "⚠️")
            "Saúde" -> Triple("#FEE2E2", "#DC2626", "🩺")
            "Educação" -> Triple("#FEF9C3", "#CA8A04", "📚")
            "Transporte" -> Triple("#E0E7FF", "#4338CA", "🚗")
            "Investimento" -> Triple("#DCFCE7", "#15803D", "📈")
            "Presente" -> Triple("#FCE7F3", "#BE185D", "🎁")
            "Outros" -> Triple("#F3F4F6", "#4B5563", "📦")
            else -> Triple("#D1FAE5", "#059669", "💰")
        }

        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(corBg))
        }
        holder.containerIcon.background = shape
        holder.tvIcon.text = emoji
        holder.tvIcon.setTextColor(Color.parseColor(corTexto))

        // Clique no item para abrir o modal de edição
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    override fun getItemCount() = transactions.size

    fun atualizarLista(novaLista: List<Transacao>) {
        this.transactions = novaLista
        notifyDataSetChanged()
    }
}
