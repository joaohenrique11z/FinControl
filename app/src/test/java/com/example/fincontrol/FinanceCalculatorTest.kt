package com.example.fincontrol

import com.example.fincontrol.data.api.TipsApiService
import com.example.fincontrol.data.model.Simulacao
import com.example.fincontrol.data.model.Transacao
import com.example.fincontrol.util.FinanceCalculator
import com.example.fincontrol.util.StatementExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes Unitários de Regras de Negócio, Cálculos Financeiros e Exportação.
 */
class FinanceCalculatorTest {

    @Test
    fun testCalcularSaldoLivreJaneiro() {
        val salario = 2500.00
        val gastosJaneiro = 1434.00
        val saldoLivre = FinanceCalculator.calcularSaldoLivre(salario, gastosJaneiro)
        assertEquals(1066.00, saldoLivre, 0.001)
    }

    @Test
    fun testCalcularPercentualBudgetJaneiro() {
        val gastosJaneiro = 1434.00
        val budget = 1200.00
        val percentual = FinanceCalculator.calcularPercentualBudget(gastosJaneiro, budget)
        assertEquals(120, percentual)
    }

    @Test
    fun testCalcularDiferencaBudgetJaneiro() {
        val gastosJaneiro = 1434.00
        val budget = 1200.00
        val diferenca = FinanceCalculator.calcularDiferencaBudget(gastosJaneiro, budget)
        assertEquals(234.00, diferenca, 0.001)
    }

    @Test
    fun testConversaoMoedaTexto() {
        assertEquals(400.00, FinanceCalculator.converterTextoParaValor("400,00")!!, 0.001)
        assertEquals(1250.50, FinanceCalculator.converterTextoParaValor("1.250,50")!!, 0.001)
        assertEquals(50.00, FinanceCalculator.converterTextoParaValor("R$ 50,00")!!, 0.001)
    }

    @Test
    fun testCalculoParcelasSimuladas() {
        val sim = Simulacao(
            item = "Tênis",
            total = 400.00,
            parcelas = 4,
            mesInicial = 1 // Fevereiro
        )

        assertEquals(0.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 0), 0.001)
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 1), 0.001)
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 2), 0.001)
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 3), 0.001)
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 4), 0.001)
        assertEquals(0.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 5), 0.001)
    }

    @Test
    fun testVereditoAprovadoSemSimulacao() {
        val gastos = listOf(1434.0, 345.0, 620.0, 1150.0, 1180.0, 510.0)
        val veredito = FinanceCalculator.calcularVeredito(
            gastos,
            null,
            1200.0,
            listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun")
        )
        assertTrue(veredito.podeComprar)
        assertEquals("Pode comprar! Seu orçamento aguenta.", veredito.titulo)
    }

    @Test
    fun testVereditoApertoQuandoEstouraMes() {
        val gastos = listOf(1434.0, 345.0, 620.0, 1150.0, 1180.0, 510.0)
        val sim = Simulacao("Tênis", 400.0, 4, 1)
        val veredito = FinanceCalculator.calcularVeredito(
            gastos,
            sim,
            1200.0,
            listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun")
        )

        assertFalse(veredito.podeComprar)
        assertTrue(veredito.titulo.contains("Aperto em Abr e Mai"))
        assertEquals(2, veredito.detalhesExcedente.size)
    }

    @Test
    fun testCalculoParcelasSimuladasAte24x() {
        val sim24x = Simulacao(
            item = "Notebook",
            total = 2400.00,
            parcelas = 24,
            mesInicial = 0
        )
        for (mes in 0 until 12) {
            assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim24x, mes, 12), 0.001)
        }
    }

    @Test
    fun testCalculoBudgetAjustado() {
        val novoBudget = 2000.00
        val gastos = 1434.00
        val percentual = FinanceCalculator.calcularPercentualBudget(gastos, novoBudget)
        val diferenca = FinanceCalculator.calcularDiferencaBudget(gastos, novoBudget)

        assertEquals(72, percentual)
        assertEquals(-566.00, diferenca, 0.001)
    }

    @Test
    fun testTransacaoTipoEntradaEDespesa() {
        val entrada = Transacao(
            descricao = "Freelance UI",
            categoria = "Freelance",
            valor = 650.0,
            mes = 0,
            tipo = Transacao.TIPO_ENTRADA
        )
        assertTrue(entrada.isEntrada)

        val despesa = Transacao(
            descricao = "Almoço",
            categoria = "Alimentação",
            valor = 45.0,
            mes = 0,
            tipo = Transacao.TIPO_DESPESA
        )
        assertFalse(despesa.isEntrada)
    }

    @Test
    fun testGeracaoExtratoTexto() {
        val lista = listOf(
            Transacao(1, "Aluguel", "Moradia", 700.0, 0, tipo = Transacao.TIPO_DESPESA),
            Transacao(2, "Freelance", "Freelance", 500.0, 0, tipo = Transacao.TIPO_ENTRADA)
        )
        val texto = StatementExporter.gerarExtratoTexto(
            nomeMes = "Janeiro",
            salarioBase = 2500.0,
            totalEntradas = 3000.0,
            totalGastos = 700.0,
            saldoLivre = 2300.0,
            transacoes = lista
        )

        assertNotNull(texto)
        assertTrue(texto.contains("FINCONTROL - EXTRATO MENSAL"))
        assertTrue(texto.contains("Janeiro"))
        assertTrue(texto.contains("[+] ENTRADA: Freelance"))
        assertTrue(texto.contains("[-] GASTO: Aluguel"))
    }

    @Test
    fun testDicasEducativasFallbackDisponiveis() {
        val dicas = TipsApiService.getDicasEducativas()
        assertTrue(dicas.isNotEmpty())
        assertEquals(3, dicas.size)
        assertTrue(dicas.any { it.titulo.contains("50-30-20") })
    }
}
