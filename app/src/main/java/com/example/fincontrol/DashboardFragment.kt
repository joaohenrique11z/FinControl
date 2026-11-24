package com.example.fincontrol

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.example.fincontrol.util.StatementExporter

/**
 * Fragmento da Tela Inicial (Dashboard).
 *
 * Apresenta a visão consolidada das finanças do usuário no mês selecionado:
 * - Saldo livre atual (Entradas consolidadas menos gastos do mês)
 * - Entradas (Salário + Receitas extras) e saídas consolidadas
 * - Termômetro de consumo do limite do budget mensal
 * - Lista de transações recentes persistidas localmente no banco SQLite
 * - Edição de transações ao clicar em qualquer item
 * - Exportação de extrato em Texto e CSV (Excel)
 * - Menu de Configurações com Dark Mode e ajuste de limites
 */
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var repository: FinControlRepository
    private lateinit var adapter: TransactionAdapter

    private lateinit var tvHeaderMonth: TextView
    private lateinit var tvSaldoLivre: TextView
    private lateinit var tvEntradaSalario: TextView
    private lateinit var tvEntradaSubtitle: TextView
    private lateinit var tvSaidaMes: TextView
    private lateinit var tvBudgetLabel: TextView
    private lateinit var tvBudgetPercent: TextView
    private lateinit var tvBudgetMessage: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var tvTransactionCount: TextView
    private lateinit var btnExportStatement: Button
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var tvEmptyTransactions: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinControlRepository.getInstance(requireContext())

        // Inicialização das referências dos componentes visuais
        tvHeaderMonth = view.findViewById(R.id.tv_header_month)
        tvSaldoLivre = view.findViewById(R.id.tv_saldo_livre)
        tvEntradaSalario = view.findViewById(R.id.tv_entrada_salario)
        tvEntradaSubtitle = view.findViewById(R.id.tv_entrada_subtitle)
        tvSaidaMes = view.findViewById(R.id.tv_saida_mes)
        tvBudgetLabel = view.findViewById(R.id.tv_budget_label)
        tvBudgetPercent = view.findViewById(R.id.tv_budget_percent)
        tvBudgetMessage = view.findViewById(R.id.tv_budget_message)
        progressBudget = view.findViewById(R.id.progress_budget)
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count)
        btnExportStatement = view.findViewById(R.id.btn_export_statement)
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions)
        tvEmptyTransactions = view.findViewById(R.id.tv_empty_transactions)

        // Configuração do RecyclerView com suporte a clique para edição de transação
        adapter = TransactionAdapter(emptyList())
        adapter.onItemClickListener = { transacao ->
            val sheet = EditTransactionBottomSheet.newInstance(transacao.id)
            sheet.onTransactionUpdatedListener = {
                carregarDadosDashboard()
            }
            sheet.show(parentFragmentManager, EditTransactionBottomSheet.TAG)
        }
        rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvRecentTransactions.adapter = adapter

        // Permite trocar o mês clicando no badge superior
        tvHeaderMonth.setOnClickListener {
            mostrarSeletorDeMes()
        }

        // Botão de Exportar Extrato
        btnExportStatement.setOnClickListener {
            mostrarMenuExportacao()
        }

        // Botão de Configurações no cabeçalho
        val btnSettings = view.findViewById<View>(R.id.btn_settings)
        btnSettings?.setOnClickListener {
            abrirConfiguracoes()
        }

        // Clique no card de Budget também abre configurações para ajuste rápido do limite mensal
        val cardBudget = view.findViewById<View>(R.id.card_budget)
        cardBudget?.setOnClickListener {
            abrirConfiguracoes()
        }

        // Carrega dados iniciais da persistência
        carregarDadosDashboard()
    }

    override fun onResume() {
        super.onResume()
        carregarDadosDashboard()
    }

    /**
     * Consulta os dados reais persistidos no SQLite e SharedPreferences
     * e recalcula todos os indicadores visuais do painel.
     */
    fun carregarDadosDashboard() {
        val mesAtual = repository.mesAtual
        val salario = repository.salario
        val budget = repository.budget

        tvHeaderMonth.text = repository.obterNomeMesAtual()

        // Consulta transações reais do SQLite para o mês atual
        val transacoes = repository.listarTransacoesPorMes(mesAtual)
        val totalGastos = repository.calcularGastoDoMes(mesAtual)
        val totalEntradasExtras = repository.calcularTotalEntradasExtras(mesAtual)
        val totalEntradasConsolidadas = repository.calcularTotalEntradasConsolidadas(mesAtual)

        val saldoLivre = FinanceCalculator.calcularSaldoLivre(totalEntradasConsolidadas, totalGastos)
        val percentual = FinanceCalculator.calcularPercentualBudget(totalGastos, budget)
        val diferenca = FinanceCalculator.calcularDiferencaBudget(totalGastos, budget)

        // Atualização dos cards de saldo e entradas/saídas
        tvSaldoLivre.text = FinanceCalculator.formatarMoeda(saldoLivre)
        tvEntradaSalario.text = FinanceCalculator.formatarMoeda(totalEntradasConsolidadas)
        tvSaidaMes.text = FinanceCalculator.formatarMoeda(totalGastos)

        if (totalEntradasExtras > 0.0) {
            tvEntradaSubtitle.text = "Salário + ${FinanceCalculator.formatarMoeda(totalEntradasExtras)} extras"
        } else {
            tvEntradaSubtitle.text = "Salário mensal"
        }

        // Atualização do termômetro do budget
        tvBudgetLabel.text = "Limite mensal ${FinanceCalculator.formatarMoeda(budget)}"
        tvBudgetPercent.text = "$percentual%"
        progressBudget.progress = percentual.coerceAtMost(100)

        if (diferenca > 0) {
            // Acima do budget (alerta vermelho)
            val corAlerta = Color.parseColor("#E11D48")
            tvBudgetPercent.setTextColor(corAlerta)
            tvBudgetMessage.setTextColor(corAlerta)
            tvBudgetMessage.text = "Acima do Budget (+${FinanceCalculator.formatarMoeda(diferenca)})"
            progressBudget.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_budget_over)
        } else {
            // Dentro do budget (folga verde)
            val corSucesso = Color.parseColor("#059669")
            tvBudgetPercent.setTextColor(corSucesso)
            tvBudgetMessage.setTextColor(corSucesso)
            tvBudgetMessage.text = "Dentro do Budget (folga de ${FinanceCalculator.formatarMoeda(-diferenca)})"
            progressBudget.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_budget)
        }

        // Atualização da lista de transações
        adapter.atualizarLista(transacoes)
        val qtd = transacoes.size
        tvTransactionCount.text = "$qtd ${if (qtd == 1) "lançamento" else "lançamentos"}"

        if (transacoes.isEmpty()) {
            rvRecentTransactions.visibility = View.GONE
            tvEmptyTransactions.visibility = View.VISIBLE
        } else {
            rvRecentTransactions.visibility = View.VISIBLE
            tvEmptyTransactions.visibility = View.GONE
        }
    }

    /**
     * Menu modal para selecionar o formato de exportação do extrato financeiro.
     */
    private fun mostrarMenuExportacao() {
        val mesAtual = repository.mesAtual
        val nomeMes = repository.obterNomeMesAtual()
        val transacoesMes = repository.listarTransacoesPorMes(mesAtual)

        val opcoes = arrayOf(
            "📄 Compartilhar Texto do Mês ($nomeMes)",
            "📊 Exportar Planilha CSV do Mês ($nomeMes)",
            "📅 Exportar CSV Completo (Ano Todo)"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Exportar Extrato")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> {
                        // Compartilhar Texto do Mês
                        val texto = StatementExporter.gerarExtratoTexto(
                            nomeMes = nomeMes,
                            salarioBase = repository.salario,
                            totalEntradas = repository.calcularTotalEntradasConsolidadas(mesAtual),
                            totalGastos = repository.calcularGastoDoMes(mesAtual),
                            saldoLivre = FinanceCalculator.calcularSaldoLivre(
                                repository.calcularTotalEntradasConsolidadas(mesAtual),
                                repository.calcularGastoDoMes(mesAtual)
                            ),
                            transacoes = transacoesMes
                        )
                        StatementExporter.compartilharTexto(
                            requireContext(),
                            "Extrato FinControl - $nomeMes",
                            texto
                        )
                    }
                    1 -> {
                        // Exportar CSV do Mês
                        if (transacoesMes.isEmpty()) {
                            Toast.makeText(requireContext(), "Nenhum lançamento no mês para exportar.", Toast.LENGTH_SHORT).show()
                            return@setItems
                        }
                        val file = StatementExporter.gerarArquivoCsv(
                            requireContext(),
                            nomeMes,
                            transacoesMes,
                            FinControlRepository.NOMES_MESES_COMPLETO
                        )
                        StatementExporter.compartilharArquivoCsv(
                            requireContext(),
                            "Extrato CSV - $nomeMes",
                            file
                        )
                    }
                    2 -> {
                        // Exportar CSV de Todos os Meses
                        val todasTransacoes = repository.listarTodasTransacoes()
                        if (todasTransacoes.isEmpty()) {
                            Toast.makeText(requireContext(), "Nenhuma transação cadastrada no aplicativo.", Toast.LENGTH_SHORT).show()
                            return@setItems
                        }
                        val file = StatementExporter.gerarArquivoCsv(
                            requireContext(),
                            "Ano_Completo",
                            todasTransacoes,
                            FinControlRepository.NOMES_MESES_COMPLETO
                        )
                        StatementExporter.compartilharArquivoCsv(
                            requireContext(),
                            "Extrato Anual Completo FinControl (CSV)",
                            file
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Dialog para permitir navegar entre todos os meses do ano (Janeiro a Dezembro).
     */
    private fun mostrarSeletorDeMes() {
        val meses = FinControlRepository.NOMES_MESES_COMPLETO.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Selecione o Mês")
            .setSingleChoiceItems(meses, repository.mesAtual) { dialog, which ->
                repository.mesAtual = which
                carregarDadosDashboard()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Abre a BottomSheet de Configurações para gerenciar Dark Mode, Limite Mensal e Salário.
     */
    private fun abrirConfiguracoes() {
        val sheet = SettingsBottomSheet.newInstance()
        sheet.onSettingsSavedListener = {
            carregarDadosDashboard()
        }
        sheet.show(parentFragmentManager, SettingsBottomSheet.TAG)
    }
}
