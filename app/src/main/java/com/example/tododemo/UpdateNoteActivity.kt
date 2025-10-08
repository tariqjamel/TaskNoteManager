package com.example.tododemo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tododemo.databinding.ActivityUpdateNoteBinding

class UpdateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var noteId: Int = -1
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("tododemo", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)

        noteId = intent.getIntExtra("noteId", -1)
        val noteContent = intent.getStringExtra("noteContent")

        if (noteId != -1 && noteContent != null) {
            binding.updateNoteContentEditText.setText(noteContent)
        } else {
            Toast.makeText(this, "Error: Note not found.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.updateNoteButton.setOnClickListener {
            val updatedContent = binding.updateNoteContentEditText.text.toString()
            if (updatedContent.isNotEmpty()) {
                val dbHandler = DataBaseHelper(this, null)
                val updatedNote = Note(noteId, updatedContent, userId)
                dbHandler.updateNote(updatedNote)
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Note content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
