package com.example.fincontrol

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.model.Simulacao
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.card.MaterialCardView

/**
 * Fragmento do Simulador de Compras Parceladas ("Posso Comprar?").
 *
 * Permite ao usuário projetar o impacto de um novo gasto parcelado sobre
 * o orçamento dos próximos 6 meses através de um gráfico comparativo empilhado
 * e de uma análise preditiva de veredito orçamentário.
 */
class SimulatorFragment : Fragment(R.layout.fragment_simulator) {

    private lateinit var repository: FinControlRepository
    private lateinit var breakdownAdapter: MonthlyBreakdownAdapter

    private lateinit var barChart: BarChart
    private lateinit var etItem: EditText
    private lateinit var etTotal: EditText
    private lateinit var spinnerInstallments: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var tvSimError: TextView
    private lateinit var btnSimulate: Button
    private lateinit var btnClear: Button

    private lateinit var cardVerdict: MaterialCardView
    private lateinit var containerVerdictIcon: FrameLayout
    private lateinit var tvVerdictIcon: TextView
    private lateinit var tvVerdictLabel: TextView
    private lateinit var tvVerdictTitle: TextView
    private lateinit var tvVerdictDetails: TextView

    private lateinit var rvMonthlyBreakdown: RecyclerView

    // Estado da simulação ativa (inicia null conforme o protótipo web)
    private var simulacaoAtiva: Simulacao? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinControlRepository.getInstance(requireContext())

        // Mapeamento dos componentes visuais
        barChart = view.findViewById(R.id.spending_chart)
        etItem = view.findViewById(R.id.et_sim_item)
        etTotal = view.findViewById(R.id.et_sim_total)
        spinnerInstallments = view.findViewById(R.id.spinner_sim_installments)
        spinnerMonth = view.findViewById(R.id.spinner_sim_month)
        tvSimError = view.findViewById(R.id.tv_sim_error)
        btnSimulate = view.findViewById(R.id.btn_simulate)
        btnClear = view.findViewById(R.id.btn_clear_simulation)

        cardVerdict = view.findViewById(R.id.card_verdict)
        containerVerdictIcon = view.findViewById(R.id.container_verdict_icon)
        tvVerdictIcon = view.findViewById(R.id.tv_verdict_icon)
        tvVerdictLabel = view.findViewById(R.id.tv_verdict_label)
        tvVerdictTitle = view.findViewById(R.id.tv_verdict_title)
        tvVerdictDetails = view.findViewById(R.id.tv_verdict_details)

        rvMonthlyBreakdown = view.findViewById(R.id.rv_monthly_breakdown)

        configurarSpinners()
        configurarRecyclerView()
        configurarBotoes()

        // Renderização inicial
        atualizarTudo()
    }

    override fun onResume() {
        super.onResume()
        atualizarTudo()
    }

    private fun configurarSpinners() {
        // Parcelas expandidas: 1x a 24x (default = 4x, índice 3)
        val opcoesParcelas = listOf("1x", "2x", "3x", "4x", "5x", "6x", "10x", "12x", "18x", "24x")
        val adapterParcelas = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opcoesParcelas)
        spinnerInstallments.adapter = adapterParcelas
        spinnerInstallments.setSelection(3) // 4x

        // Todos os 12 meses do ano: Jan a Dez (default = Fev / 1)
        val adapterMeses = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, FinControlRepository.NOMES_MESES_COMPLETO)
        spinnerMonth.adapter = adapterMeses
        spinnerMonth.setSelection(1) // Fevereiro
    }

    private fun configurarRecyclerView() {
        breakdownAdapter = MonthlyBreakdownAdapter(emptyList())
        rvMonthlyBreakdown.layoutManager = LinearLayoutManager(requireContext())
        rvMonthlyBreakdown.adapter = breakdownAdapter
    }

    private fun configurarBotoes() {
        btnSimulate.setOnClickListener {
            executarSimulacao()
        }

        btnClear.setOnClickListener {
            limparSimulacao()
        }
    }

    private fun executarSimulacao() {
        val item = etItem.text.toString().trim()
        val total = FinanceCalculator.converterTextoParaValor(etTotal.text.toString())

        if (item.isEmpty()) {
            tvSimError.text = "Informe o nome do item."
            tvSimError.visibility = View.VISIBLE
            return
        }

        if (total == null || total <= 0.0) {
            tvSimError.text = "Informe um valor maior que zero."
            tvSimError.visibility = View.VISIBLE
            return
        }

        tvSimError.visibility = View.GONE

        val parcelasValores = listOf(1, 2, 3, 4, 5, 6, 10, 12, 18, 24)
        val parcelas = parcelasValores.getOrElse(spinnerInstallments.selectedItemPosition) { 4 }
        val mesInicial = spinnerMonth.selectedItemPosition

        simulacaoAtiva = Simulacao(
            item = item,
            total = total,
            parcelas = parcelas,
            mesInicial = mesInicial
        )

        atualizarTudo()
    }

    private fun limparSimulacao() {
        simulacaoAtiva = null
        etItem.setText("Tênis")
        etTotal.setText("400,00")
        spinnerInstallments.setSelection(3) // 4x
        spinnerMonth.setSelection(1) // Fev
        tvSimError.visibility = View.GONE
        atualizarTudo()
    }

    /**
     * Atualiza o gráfico de projeção, o card de veredito e a tabela de detalhamento.
     */
    private fun atualizarTudo() {
        val gastosReais = repository.calcularGastosDeTodosOsMeses()
        val budget = repository.budget
        val meses = FinControlRepository.NOMES_MESES

        atualizarGrafico(gastosReais, budget, meses)
        atualizarVeredito(gastosReais, budget, meses)
        atualizarTabela(gastosReais, budget, meses)
    }

    /**
     * Configura o gráfico de barras empilhadas com MPAndroidChart,
     * espelhando as cores, a linha do budget e as barras do protótipo web original.
     */
    private fun atualizarGrafico(gastosReais: List<Double>, budget: Double, meses: List<String>) {
        val entries = ArrayList<BarEntry>()

        for (i in meses.indices) {
            val gastoReal = if (i < gastosReais.size) gastosReais[i] else 0.0
            val parcela = FinanceCalculator.calcularParcelaSimuladaNoMes(simulacaoAtiva, i, meses.size)
            // Barra empilhada: gasto real na base, parcela simulada no topo
            entries.add(BarEntry(i.toFloat(), floatArrayOf(gastoReal.toFloat(), parcela.toFloat())))
        }

        val dataSet = BarDataSet(entries, "")
        // Cores padrão: Verde para gasto registrado e Roxo/Lilás para parcela simulada
        dataSet.colors = listOf(
            Color.parseColor("#059669"),
            Color.parseColor("#A5B4FC")
        )
        dataSet.stackLabels = arrayOf("Gasto registrado", "Parcela simulada")
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = Color.parseColor("#4B5563")
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) "R$ ${value.toInt()}" else ""
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setFitBars(true)

        // Eixo X (Meses)
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(meses)
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#6B7280")
        xAxis.textSize = 10f
        xAxis.granularity = 1f

        // Eixo Y Esquerdo
        val leftAxis = barChart.axisLeft
        leftAxis.removeAllLimitLines()

        // Linha tracejada vermelha do limite de budget
        val limitLine = LimitLine(budget.toFloat(), "Budget: ${FinanceCalculator.formatarMoeda(budget)}").apply {
            lineColor = Color.parseColor("#EF4444")
            lineWidth = 1.5f
            enableDashedLine(10f, 10f, 0f)
            textColor = Color.parseColor("#DC2626")
            textSize = 9f
        }
        leftAxis.addLimitLine(limitLine)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 2000f
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#F3F4F6")
        leftAxis.textColor = Color.parseColor("#9CA3AF")
        leftAxis.textSize = 9f

        // Desativa Eixo Y Direito
        barChart.axisRight.isEnabled = false

        barChart.animateY(500)
        barChart.invalidate()
    }

    /**
     * Atualiza o card de veredito preditivo com as cores e textos do protótipo web.
     */
    private fun atualizarVeredito(gastosReais: List<Double>, budget: Double, meses: List<String>) {
        val veredito = FinanceCalculator.calcularVeredito(gastosReais, simulacaoAtiva, budget, meses)

        if (veredito.podeComprar) {
            // Sucesso / Orçamento aguenta
            cardVerdict.setCardBackgroundColor(Color.parseColor("#ECFDF5"))
            cardVerdict.strokeColor = Color.parseColor("#A7F3D0")
            tvVerdictLabel.setTextColor(Color.parseColor("#059669"))
            tvVerdictTitle.setTextColor(Color.parseColor("#064E3B"))
            tvVerdictTitle.text = veredito.titulo
            tvVerdictIcon.text = "✓"
            tvVerdictIcon.setTextColor(Color.parseColor("#065F46"))

            val iconShape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#D1FAE5"))
            }
            containerVerdictIcon.background = iconShape
            tvVerdictDetails.visibility = View.GONE
        } else {
            // Aperto orçamentário
            cardVerdict.setCardBackgroundColor(Color.parseColor("#FFF7ED"))
            cardVerdict.strokeColor = Color.parseColor("#FED7AA")
            tvVerdictLabel.setTextColor(Color.parseColor("#EA580C"))
            tvVerdictTitle.setTextColor(Color.parseColor("#7C2D12"))
            tvVerdictTitle.text = veredito.titulo
            tvVerdictIcon.text = "⚠"
            tvVerdictIcon.setTextColor(Color.parseColor("#C2410C"))

            val iconShape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FFEDD5"))
            }
            containerVerdictIcon.background = iconShape

            val detalhesTexto = veredito.detalhesExcedente.joinToString("\n") {
                "${it.first}: +${FinanceCalculator.formatarMoeda(it.second)} acima do budget"
            }
            tvVerdictDetails.text = detalhesTexto
            tvVerdictDetails.visibility = View.VISIBLE
        }
    }

    /**
     * Atualiza a tabela mensal de detalhamento.
     */
    private fun atualizarTabela(gastosReais: List<Double>, budget: Double, meses: List<String>) {
        val itens = FinanceCalculator.gerarDetalhamentoMeses(gastosReais, simulacaoAtiva, budget, meses)
        breakdownAdapter.atualizarLista(itens)
    }
}
