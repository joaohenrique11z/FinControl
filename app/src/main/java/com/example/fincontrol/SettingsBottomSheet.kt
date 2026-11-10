package com.example.fincontrol

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

/**
 * BottomSheetDialog para o Menu de Configurações do Aplicativo.
 *
 * Permite ao usuário:
 * 1. Alternar entre Modo Claro e Modo Escuro (Dark Mode).
 * 2. Ajustar e persistir o limite mensal de gastos (Budget).
 * 3. Ajustar e persistir o salário mensal base.
 * 4. Restaurar os dados iniciais do protótipo acadêmico.
 *
 * Todas as alterações são salvas diretamente no SharedPreferences / SQLite
 * através do FinControlRepository.
 */
class SettingsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var repository: FinControlRepository
    var onSettingsSavedListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinControlRepository.getInstance(requireContext())

        val switchDarkMode = view.findViewById<MaterialSwitch>(R.id.switch_dark_mode)
        val tvDarkModeIcon = view.findViewById<TextView>(R.id.tv_dark_mode_icon)
        val etBudget = view.findViewById<TextInputEditText>(R.id.edit_settings_budget)
        val etSalary = view.findViewById<TextInputEditText>(R.id.edit_settings_salary)
        val tvError = view.findViewById<TextView>(R.id.tv_settings_error)
        val btnSave = view.findViewById<Button>(R.id.btn_save_settings)
        val btnReset = view.findViewById<Button>(R.id.btn_reset_defaults)

        // Preenche com os dados atualmente persistidos
        val isDark = repository.isDarkMode
        switchDarkMode.isChecked = isDark
        tvDarkModeIcon.text = if (isDark) "🌙" else "☀️"

        etBudget.setText(String.format(Locale.US, "%.2f", repository.budget))
        etSalary.setText(String.format(Locale.US, "%.2f", repository.salario))

        // Alternância em tempo real do Dark Mode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            tvDarkModeIcon.text = if (isChecked) "🌙" else "☀️"
            repository.isDarkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Salvar alterações de limite e salário
        btnSave.setOnClickListener {
            val budgetRaw = etBudget.text?.toString()?.trim()?.replace(",", ".") ?: ""
            val salaryRaw = etSalary.text?.toString()?.trim()?.replace(",", ".") ?: ""

            val novoBudget = budgetRaw.toDoubleOrNull()
            val novoSalario = salaryRaw.toDoubleOrNull()

            if (novoBudget == null || novoBudget < 0) {
                tvError.text = "Informe um valor de limite mensal válido."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (novoSalario == null || novoSalario < 0) {
                tvError.text = "Informe um valor de salário mensal válido."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Persistência em SharedPreferences
            repository.budget = novoBudget
            repository.salario = novoSalario

            Toast.makeText(requireContext(), "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
            onSettingsSavedListener?.invoke()
            dismiss()
        }

        // Redefinir dados para o estado inicial acadêmico
        btnReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Restaurar Dados")
                .setMessage("Deseja restaurar as transações, salário e limite originais do protótipo?")
                .setPositiveButton("Restaurar") { _, _ ->
                    repository.redefinirDadosPadrao()
                    Toast.makeText(requireContext(), "Dados padrão restaurados!", Toast.LENGTH_SHORT).show()
                    onSettingsSavedListener?.invoke()
                    dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    companion object {
        const val TAG = "SettingsBottomSheet"

        fun newInstance(): SettingsBottomSheet {
            return SettingsBottomSheet()
        }
    }
}
