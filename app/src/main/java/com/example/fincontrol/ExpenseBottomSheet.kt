package com.example.fincontrol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.chip.ChipGroup

class ExpenseBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val expenseValue = view.findViewById<TextInputEditText>(R.id.expense_value)
        val expenseDescription = view.findViewById<TextInputEditText>(R.id.expense_description)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save_expense)
        
        btnSave.setOnClickListener {
            val value = expenseValue.text.toString()
            val desc = expenseDescription.text.toString()
            if (value.isNotEmpty() && desc.isNotEmpty()) {
                Toast.makeText(context, "Lançamento salvo!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Preencha os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}