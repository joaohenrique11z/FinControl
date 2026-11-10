package com.example.fincontrol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

/**
 * BottomSheetDialog para registro de novo gasto financeiro.
 *
 * Implementa a mesma lógica e fluxo do protótipo web original:
 * - Leitura e validação de valor monetário e descrição
 * - Seleção de categoria por Chips
 * - Seleção de parcelamento (1x à vista, 2x, 3x, 4x, 6x)
 * - Seleção de mês inicial
 * - Persistência local imediata através do FinControlRepository (SQLite)
 */
class ExpenseBottomSheet : BottomSheetDialogFragment() {

    private lateinit var repository: FinControlRepository
    var onExpenseSavedListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinControlRepository.getInstance(requireContext())

        val etValue = view.findViewById<TextInputEditText>(R.id.expense_value)
        val etDesc = view.findViewById<TextInputEditText>(R.id.expense_description)
        val chipGroupCategory = view.findViewById<ChipGroup>(R.id.chip_group_category)
        val chipGroupInstallments = view.findViewById<ChipGroup>(R.id.chip_group_installments)
        val spinnerMonth = view.findViewById<Spinner>(R.id.spinner_expense_month)
        val tvError = view.findViewById<TextView>(R.id.tv_expense_error)
        val btnSave = view.findViewById<Button>(R.id.btn_save_expense)

        // Configuração do Spinner de meses
        val adapterMeses = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            FinControlRepository.NOMES_MESES_COMPLETO
        )
        spinnerMonth.adapter = adapterMeses
        spinnerMonth.setSelection(repository.mesAtual)

        btnSave.setOnClickListener {
            val valorTexto = etValue.text?.toString().orEmpty()
            val descricao = etDesc.text?.toString()?.trim().orEmpty()
            val valor = FinanceCalculator.converterTextoParaValor(valorTexto)

            // Validações idênticas ao JavaScript do protótipo
            if (valor == null || valor <= 0.0) {
                tvError.text = "Informe um valor maior que zero."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (descricao.isEmpty()) {
                tvError.text = "Preencha a descrição do gasto."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE

            // Categoria selecionada
            val selectedCatChipId = chipGroupCategory.checkedChipId
            val categoria = if (selectedCatChipId != View.NO_ID) {
                view.findViewById<Chip>(selectedCatChipId).text.toString()
            } else {
                "Fixo"
            }

            // Parcelas selecionadas (opções expandidas)
            val selectedInstallmentChipId = chipGroupInstallments.checkedChipId
            val parcelas = when (selectedInstallmentChipId) {
                R.id.chip_parc_2 -> 2
                R.id.chip_parc_3 -> 3
                R.id.chip_parc_4 -> 4
                R.id.chip_parc_5 -> 5
                R.id.chip_parc_6 -> 6
                R.id.chip_parc_10 -> 10
                R.id.chip_parc_12 -> 12
                R.id.chip_parc_18 -> 18
                R.id.chip_parc_24 -> 24
                else -> 1
            }

            val mesInicial = spinnerMonth.selectedItemPosition

            // Persiste no SQLite dividindo centavos se parcelado
            repository.registrarGasto(
                descricao = descricao,
                categoria = categoria,
                valorTotal = valor,
                parcelas = parcelas,
                mesInicial = mesInicial
            )

            Toast.makeText(requireContext(), "Gasto salvo!", Toast.LENGTH_SHORT).show()
            onExpenseSavedListener?.invoke()
            dismiss()
        }
    }
}
