package com.example.fincontrol.util

import com.example.fincontrol.data.model.DetalheMes
import com.example.fincontrol.data.model.Simulacao
import com.example.fincontrol.data.model.Veredito
import java.text.NumberFormat
import java.util.Locale

/**
 * Utilitário central de cálculos financeiros e regras de negócio.
 *
 * Reproduz fielmente a lógica de cálculo contida no script JavaScript do protótipo web:
 * - Formatação monetária padrão Real brasileiro (R$)
 * - Cálculo de saldo livre e termômetro de budget
 * - Projeção de impacto de parcelamento em compras futuras
 * - Geração de veredito ("Posso Comprar?")
 */
object FinanceCalculator {

    private val localePtBr = Locale("pt", "BR")
    private val formatadorMoeda = NumberFormat.getCurrencyInstance(localePtBr)

    /**
     * Formata um valor numérico para o padrão de moeda brasileiro: "R$ 1.066,00".
     */
    fun formatarMoeda(valor: Double): String {
        return formatadorMoeda.format(valor)
    }

    /**
     * Converte um texto de entrada monetária (ex: "400,00" ou "1.250,50") em Double.
     * Retorna null se a string for inválida ou não puder ser convertida.
     */
    fun converterTextoParaValor(texto: String): Double? {
        val limpo = texto.trim()
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
        if (limpo.isEmpty()) return null

        val normalizado = if (limpo.contains(",")) {
            limpo.replace(".", "").replace(",", ".")
        } else {
            limpo
        }

        return normalizado.toDoubleOrNull()
    }

    /**
     * Calcula o Saldo Livre Atual = Salário - Gastos do Mês.
     */
    fun calcularSaldoLivre(salario: Double, totalGastosMes: Double): Double {
        return salario - totalGastosMes
    }

    /**
     * Calcula o percentual de consumo do budget mensal.
     * Exemplo: Gastou 1434 com budget de 1200 -> 120%.
     */
    fun calcularPercentualBudget(totalGastosMes: Double, limiteBudget: Double): Int {
        if (limiteBudget <= 0.0) return 0
        return Math.round((totalGastosMes / limiteBudget) * 100).toInt()
    }

    /**
     * Calcula a diferença entre o gasto do mês e o budget:
     * - Se positivo: excedeu o budget (+R$ 234,00).
     * - Se negativo: folga orçamentária (-R$ 100,00).
     */
    fun calcularDiferencaBudget(totalGastosMes: Double, limiteBudget: Double): Double {
        return totalGastosMes - limiteBudget
    }

    /**
     * Retorna o valor da parcela simulada que incide sobre um mês específico.
     * Se o mês estiver fora do intervalo da simulação, retorna 0.0.
     */
    fun calcularParcelaSimuladaNoMes(simulacao: Simulacao?, mesIndice: Int, totalMeses: Int = 12): Double {
        if (simulacao == null) return 0.0
        val inicio = simulacao.mesInicial
        val fim = inicio + simulacao.parcelas
        if (mesIndice < inicio || mesIndice >= fim || mesIndice >= totalMeses) {
            return 0.0
        }
        return if (simulacao.parcelas > 0) simulacao.total / simulacao.parcelas else 0.0
    }

    /**
     * Gera a lista de detalhamento por mês (gasto registrado + parcela = total vs budget).
     */
    fun gerarDetalhamentoMeses(
        gastosRegistrados: List<Double>,
        simulacao: Simulacao?,
        limiteBudget: Double,
        nomesMeses: List<String>
    ): List<DetalheMes> {
        val lista = mutableListOf<DetalheMes>()
        for (i in nomesMeses.indices) {
            val gasto = if (i < gastosRegistrados.size) gastosRegistrados[i] else 0.0
            val parcela = calcularParcelaSimuladaNoMes(simulacao, i, nomesMeses.size)
            val total = gasto + parcela
            val alerta = total > limiteBudget

            lista.add(
                DetalheMes(
                    nomeMes = nomesMeses[i],
                    mesIndice = i,
                    gastoRegistrado = gasto,
                    parcelaSimulada = parcela,
                    totalProjetado = total,
                    limiteBudget = limiteBudget,
                    estaAlerta = alerta
                )
            )
        }
        return lista
    }

    /**
     * Calcula o Veredito do Simulador:
     *
     * Identifica se a adição da nova parcela faz com que meses que estavam abaixo do budget
     * passem a estourar o limite orçamentário.
     *
     * Retorna a mensagem de aprovação ("Pode comprar!") ou aviso de aperto
     * ("Aperto em Fev e Mar! Considere adiar a compra...").
     */
    fun calcularVeredito(
        gastosRegistrados: List<Double>,
        simulacao: Simulacao?,
        limiteBudget: Double,
        nomesMeses: List<String>
    ): Veredito {
        if (simulacao == null) {
            return Veredito(
                podeComprar = true,
                titulo = "Pode comprar! Seu orçamento aguenta.",
                detalhesExcedente = emptyList()
            )
        }

        val mesesEmAperto = mutableListOf<Pair<String, Double>>()

        for (i in nomesMeses.indices) {
            val gastoOriginal = if (i < gastosRegistrados.size) gastosRegistrados[i] else 0.0
            val parcela = calcularParcelaSimuladaNoMes(simulacao, i, nomesMeses.size)
            val total = gastoOriginal + parcela
            val excedente = total - limiteBudget

            // O aperto é acionado se a compra nova empurrou o mês para além do budget
            // (ou se o mês já estava apertado ou passou a ficar com a parcela)
            if (excedente > 0 && parcela > 0) {
                mesesEmAperto.add(Pair(nomesMeses[i], excedente))
            }
        }

        return if (mesesEmAperto.isNotEmpty()) {
            val listaMesesTexto = mesesEmAperto.joinToString(" e ") { it.first }
            Veredito(
                podeComprar = false,
                titulo = "Aperto em $listaMesesTexto! Considere adiar a compra ou aumentar o número de parcelas.",
                detalhesExcedente = mesesEmAperto
            )
        } else {
            Veredito(
                podeComprar = true,
                titulo = "Pode comprar! Seu orçamento aguenta.",
                detalhesExcedente = emptyList()
            )
        }
    }
}
