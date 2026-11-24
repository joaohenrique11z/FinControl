package com.example.fincontrol.data.model

/**
 * Modelo de dados representando um lançamento financeiro (Gasto ou Entrada/Receita).
 *
 * Mapeia fielmente os campos utilizados na aplicação:
 * - id: Identificador único da transação
 * - descricao: Nome/descrição do lançamento (ex: "Aluguel", "Freelance")
 * - categoria: Categoria ("Moradia", "Alimentação", "Salário", "Freelance", etc.)
 * - valor: Valor monetário em reais (ex: 700.00)
 * - mes: Índice do mês do lançamento (0 = Jan ... 11 = Dez)
 * - dataCriacao: Timestamp da data de registro
 * - tipo: Natureza do lançamento ("DESPESA" ou "ENTRADA")
 */
data class Transacao(
    val id: Long = 0,
    val descricao: String,
    val categoria: String,
    val valor: Double,
    val mes: Int,
    val dataCriacao: Long = System.currentTimeMillis(),
    val tipo: String = TIPO_DESPESA
) {
    companion object {
        const val TIPO_DESPESA = "DESPESA"
        const val TIPO_ENTRADA = "ENTRADA"
    }

    val isEntrada: Boolean get() = tipo.equals(TIPO_ENTRADA, ignoreCase = true)
}
