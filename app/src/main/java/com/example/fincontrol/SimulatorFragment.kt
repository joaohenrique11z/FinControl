package com.example.fincontrol

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class SimulatorFragment : Fragment(R.layout.fragment_simulator) {

    private lateinit var barChart: BarChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barChart = view.findViewById(R.id.spending_chart)
        setupChart()
    }

    private fun setupChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 1500f))
        entries.add(BarEntry(2f, 1300f))
        entries.add(BarEntry(3f, 1600f))
        entries.add(BarEntry(4f, 1200f))
        entries.add(BarEntry(5f, 1400f))

        val dataSet = BarDataSet(entries, "Projeção de Gastos (Meses)")
        dataSet.color = Color.parseColor("#059669")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.animateY(1000)
        barChart.invalidate()
    }
}
