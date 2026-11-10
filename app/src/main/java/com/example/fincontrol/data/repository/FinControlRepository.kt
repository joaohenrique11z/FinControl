package com.example.fincontrol.data.repository

import android.content.Context
import com.example.fincontrol.data.db.DatabaseHelper
import com.example.fincontrol.data.model.Transacao
import com.example.fincontrol.data.preferences.PreferencesManager

/**
 * Repositório Central de Dados (Repository Pattern).
 *
 * Provê uma API limpa e unificada para os Fragments e Atividades da UI,
 * encapsulando tanto o acesso ao banco SQLite (DatabaseHelper) quanto às
 * preferências do usuário (PreferencesManager).
 */
class FinControlRepository private constructor(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private val prefsManager = PreferencesManager(context.applicationContext)

    companion object {
        @Volatile
        private var INSTANCE: FinControlRepository? = null

        fun getInstance(context: Context): FinControlRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinControlRepository(context).also { INSTANCE = it }
            }
        }

        val NOMES_MESES = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
        val NOMES_MESES_COMPLETO = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val CATEGORIAS = listOf(
            "Fixo", "Alimentação", "Lazer", "Viagem", "Imprevisto", "Moradia",
            "Saúde", "Educação", "Transporte", "Investimento", "Presente", "Outros"
        )
    }

    // Configurações do Usuário
    var salario: Double
        get() = prefsManager.salario
        set(value) { prefsManager.salario = value }

    var budget: Double
        get() = prefsManager.budget
        set(value) { prefsManager.budget = value }

    var mesAtual: Int
        get() = prefsManager.mesAtual
        set(value) { prefsManager.mesAtual = value }

    var isDarkMode: Boolean
        get() = prefsManager.isDarkMode
        set(value) { prefsManager.isDarkMode = value }

    fun obterNomeMesAtual(): String {
        val idx = mesAtual.coerceIn(0, NOMES_MESES_COMPLETO.size - 1)
        return NOMES_MESES_COMPLETO[idx]
    }

    // Operações de Banco de Dados
    fun listarTransacoesPorMes(mes: Int = mesAtual): List<Transacao> {
        return dbHelper.listarPorMes(mes)
    }

    fun calcularGastoDoMes(mes: Int = mesAtual): Double {
        return dbHelper.calcularTotalPorMes(mes)
    }

    fun calcularGastosDeTodosOsMeses(): List<Double> {
        return dbHelper.calcularTotaisDosMeses(NOMES_MESES.size)
    }

    /**
     * Registra um gasto (à vista ou parcelado).
     *
     * Se parcelas > 1, divide o valor em centavos entre os meses subsequentes,
     * exatamente como implementado na função salvarGasto() do JavaScript original:
     * - Exemplo: R$ 100,00 em 3x -> R$ 33,34 (1/3), R$ 33,33 (2/3), R$ 33,33 (3/3).
     *
     * Retorna a quantidade de parcelas registradas com sucesso.
     */
    fun registrarGasto(
        descricao: String,
        categoria: String,
        valorTotal: Double,
        parcelas: Int,
        mesInicial: Int
    ): Int {
        val parcelasEfetivas = parcelas.coerceAtLeast(1)
        val valorCentavos = Math.round(valorTotal * 100)
        val parcelaBase = valorCentavos / parcelasEfetivas
        val resto = (valorCentavos % parcelasEfetivas).toInt()

        var registrosSalvos = 0
        for (i in 0 until parcelasEfetivas) {
            val mesAlvo = mesInicial + i
            if (mesAlvo >= NOMES_MESES.size) break // Limite de 6 meses

            val centavos = parcelaBase + if (i < resto) 1 else 0
            val valorParcela = centavos / 100.0
            val descricaoFormatada = if (parcelasEfetivas > 1) {
                "$descricao (${i + 1}/$parcelasEfetivas)"
            } else {
                descricao
            }

            val transacao = Transacao(
                descricao = descricaoFormatada,
                categoria = categoria,
                valor = valorParcela,
                mes = mesAlvo
            )
            dbHelper.inserirTransacao(transacao)
            registrosSalvos++
        }
        return registrosSalvos
    }

    fun obterTransacaoPorId(id: Long): Transacao? {
        return dbHelper.obterTransacaoPorId(id)
    }

    fun atualizarTransacao(transacao: Transacao): Boolean {
        return dbHelper.atualizarTransacao(transacao) > 0
    }

    fun excluirTransacao(id: Long): Boolean {
        return dbHelper.deletarTransacao(id) > 0
    }

    fun redefinirDadosPadrao() {
        dbHelper.redefinirDados()
        prefsManager.salario = PreferencesManager.DEFAULT_SALARIO
        prefsManager.budget = PreferencesManager.DEFAULT_BUDGET
        prefsManager.mesAtual = PreferencesManager.DEFAULT_MES
    }
}
