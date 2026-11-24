package com.example.fincontrol.data.api

import com.example.fincontrol.data.model.Tip
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

/**
 * Serviço de Conexão com APIs Públicas de Finanças e Economia em Tempo Real.
 *
 * Conecta-se às APIs gratuitas e oficiais:
 * 1. AwesomeAPI (Economia & Câmbio): cotações atualizadas de Dólar (USD), Euro (EUR) e Bitcoin (BTC).
 * 2. BrasilAPI (Taxas Macroeconômicas): indicadores de Taxa Selic, CDI e IPCA.
 *
 * Fornece fallback transparente com as dicas educativas recomendadas caso não haja conexão à internet.
 */
object TipsApiService {

    private const val URL_AWESOME_API = "https://economia.awesomeapi.com.br/last/USD-BRL,EUR-BRL,BTC-BRL"
    private const val URL_BRASIL_API_TAXAS = "https://brasilapi.com.br/api/taxas/v1"
    private const val TIMEOUT_MS = 6000

    /**
     * Carrega as dicas dinamicamente combinando cotações e taxas reais com dicas educativas de apoio.
     */
    fun carregarDicasComApi(): List<Tip> {
        val listaDicas = mutableListOf<Tip>()

        // 1. Tenta carregar cotações cambiais (AwesomeAPI)
        try {
            val jsonCotas = fazerRequisicaoHttp(URL_AWESOME_API)
            if (jsonCotas.isNotEmpty()) {
                val obj = JSONObject(jsonCotas)

                // Dólar
                if (obj.has("USDBRL")) {
                    val usd = obj.getJSONObject("USDBRL")
                    val bid = usd.optDouble("bid", 0.0)
                    val pct = usd.optString("pctChange", "0.0")
                    val sinalPct = if (!pct.startsWith("-")) "+$pct%" else "$pct%"

                    listaDicas.add(
                        Tip(
                            titulo = "Dólar Comercial: R$ ${String.format(Locale("pt", "BR"), "%.2f", bid)} ($sinalPct)",
                            resumo = "Mercado Cambial: O dólar opera hoje a R$ ${String.format(Locale("pt", "BR"), "%.2f", bid)}. Ideal para planejar compras internacionais ou aportes em reserva cambial.",
                            fonte = "AwesomeAPI • Câmbio em Tempo Real",
                            tempo = "Agora",
                            tag = "Câmbio",
                            corTagHex = "emerald",
                            icone = "💲",
                            url = "https://economia.awesomeapi.com.br"
                        )
                    )
                }

                // Euro
                if (obj.has("EURBRL")) {
                    val eur = obj.getJSONObject("EURBRL")
                    val bid = eur.optDouble("bid", 0.0)
                    val pct = eur.optString("pctChange", "0.0")
                    val sinalPct = if (!pct.startsWith("-")) "+$pct%" else "$pct%"

                    listaDicas.add(
                        Tip(
                            titulo = "Euro: R$ ${String.format(Locale("pt", "BR"), "%.2f", bid)} ($sinalPct)",
                            resumo = "Planejamento de Viagem: Cotação da moeda europeia em tempo real para reservas e remessas.",
                            fonte = "AwesomeAPI • Câmbio Europeu",
                            tempo = "Agora",
                            tag = "Câmbio",
                            corTagHex = "sky",
                            icone = "💶",
                            url = "https://economia.awesomeapi.com.br"
                        )
                    )
                }

                // Bitcoin
                if (obj.has("BTCBRL")) {
                    val btc = obj.getJSONObject("BTCBRL")
                    val bid = btc.optDouble("bid", 0.0)
                    val bidFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(bid)

                    listaDicas.add(
                        Tip(
                            titulo = "Bitcoin (BTC): $bidFormatado",
                            resumo = "Criptoativos: Alta volatilidade no mercado cripto. Mantenha cautela e limite sua exposição a uma parcela prudente do seu patrimônio.",
                            fonte = "Mercado Cripto • Tempo Real",
                            tempo = "Agora",
                            tag = "Investimento",
                            corTagHex = "amber",
                            icone = "₿",
                            url = "https://economia.awesomeapi.com.br"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Em caso de erro na AwesomeAPI, prossegue para as outras fontes
        }

        // 2. Tenta carregar taxas econômicas do Brasil (BrasilAPI)
        try {
            val jsonTaxas = fazerRequisicaoHttp(URL_BRASIL_API_TAXAS)
            if (jsonTaxas.isNotEmpty()) {
                val array = JSONArray(jsonTaxas)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val nome = item.optString("nome", "")
                    val valor = item.optDouble("valor", 0.0)

                    if (nome.equals("Selic", ignoreCase = true)) {
                        listaDicas.add(
                            Tip(
                                titulo = "Taxa Selic Hoje: $valor% ao ano",
                                resumo = "A taxa básica de juros do Banco Central influencia os rendimentos de Renda Fixa (Tesouro Selic e CDBs) e o custo de empréstimos.",
                                fonte = "Banco Central • BrasilAPI",
                                tempo = "Oficial",
                                tag = "Renda Fixa",
                                corTagHex = "violet",
                                icone = "🏛️",
                                url = "https://brasilapi.com.br"
                            )
                        )
                    } else if (nome.equals("IPCA", ignoreCase = true)) {
                        listaDicas.add(
                            Tip(
                                titulo = "Inflação IPCA Acumulada: $valor%",
                                resumo = "Mede a inflação oficial do país. Busque proteger o seu poder de compra com investimentos indexados a IPCA + juros reais.",
                                fonte = "IBGE • BrasilAPI",
                                tempo = "Oficial",
                                tag = "Inflação",
                                corTagHex = "orange",
                                icone = "📊",
                                url = "https://brasilapi.com.br"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Prossegue
        }

        // 3. Adiciona as dicas educativas essenciais do aplicativo
        val dicasEducativas = getDicasEducativas()

        if (listaDicas.isEmpty()) {
            // Se estiver totalmente offline, retorna todas as dicas educativas padrão
            return dicasEducativas
        }

        // Concatena as dicas de APIs em tempo real com as dicas educativas
        listaDicas.addAll(dicasEducativas)
        return listaDicas
    }

    /**
     * Dicas de educação orçamentária e planejamento pessoal (sempre disponíveis offline).
     */
    fun getDicasEducativas(): List<Tip> {
        return listOf(
            Tip(
                titulo = "Regra 50-30-20: como dividir seu salário",
                resumo = "Organize sua renda entre necessidades (50%), desejos pessoais (30%) e planos para o futuro/investimentos (20%).",
                fonte = "Educação Financeira",
                tempo = "Guia Básico",
                tag = "Planejamento",
                corTagHex = "emerald",
                icone = "◒"
            ),
            Tip(
                titulo = "Cuidado com o parcelamento sem juros",
                resumo = "Parcelas pequenas comprometem o orçamento dos meses seguintes e reduzem sua margem de segurança financeira.",
                fonte = "FinControl Dicas",
                tempo = "Alerta",
                tag = "Orçamento",
                corTagHex = "orange",
                icone = "⌁"
            ),
            Tip(
                titulo = "Como montar sua reserva de emergência",
                resumo = "Comece guardando o equivalente a 3 a 6 meses de gastos essenciais em aplicações com liquidez diária.",
                fonte = "Finanças na Prática",
                tempo = "Essencial",
                tag = "Segurança",
                corTagHex = "sky",
                icone = "◈"
            )
        )
    }

    private fun fazerRequisicaoHttp(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.setRequestProperty("User-Agent", "FinControl-Android/1.0")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                response.toString()
            } else {
                ""
            }
        } finally {
            conn.disconnect()
        }
    }
}
