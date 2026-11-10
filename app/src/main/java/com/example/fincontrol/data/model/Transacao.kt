package com.example.fincontrol.data.model

/**
 * Modelo de dados representando um lançamento financeiro (Gasto).
 *
 * Mapeia fielmente os campos utilizados no protótipo web original (index.html):
 * - id: Identificador único da transação
 * - descricao: Nome/descrição do gasto (ex: "Aluguel", "Supermercado")
 * - categoria: Categoria do gasto ("Moradia", "Alimentação", "Lazer", "Fixo", "Viagem", "Imprevisto")
 * - valor: Valor monetário em reais (ex: 700.00)
 * - mes: Índice do mês do lançamento (0 = Jan, 1 = Fev, 2 = Mar, 3 = Abr, 4 = Mai, 5 = Jun)
 * - dataCriacao: Timestamp da data de registro
 */
data class Transacao(
    val id: Long = 0,
    val descricao: String,
    val categoria: String,
    val valor: Double,
    val mes: Int,
    val dataCriacao: Long = System.currentTimeMillis()
)
