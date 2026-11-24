package com.example.fincontrol.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.fincontrol.data.model.Transacao
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitário responsável pela geração e compartilhamento de Extratos Financeiros.
 *
 * Suporta dois formatos profissionais:
 * 1. Texto formatado legível (pronto para envio no WhatsApp, Telegram, E-mail ou Bloco de Notas).
 * 2. Arquivo estruturado CSV (compatível com Microsoft Excel, Google Planilhas e LibreOffice).
 */
object StatementExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private val dateFileFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale("pt", "BR"))

    /**
     * Gera um resumo textual detalhado das finanças de um mês específico.
     */
    fun gerarExtratoTexto(
        nomeMes: String,
        salarioBase: Double,
        totalEntradas: Double,
        totalGastos: Double,
        saldoLivre: Double,
        transacoes: List<Transacao>
    ): String {
        val dataGeracao = dateFormat.format(Date())
        val sb = StringBuilder()

        sb.appendLine("==========================================")
        sb.appendLine("          FINCONTROL - EXTRATO MENSAL     ")
        sb.appendLine("==========================================")
        sb.appendLine("Mês de Referência: $nomeMes")
        sb.appendLine("Gerado em: $dataGeracao")
        sb.appendLine("------------------------------------------")
        sb.appendLine("RESUMO ORÇAMENTÁRIO:")
        sb.appendLine("• Salário Base: ${FinanceCalculator.formatarMoeda(salarioBase)}")
        sb.appendLine("• Total Entradas (Salário + Extras): ${FinanceCalculator.formatarMoeda(totalEntradas)}")
        sb.appendLine("• Total de Saídas (Gastos): ${FinanceCalculator.formatarMoeda(totalGastos)}")
        sb.appendLine("• Saldo Livre Restante: ${FinanceCalculator.formatarMoeda(saldoLivre)}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("LANÇAMENTOS REGISTRADOS (${transacoes.size}):")

        if (transacoes.isEmpty()) {
            sb.appendLine("Nenhum lançamento registrado neste mês.")
        } else {
            for (t in transacoes) {
                val sinal = if (t.isEntrada) "[+] ENTRADA" else "[-] GASTO"
                val valorFormatado = FinanceCalculator.formatarMoeda(t.valor)
                sb.appendLine("$sinal: ${t.descricao} | Cat: ${t.categoria} | $valorFormatado")
            }
        }

        sb.appendLine("==========================================")
        sb.appendLine("FinControl Mobile — Gestão Financeira Pessoal")
        return sb.toString()
    }

    /**
     * Gera um arquivo CSV delimitado por ponto e vírgula com encoding UTF-8, ideal para o Excel brasileiro.
     */
    fun gerarArquivoCsv(
        context: Context,
        prefixoNome: String,
        transacoes: List<Transacao>,
        nomesMeses: List<String>
    ): File {
        val timestamp = dateFileFormat.format(Date())
        val fileName = "Extrato_FinControl_${prefixoNome}_$timestamp.csv"
        val file = File(context.cacheDir, fileName)

        FileWriter(file, Charsets.UTF_8).use { writer ->
            // Escreve o BOM UTF-8 para o Excel reconhecer acentos automaticamente
            writer.write("\uFEFF")
            writer.write("ID;Data Registro;Mês;Tipo;Categoria;Descrição;Valor (R$)\n")

            for (t in transacoes) {
                val dataCriacaoStr = dateFormat.format(Date(t.dataCriacao))
                val mesNome = nomesMeses.getOrElse(t.mes) { "Mês ${t.mes + 1}" }
                val tipoStr = if (t.isEntrada) "Entrada" else "Despesa"
                val valorFormatado = String.format(Locale.US, "%.2f", t.valor).replace(".", ",")

                val descLimpa = t.descricao.replace(";", ",")
                val catLimpa = t.categoria.replace(";", ",")

                writer.write("${t.id};$dataCriacaoStr;$mesNome;$tipoStr;$catLimpa;$descLimpa;$valorFormatado\n")
            }
        }

        return file
    }

    /**
     * Inicia o compartilhamento nativo do Android com o texto formatado.
     */
    fun compartilharTexto(context: Context, titulo: String, texto: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_TEXT, texto)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar Extrato via")
        context.startActivity(shareIntent)
    }

    /**
     * Inicia o compartilhamento nativo do Android fornecendo o arquivo CSV via FileProvider.
     */
    fun compartilharArquivoCsv(context: Context, titulo: String, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Salvar/Compartilhar Extrato CSV")
        context.startActivity(shareIntent)
    }
}
