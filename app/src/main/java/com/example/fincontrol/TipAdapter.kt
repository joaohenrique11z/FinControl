package com.example.fincontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.model.Tip

class TipAdapter(private val tips: List<Tip>) :
    RecyclerView.Adapter<TipAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_tip_title)
        val tvDesc: TextView = view.findViewById(R.id.tv_tip_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tip = tips[position]
        holder.tvTitle.text = tip.title
        holder.tvDesc.text = tip.description
    }

    override fun getItemCount() = tips.size
}
