package com.example.tododemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(private val notes: ArrayList<Note>, private val onItemClick: (position: Int) -> Unit, private val onItemLongClick: (position: Int) -> Boolean) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.noteContent.text = currentNote.content
        holder.itemView.setOnClickListener { // Set OnClickListener
            onItemClick(position)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(position)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
