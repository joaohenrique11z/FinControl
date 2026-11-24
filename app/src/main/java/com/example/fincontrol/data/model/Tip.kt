package com.example.fincontrol.data.model

/**
 * Modelo de dados para os cards de dicas financeiras e cotações em tempo real da aba Dicas.
 * Suporta conteúdo estático e dinâmico conectado via APIs externas.
 */
data class Tip(
    val titulo: String,
    val resumo: String,
    val fonte: String,
    val tempo: String,
    val tag: String,
    val corTagHex: String,
    val icone: String,
    val url: String? = null
)
