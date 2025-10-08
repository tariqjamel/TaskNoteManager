package com.example.tododemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tododemo.databinding.BottomSheetFabOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FabOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFabOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetFabOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddTask.setOnClickListener {
            startActivity(Intent(activity, CreateActivity::class.java))
            dismiss()
        }

        binding.btnAddNote.setOnClickListener {
            startActivity(Intent(activity, NoteActivity::class.java))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
