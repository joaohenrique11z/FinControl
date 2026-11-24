package com.example.fincontrol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

/**
 * BottomSheetDialog para registro de novos lançamentos (Gastos e Entradas/Receitas).
 *
 * Suporta:
 * - Alternância entre Saída (Gasto) e Entrada (Receita)
 * - Leitura e validação de valor monetário e descrição
 * - Seleção de categorias expandidas
 * - Parcelamento automático em até 24x para gastos
 * - Seleção de mês inicial de incidência
 * - Persistência imediata no SQLite via FinControlRepository
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

        val tvSheetTitle = view.findViewById<TextView>(R.id.tv_sheet_title)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_type)
        val etValue = view.findViewById<TextInputEditText>(R.id.expense_value)
        val etDesc = view.findViewById<TextInputEditText>(R.id.expense_description)
        val chipGroupCategory = view.findViewById<ChipGroup>(R.id.chip_group_category)
        val containerInstallments = view.findViewById<LinearLayout>(R.id.container_installments)
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

        // Configura alternância entre Saída (Despesa) e Entrada (Receita)
        toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn_toggle_income) {
                    tvSheetTitle.text = "Nova entrada"
                    containerInstallments.visibility = View.GONE
                    btnSave.text = "Salvar entrada"
                    view.findViewById<Chip>(R.id.chip_cat_salario)?.isChecked = true
                } else {
                    tvSheetTitle.text = "Gasto rápido"
                    containerInstallments.visibility = View.VISIBLE
                    btnSave.text = "Salvar gasto"
                    view.findViewById<Chip>(R.id.chip_cat_fixo)?.isChecked = true
                }
            }
        }

        btnSave.setOnClickListener {
            val valorTexto = etValue.text?.toString().orEmpty()
            val descricao = etDesc.text?.toString()?.trim().orEmpty()
            val valor = FinanceCalculator.converterTextoParaValor(valorTexto)

            if (valor == null || valor <= 0.0) {
                tvError.text = "Informe um valor maior que zero."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (descricao.isEmpty()) {
                tvError.text = "Preencha a descrição do lançamento."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE

            // Categoria selecionada
            val selectedCatChipId = chipGroupCategory.checkedChipId
            val categoria = if (selectedCatChipId != View.NO_ID) {
                view.findViewById<Chip>(selectedCatChipId).text.toString()
            } else {
                "Outros"
            }

            val mesInicial = spinnerMonth.selectedItemPosition
            val isEntrada = toggleType.checkedButtonId == R.id.btn_toggle_income

            if (isEntrada) {
                // Registro de entrada (receita pontual/extra)
                repository.registrarEntrada(descricao, categoria, valor, mesInicial)
                Toast.makeText(
                    requireContext(),
                    "Entrada de ${FinanceCalculator.formatarMoeda(valor)} registrada!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Registro de despesa (à vista ou parcelada)
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

                val qtdSalva = repository.registrarGasto(descricao, categoria, valor, parcelas, mesInicial)
                Toast.makeText(
                    requireContext(),
                    if (parcelas > 1) "$qtdSalva parcelas registradas com sucesso!" else "Gasto registrado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            onExpenseSavedListener?.invoke()
            dismiss()
        }
    }
}
