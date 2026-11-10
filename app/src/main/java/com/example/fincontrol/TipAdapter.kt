package com.example.fincontrol

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.model.Tip

/**
 * Adaptador para o feed de Dicas Financeiras.
 */
class TipAdapter(private val tips: List<Tip>) :
    RecyclerView.Adapter<TipAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containerIcon: FrameLayout = view.findViewById(R.id.container_tip_icon)
        val tvIcon: TextView = view.findViewById(R.id.tv_tip_icon)
        val tvTag: TextView = view.findViewById(R.id.tv_tip_tag)
        val tvTime: TextView = view.findViewById(R.id.tv_tip_time)
        val tvTitle: TextView = view.findViewById(R.id.tv_tip_title)
        val tvDesc: TextView = view.findViewById(R.id.tv_tip_desc)
        val tvSource: TextView = view.findViewById(R.id.tv_tip_source)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tip = tips[position]
        holder.tvTitle.text = tip.titulo
        holder.tvDesc.text = tip.resumo
        holder.tvSource.text = tip.fonte
        holder.tvTime.text = tip.tempo
        holder.tvTag.text = tip.tag.uppercase()
        holder.tvIcon.text = tip.icone

        val (corBg, corTexto) = when (tip.corTagHex) {
            "emerald" -> Pair("#D1FAE5", "#065F46")
            "orange" -> Pair("#FFEDD5", "#C2410C")
            "sky" -> Pair("#E0F2FE", "#0369A1")
            "violet" -> Pair("#EDE9FE", "#6D28D9")
            "amber" -> Pair("#FEF3C7", "#B45309")
            else -> Pair("#F3F4F6", "#374151")
        }

        // Ícone Background
        val iconShape = GradientDrawable().apply {
            cornerRadius = 24f
            setColor(Color.parseColor(corBg))
        }
        holder.containerIcon.background = iconShape

        // Tag Background
        val tagShape = GradientDrawable().apply {
            cornerRadius = 20f
            setColor(Color.parseColor(corBg))
        }
        holder.tvTag.background = tagShape
        holder.tvTag.setTextColor(Color.parseColor(corTexto))
    }

    override fun getItemCount() = tips.size
}
