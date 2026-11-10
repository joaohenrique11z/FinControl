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
import com.example.fincontrol.data.model.Transacao
import com.example.fincontrol.data.repository.FinControlRepository
import com.example.fincontrol.util.FinanceCalculator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

/**
 * BottomSheetDialog para Edição e Exclusão de uma Transação existente.
 *
 * Permite alterar descrição, valor, categoria e mês do lançamento,
 * persistindo as modificações diretamente no banco SQLite local.
 */
class EditTransactionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var repository: FinControlRepository
    private var transacaoId: Long = 0
    var onTransactionUpdatedListener: (() -> Unit)? = null

    companion object {
        const val TAG = "EditTransactionBottomSheet"
        private const val ARG_TRANSACTION_ID = "arg_transacao_id"

        fun newInstance(id: Long): EditTransactionBottomSheet {
            val fragment = EditTransactionBottomSheet()
            val args = Bundle().apply {
                putLong(ARG_TRANSACTION_ID, id)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transacaoId = arguments?.getLong(ARG_TRANSACTION_ID) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinControlRepository.getInstance(requireContext())
        val transacao = repository.obterTransacaoPorId(transacaoId)

        if (transacao == null) {
            Toast.makeText(requireContext(), "Transação não encontrada.", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        val etValue = view.findViewById<TextInputEditText>(R.id.edit_expense_value)
        val etDesc = view.findViewById<TextInputEditText>(R.id.edit_expense_description)
        val chipGroupCategory = view.findViewById<ChipGroup>(R.id.edit_chip_group_category)
        val spinnerMonth = view.findViewById<Spinner>(R.id.edit_spinner_month)
        val tvError = view.findViewById<TextView>(R.id.tv_edit_expense_error)
        val btnUpdate = view.findViewById<Button>(R.id.btn_update_expense)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete_expense)

        // Preenche campos com dados existentes
        etValue.setText(String.format(java.util.Locale.US, "%.2f", transacao.valor).replace(".", ","))
        etDesc.setText(transacao.descricao)

        // Configuração do Spinner de meses (todos os 12 meses)
        val adapterMeses = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            FinControlRepository.NOMES_MESES_COMPLETO
        )
        spinnerMonth.adapter = adapterMeses
        spinnerMonth.setSelection(transacao.mes.coerceIn(0, FinControlRepository.NOMES_MESES_COMPLETO.size - 1))

        // Seleciona o chip correspondente à categoria
        selecionarChipCategoria(chipGroupCategory, transacao.categoria)

        // Salvar alterações
        btnUpdate.setOnClickListener {
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

            val selectedCatChipId = chipGroupCategory.checkedChipId
            val categoria = if (selectedCatChipId != View.NO_ID) {
                view.findViewById<Chip>(selectedCatChipId).text.toString()
            } else {
                transacao.categoria
            }

            val mes = spinnerMonth.selectedItemPosition

            val transacaoAtualizada = transacao.copy(
                descricao = descricao,
                categoria = categoria,
                valor = valor,
                mes = mes
            )

            val sucesso = repository.atualizarTransacao(transacaoAtualizada)
            if (sucesso) {
                Toast.makeText(requireContext(), "Lançamento atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                onTransactionUpdatedListener?.invoke()
                dismiss()
            } else {
                tvError.text = "Erro ao atualizar registro no banco."
                tvError.visibility = View.VISIBLE
            }
        }

        // Excluir transação
        btnDelete.setOnClickListener {
            val sucesso = repository.excluirTransacao(transacao.id)
            if (sucesso) {
                Toast.makeText(requireContext(), "Lançamento excluído com sucesso!", Toast.LENGTH_SHORT).show()
                onTransactionUpdatedListener?.invoke()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Erro ao excluir lançamento.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selecionarChipCategoria(chipGroup: ChipGroup, categoria: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip != null && chip.text.toString().equals(categoria, ignoreCase = true)) {
                chip.isChecked = true
                return
            }
        }
    }
}
