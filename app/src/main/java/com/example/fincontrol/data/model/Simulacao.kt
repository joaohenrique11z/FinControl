package com.example.fincontrol.data.model

/**
 * Dados de uma simulação de compra parcelada ("Posso Comprar?").
 */
data class Simulacao(
    val item: String,
    val total: Double,
    val parcelas: Int,
    val mesInicial: Int
)

/**
 * Resultado do cálculo de impacto orçamentário da simulação.
 * - podeComprar: true se o orçamento aguenta todos os meses; false se estoura o budget
 * - titulo: Mensagem principal exibida no card de veredito
 * - detalhesExcedente: Lista de pares (Nome do Mês, Valor Excedente) caso haja aperto
 */
data class Veredito(
    val podeComprar: Boolean,
    val titulo: String,
    val detalhesExcedente: List<Pair<String, Double>> = emptyList()
)

/**
 * Detalhamento consolidado de um mês específico para a tabela comparativa.
 */
data class DetalheMes(
    val nomeMes: String,
    val mesIndice: Int,
    val gastoRegistrado: Double,
    val parcelaSimulada: Double,
    val totalProjetado: Double,
    val limiteBudget: Double,
    val estaAlerta: Boolean
)
