package com.example.fincontrol.data.api

import com.example.fincontrol.data.model.Tip
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

/**
 * Serviço de Conexão com APIs Públicas e Feeds de Notícias Financeiras em Tempo Real.
 *
 * Conecta-se a:
 * 1. Feeds RSS de Notícias Econômicas (G1 Economia / Google News Economia) para matérias e artigos completos.
 * 2. AwesomeAPI (Economia & Câmbio): cotações atualizadas de Dólar (USD), Euro (EUR) e Bitcoin (BTC).
 * 3. BrasilAPI (Taxas Macroeconômicas): indicadores de Taxa Selic e IPCA.
 *
 * Fornece fallback transparente com links reais para portais de educação financeira.
 */
object TipsApiService {

    private const val URL_AWESOME_API = "https://economia.awesomeapi.com.br/last/USD-BRL,EUR-BRL,BTC-BRL"
    private const val URL_BRASIL_API_TAXAS = "https://brasilapi.com.br/api/taxas/v1"
    private const val TIMEOUT_MS = 6000

    /**
     * Carrega as dicas dinamicamente combinando notícias de economia, cotações e taxas reais
     * com matérias educativas de apoio.
     */
    fun carregarDicasComApi(): List<Tip> {
        val listaDicas = mutableListOf<Tip>()

        // 1. Tenta carregar notícias recentes do feed RSS (G1 Economia / Google News)
        try {
            val noticiasRss = carregarNoticiasRss()
            listaDicas.addAll(noticiasRss)
        } catch (e: Exception) {
            // Se falhar o RSS, prossegue para as cotações
        }

        // 2. Tenta carregar cotações cambiais (AwesomeAPI) com links para dados oficiais
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
                            fonte = "Banco Central do Brasil • Câmbio",
                            tempo = "Agora",
                            tag = "Câmbio",
                            corTagHex = "emerald",
                            icone = "💲",
                            url = "https://www.bcb.gov.br/estabilidadefinanceira/cambio"
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
                            fonte = "Banco Central do Brasil • Câmbio",
                            tempo = "Agora",
                            tag = "Câmbio",
                            corTagHex = "sky",
                            icone = "💶",
                            url = "https://www.bcb.gov.br/estabilidadefinanceira/cambio"
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
                            fonte = "CoinMarketCap • Mercado Cripto",
                            tempo = "Agora",
                            tag = "Investimento",
                            corTagHex = "amber",
                            icone = "₿",
                            url = "https://coinmarketcap.com/currencies/bitcoin/"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Em caso de erro na AwesomeAPI, prossegue
        }

        // 3. Tenta carregar taxas econômicas do Brasil (BrasilAPI) com links para os órgãos oficiais
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
                                fonte = "Banco Central do Brasil • Selic",
                                tempo = "Oficial",
                                tag = "Renda Fixa",
                                corTagHex = "violet",
                                icone = "🏛️",
                                url = "https://www.bcb.gov.br/controleinflacao/taxaselic"
                            )
                        )
                    } else if (nome.equals("IPCA", ignoreCase = true)) {
                        listaDicas.add(
                            Tip(
                                titulo = "Inflação IPCA Acumulada: $valor%",
                                resumo = "Mede a inflação oficial do país. Busque proteger o seu poder de compra com investimentos indexados a IPCA + juros reais.",
                                fonte = "IBGE • Indicadores de Inflação",
                                tempo = "Oficial",
                                tag = "Inflação",
                                corTagHex = "orange",
                                icone = "📊",
                                url = "https://www.ibge.gov.br/indicadores.html"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Prossegue
        }

        // 4. Adiciona as dicas educativas essenciais com matérias reais
        val dicasEducativas = getDicasEducativas()

        if (listaDicas.isEmpty()) {
            return dicasEducativas
        }

        listaDicas.addAll(dicasEducativas)
        return listaDicas
    }

    /**
     * Dicas de educação orçamentária e planejamento pessoal com links diretos para páginas de conteúdo.
     */
    fun getDicasEducativas(): List<Tip> {
        return listOf(
            Tip(
                titulo = "Regra 50-30-20: como dividir seu salário",
                resumo = "Organize sua renda entre necessidades (50%), desejos pessoais (30%) e planos para o futuro/investimentos (20%).",
                fonte = "Banco Central do Brasil",
                tempo = "Guia Oficial",
                tag = "Planejamento",
                corTagHex = "emerald",
                icone = "◒",
                url = "https://www.bcb.gov.br/cidadaniafinanceira/duvidas_planejamento"
            ),
            Tip(
                titulo = "Cuidado com o parcelamento sem juros",
                resumo = "Parcelas pequenas comprometem o orçamento dos meses seguintes e reduzem sua margem de segurança financeira.",
                fonte = "Banco Central do Brasil",
                tempo = "Alerta",
                tag = "Orçamento",
                corTagHex = "orange",
                icone = "⌁",
                url = "https://www.bcb.gov.br/cidadaniafinanceira/duvidas_credito"
            ),
            Tip(
                titulo = "Como montar sua reserva de emergência",
                resumo = "Comece guardando o equivalente a 3 a 6 meses de gastos essenciais em aplicações com liquidez diária.",
                fonte = "Tesouro Direto",
                tempo = "Essencial",
                tag = "Segurança",
                corTagHex = "sky",
                icone = "◈",
                url = "https://www.tesourodireto.com.br/aprenda.htm"
            ),
            Tip(
                titulo = "Introdução aos Investimentos e Renda Fixa",
                resumo = "Entenda a diferença entre Renda Fixa e Renda Variável para construir uma carteira de investimentos equilibrada.",
                fonte = "B3 Educação",
                tempo = "Guia Básico",
                tag = "Investimento",
                corTagHex = "violet",
                icone = "📈",
                url = "https://www.b3.com.br/pt_br/educacao/"
            )
        )
    }

    private fun carregarNoticiasRss(): List<Tip> {
        val noticias = mutableListOf<Tip>()
        val rssUrls = listOf(
            "https://g1.globo.com/rss/g1/economia/",
            "https://news.google.com/rss/search?q=economia+financas&hl=pt-BR&gl=BR&ceid=BR:pt-419"
        )

        for (rssUrl in rssUrls) {
            try {
                val xmlData = fazerRequisicaoHttp(rssUrl)
                if (xmlData.isNotEmpty()) {
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = false
                    val parser = factory.newPullParser()
                    parser.setInput(StringReader(xmlData))

                    var eventType = parser.eventType
                    var inItem = false
                    var title = ""
                    var link = ""
                    var description = ""

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        val tagName = parser.name
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                if (tagName.equals("item", ignoreCase = true)) {
                                    inItem = true
                                    title = ""
                                    link = ""
                                    description = ""
                                } else if (inItem) {
                                    when (tagName.lowercase(Locale.ROOT)) {
                                        "title" -> title = parser.nextText().trim()
                                        "link" -> link = parser.nextText().trim()
                                        "description" -> {
                                            val rawDesc = parser.nextText()
                                            description = rawDesc.replace(Regex("<[^>]*>"), "").trim()
                                        }
                                    }
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                if (tagName.equals("item", ignoreCase = true) && inItem) {
                                    inItem = false
                                    if (title.isNotEmpty() && link.isNotEmpty() &&
                                        (link.startsWith("http://", ignoreCase = true) || link.startsWith("https://", ignoreCase = true))
                                    ) {
                                        val resumoLimpo = if (description.length > 140) {
                                            description.take(137) + "..."
                                        } else if (description.isNotEmpty()) {
                                            description
                                        } else {
                                            "Acesse a matéria completa para ler todos os detalhes desta notícia de economia."
                                        }

                                        val fonteMateria = if (rssUrl.contains("g1")) "G1 Economia" else "Google News Economia"

                                        noticias.add(
                                            Tip(
                                                titulo = title,
                                                resumo = resumoLimpo,
                                                fonte = fonteMateria,
                                                tempo = "Notícia Recente",
                                                tag = "Notícia",
                                                corTagHex = "sky",
                                                icone = "📰",
                                                url = link
                                            )
                                        )
                                        if (noticias.size >= 5) break
                                    }
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                }
            } catch (e: Exception) {
                // Tenta a próxima fonte RSS caso ocorra erro de conexão ou parse
            }
            if (noticias.isNotEmpty()) break
        }
        return noticias
    }

    private fun fazerRequisicaoHttp(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "GET"
            conn.connectTimeout = TIMEOUT_MS
            conn.readTimeout = TIMEOUT_MS
            conn.setRequestProperty("User-Agent", "FinControl-Android/1.0")
            conn.setRequestProperty("Accept", "application/json, application/xml, text/xml")

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
