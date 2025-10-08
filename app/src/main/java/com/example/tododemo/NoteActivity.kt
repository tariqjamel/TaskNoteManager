package com.example.tododemo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.tododemo.databinding.ActivityNoteBinding // This will be generated once activity_note.xml is created

class NoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("tododemo", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)

        binding.saveNoteButton.setOnClickListener {
            val noteContent = binding.noteContentEditText.text.toString()
            if (noteContent.isNotEmpty()) {
                val dbHandler = DataBaseHelper(this, null)
                dbHandler.addNote(userId, noteContent)
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
