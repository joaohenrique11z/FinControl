package com.example.fincontrol.data.model

/**
 * Modelo de dados para os cards de dicas financeiras da aba Dicas.
 * Espelha as 5 dicas do protótipo web original.
 */
data class Tip(
    val titulo: String,
    val resumo: String,
    val fonte: String,
    val tempo: String,
    val tag: String,
    val corTagHex: String,
    val icone: String
)
