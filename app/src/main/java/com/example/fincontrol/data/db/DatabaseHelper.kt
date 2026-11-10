package com.example.fincontrol.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fincontrol.data.model.Transacao

/**
 * Gerenciador de Banco de Dados Local SQLite (Persistência Nativa Android).
 *
 * Esta classe estende SQLiteOpenHelper e é responsável por criar a tabela de transações,
 * gerenciar versões do esquema e pré-popular os dados iniciais do protótipo web (index.html).
 *
 * Utiliza ContentValues e queries parametrizadas para garantir segurança e robustez,
 * seguindo os padrões acadêmicos de desenvolvimento mobile em Engenharia de Software.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "fincontrol.db"
        const val DATABASE_VERSION = 1

        // Nome da tabela e colunas
        const val TABLE_TRANSACOES = "transacoes"
        const val COL_ID = "id"
        const val COL_DESCRICAO = "descricao"
        const val COL_CATEGORIA = "categoria"
        const val COL_VALOR = "valor"
        const val COL_MES = "mes"
        const val COL_DATA_CRIACAO = "data_criacao"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // DDL: Criação da tabela transacoes
        val sqlCreate = """
            CREATE TABLE $TABLE_TRANSACOES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DESCRICAO TEXT NOT NULL,
                $COL_CATEGORIA TEXT NOT NULL,
                $COL_VALOR REAL NOT NULL,
                $COL_MES INTEGER NOT NULL,
                $COL_DATA_CRIACAO INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlCreate)

        // Seed: Pré-população com os 17 lançamentos idênticos ao protótipo HTML original
        inserirDadosIniciais(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACOES")
        onCreate(db)
    }

    /**
     * Insere os 17 dados iniciais contidos no estado padrão do HTML original.
     */
    private fun inserirDadosIniciais(db: SQLiteDatabase) {
        val dadosIniciais = listOf(
            // Mês 0 - Janeiro (Total: R$ 1.434,00)
            Transacao(0, "Aluguel", "Moradia", 700.0, 0),
            Transacao(0, "Supermercado", "Alimentação", 320.0, 0),
            Transacao(0, "Energia", "Moradia", 180.0, 0),
            Transacao(0, "Cinema", "Lazer", 134.0, 0),
            Transacao(0, "Transporte", "Fixo", 100.0, 0),

            // Mês 1 - Fevereiro (Total: R$ 345,00)
            Transacao(0, "Supermercado", "Alimentação", 245.0, 1),
            Transacao(0, "Internet", "Fixo", 100.0, 1),

            // Mês 2 - Março (Total: R$ 620,00)
            Transacao(0, "Supermercado", "Alimentação", 320.0, 2),
            Transacao(0, "Contas", "Fixo", 300.0, 2),

            // Mês 3 - Abril (Total: R$ 1.150,00)
            Transacao(0, "Reserva da viagem (parte 1)", "Viagem", 600.0, 3),
            Transacao(0, "Supermercado", "Alimentação", 300.0, 3),
            Transacao(0, "Contas", "Fixo", 250.0, 3),

            // Mês 4 - Maio (Total: R$ 1.180,00)
            Transacao(0, "Reserva da viagem (parte 2)", "Viagem", 650.0, 4),
            Transacao(0, "Supermercado", "Alimentação", 300.0, 4),
            Transacao(0, "Contas", "Fixo", 230.0, 4),

            // Mês 5 - Junho (Total: R$ 510,00)
            Transacao(0, "Supermercado", "Alimentação", 310.0, 5),
            Transacao(0, "Contas", "Fixo", 200.0, 5)
        )

        db.beginTransaction()
        try {
            for (item in dadosIniciais) {
                val cv = ContentValues().apply {
                    put(COL_DESCRICAO, item.descricao)
                    put(COL_CATEGORIA, item.categoria)
                    put(COL_VALOR, item.valor)
                    put(COL_MES, item.mes)
                    put(COL_DATA_CRIACAO, item.dataCriacao)
                }
                db.insert(TABLE_TRANSACOES, null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Insere uma nova transação no banco de dados.
     * Retorna o ID gerado pelo SQLite.
     */
    fun inserirTransacao(transacao: Transacao): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_DESCRICAO, transacao.descricao)
            put(COL_CATEGORIA, transacao.categoria)
            put(COL_VALOR, transacao.valor)
            put(COL_MES, transacao.mes)
            put(COL_DATA_CRIACAO, transacao.dataCriacao)
        }
        return db.insert(TABLE_TRANSACOES, null, cv)
    }

    /**
     * Retorna a lista de transações filtradas por mês, ordenadas da mais recente para a mais antiga.
     */
    fun listarPorMes(mes: Int): List<Transacao> {
        val lista = mutableListOf<Transacao>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_TRANSACOES,
            null,
            "$COL_MES = ?",
            arrayOf(mes.toString()),
            null,
            null,
            "$COL_ID DESC"
        )

        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COL_ID)
            val descIdx = it.getColumnIndexOrThrow(COL_DESCRICAO)
            val catIdx = it.getColumnIndexOrThrow(COL_CATEGORIA)
            val valIdx = it.getColumnIndexOrThrow(COL_VALOR)
            val mesIdx = it.getColumnIndexOrThrow(COL_MES)
            val dataIdx = it.getColumnIndexOrThrow(COL_DATA_CRIACAO)

            while (it.moveToNext()) {
                lista.add(
                    Transacao(
                        id = it.getLong(idIdx),
                        descricao = it.getString(descIdx),
                        categoria = it.getString(catIdx),
                        valor = it.getDouble(valIdx),
                        mes = it.getInt(mesIdx),
                        dataCriacao = it.getLong(dataIdx)
                    )
                )
            }
        }
        return lista
    }

    /**
     * Calcula o somatório total de gastos registrados para um mês específico.
     */
    fun calcularTotalPorMes(mes: Int): Double {
        val db = readableDatabase
        var total = 0.0
        val cursor = db.rawQuery(
            "SELECT SUM($COL_VALOR) FROM $TABLE_TRANSACOES WHERE $COL_MES = ?",
            arrayOf(mes.toString())
        )
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                total = it.getDouble(0)
            }
        }
        return total
    }

    /**
     * Retorna o somatório de gastos de todos os meses (índices 0 a 11).
     */
    fun calcularTotaisDosMeses(quantidadeMeses: Int = 12): List<Double> {
        val totais = mutableListOf<Double>()
        for (m in 0 until quantidadeMeses) {
            totais.add(calcularTotalPorMes(m))
        }
        return totais
    }

    /**
     * Busca uma transação específica pelo seu ID.
     */
    fun obterTransacaoPorId(id: Long): Transacao? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACOES,
            null,
            "$COL_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_ID)
                val descIdx = it.getColumnIndexOrThrow(COL_DESCRICAO)
                val catIdx = it.getColumnIndexOrThrow(COL_CATEGORIA)
                val valIdx = it.getColumnIndexOrThrow(COL_VALOR)
                val mesIdx = it.getColumnIndexOrThrow(COL_MES)
                val dataIdx = it.getColumnIndexOrThrow(COL_DATA_CRIACAO)

                return Transacao(
                    id = it.getLong(idIdx),
                    descricao = it.getString(descIdx),
                    categoria = it.getString(catIdx),
                    valor = it.getDouble(valIdx),
                    mes = it.getInt(mesIdx),
                    dataCriacao = it.getLong(dataIdx)
                )
            }
        }
        return null
    }

    /**
     * Atualiza os dados de uma transação existente no banco de dados.
     * Retorna a quantidade de linhas afetadas (deve ser 1 em caso de sucesso).
     */
    fun atualizarTransacao(transacao: Transacao): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_DESCRICAO, transacao.descricao)
            put(COL_CATEGORIA, transacao.categoria)
            put(COL_VALOR, transacao.valor)
            put(COL_MES, transacao.mes)
        }
        return db.update(TABLE_TRANSACOES, cv, "$COL_ID = ?", arrayOf(transacao.id.toString()))
    }

    /**
     * Exclui uma transação pelo seu ID.
     */
    fun deletarTransacao(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRANSACOES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Limpa o banco de dados e repopula os dados iniciais.
     * Útil para testes ou para redefinir o estado da aplicação.
     */
    fun redefinirDados() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_TRANSACOES")
        inserirDadosIniciais(db)
    }
}
