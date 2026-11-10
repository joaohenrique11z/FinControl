package com.example.fincontrol

import com.example.fincontrol.data.model.Simulacao
import com.example.fincontrol.util.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes Unitários de Regras de Negócio e Cálculos Financeiros.
 *
 * Valida a exatidão matemática de todos os cálculos do aplicativo,
 * garantindo fidelidade absoluta ao protótipo web original:
 * - Saldo Livre = R$ 1.066,00
 * - Percentual de Budget = 120%
 * - Diferença do Budget = +R$ 234,00
 * - Projeção de parcelas e detecção de aperto orçamentário
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
        // Simulação padrão do protótipo: Tênis R$ 400 em 4x a partir de Fevereiro (índice 1)
        val sim = Simulacao(
            item = "Tênis",
            total = 400.00,
            parcelas = 4,
            mesInicial = 1 // Fevereiro
        )

        // Jan (índice 0): parcela deve ser 0
        assertEquals(0.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 0), 0.001)
        // Fev (índice 1): parcela de R$ 100,00
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 1), 0.001)
        // Mar (índice 2): parcela de R$ 100,00
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 2), 0.001)
        // Abr (índice 3): parcela de R$ 100,00
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 3), 0.001)
        // Mai (índice 4): parcela de R$ 100,00
        assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim, 4), 0.001)
        // Jun (índice 5): parcela deve ser 0 (já foram 4 parcelas em Fev, Mar, Abr, Mai)
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
        // Gastos onde Maio tem 1180 e Abril tem 1150.
        // Com parcela de 100 em Abril (1150 + 100 = 1250 > 1200) e Maio (1180 + 100 = 1280 > 1200)
        val gastos = listOf(1434.0, 345.0, 620.0, 1150.0, 1180.0, 510.0)
        val sim = Simulacao("Tênis", 400.0, 4, 1) // Fev, Mar, Abr, Mai
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
        // Cada parcela deve ser R$ 100,00 para todos os 12 meses do ano
        for (mes in 0 until 12) {
            assertEquals(100.0, FinanceCalculator.calcularParcelaSimuladaNoMes(sim24x, mes, 12), 0.001)
        }
    }

    @Test
    fun testCalculoBudgetAjustado() {
        // Testa se o ajuste de limite mensal funciona com folga e com estouro
        val novoBudget = 2000.00
        val gastos = 1434.00
        val percentual = FinanceCalculator.calcularPercentualBudget(gastos, novoBudget)
        val diferenca = FinanceCalculator.calcularDiferencaBudget(gastos, novoBudget)

        // 1434 / 2000 = 71.7% -> 72%
        assertEquals(72, percentual)
        // Folga de 566 (diferença negativa)
        assertEquals(-566.00, diferenca, 0.001)
    }
}
