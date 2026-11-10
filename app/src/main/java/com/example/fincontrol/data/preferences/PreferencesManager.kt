package com.example.fincontrol.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerenciador de Persistência Chave-Valor (SharedPreferences).
 *
 * Responsável por persistir configurações de usuário essenciais:
 * - Salário mensal base (padrão: R$ 2.500,00)
 * - Teto / Budget mensal de gastos (padrão: R$ 1.200,00)
 * - Mês selecionado no cabeçalho (padrão: 0 = Janeiro)
 *
 * Garante que alterações nas configurações sobrevivam ao fechamento do aplicativo.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "fincontrol_prefs"
        private const val KEY_SALARIO = "pref_salario"
        private const val KEY_BUDGET = "pref_budget"
        private const val KEY_MES_ATUAL = "pref_mes_atual"
        private const val KEY_DARK_MODE = "pref_dark_mode"

        // Valores padrão definidos no protótipo web original
        const val DEFAULT_SALARIO = 2500.00
        const val DEFAULT_BUDGET = 1200.00
        const val DEFAULT_MES = 0 // 0 = Jan
        const val DEFAULT_DARK_MODE = false
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    var salario: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_SALARIO, java.lang.Double.doubleToRawLongBits(DEFAULT_SALARIO))
        )
        set(value) {
            prefs.edit().putLong(KEY_SALARIO, java.lang.Double.doubleToRawLongBits(value)).apply()
        }

    var budget: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_BUDGET, java.lang.Double.doubleToRawLongBits(DEFAULT_BUDGET))
        )
        set(value) {
            prefs.edit().putLong(KEY_BUDGET, java.lang.Double.doubleToRawLongBits(value)).apply()
        }

    var mesAtual: Int
        get() = prefs.getInt(KEY_MES_ATUAL, DEFAULT_MES)
        set(value) {
            prefs.edit().putInt(KEY_MES_ATUAL, value).apply()
        }
}
