package com.example.tododemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), TaskAdapter.ItemClickListener {
    lateinit var recyclerView: RecyclerView
    var taskIds: ArrayList<Int> = ArrayList()
    var taskNames: ArrayList<String> = ArrayList()
    var taskPriorities: ArrayList<String> = ArrayList()
    var taskTime: ArrayList<String> = ArrayList()
    var taskScheduleDate: ArrayList<String> = ArrayList()
    var taskScheduleTime: ArrayList<String> = ArrayList()
    var notes: ArrayList<Note> = ArrayList()

    lateinit var sharedPreferences: SharedPreferences
    var userId = -1

    // FAB variables
    lateinit var fabMain: ExtendedFloatingActionButton
    lateinit var fabTask: FloatingActionButton
    lateinit var fabNote: FloatingActionButton
    private var isFabExpanded = false

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        sharedPreferences = getSharedPreferences("tododemo", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)

        recyclerView = findViewById(R.id.recycler_view)

        // Initialize FABs
        fabMain = findViewById(R.id.add_main)
        fabTask = findViewById(R.id.add_task)
        fabNote = findViewById(R.id.add_note)

        taskIds = ArrayList()
        taskNames = ArrayList()
        taskPriorities = ArrayList()
        taskTime = ArrayList()
        taskScheduleDate = ArrayList()
        taskScheduleTime = ArrayList()
        notes = ArrayList()

        val dbHandler = DataBaseHelper(this, null)
        val taskCursor = dbHandler.getTask(userId)

        if (taskCursor != null && taskCursor.moveToFirst()) {
            do {
                taskIds.add(taskCursor.getInt(taskCursor.getColumnIndex(DataBaseHelper.TASK_ID)))
                taskNames.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_TITLE)))
                taskPriorities.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_PRIORITY)))
                taskTime.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_TIME)))
                taskScheduleDate.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_SCHEDULE_DATE)))
                taskScheduleTime.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_SCHEDULE_TIME)))
            } while (taskCursor.moveToNext())
            taskCursor.close()
        }

        val noteCursor = dbHandler.getNotes(userId)
        if (noteCursor != null && noteCursor.moveToFirst()) {
            do {
                val noteId = noteCursor.getInt(noteCursor.getColumnIndex(DataBaseHelper.NOTE_ID))
                val content = noteCursor.getString(noteCursor.getColumnIndex(DataBaseHelper.NOTE_CONTENT))
                val noteUserId = noteCursor.getInt(noteCursor.getColumnIndex(DataBaseHelper.USER_ID))
                notes.add(Note(noteId, content, noteUserId))
            } while (noteCursor.moveToNext())
            noteCursor.close()
        }

        var fullName = findViewById<TextView>(R.id.fullName)
        val cursr = dbHandler.getName(userId)
        if (cursr != null && cursr.moveToFirst()) {
            val fName = cursr.getString(cursr.getColumnIndex(DataBaseHelper.FIRST_NAME))
            val lName = cursr.getString(cursr.getColumnIndex(DataBaseHelper.LAST_NAME))
            fullName.text = fName + " " + lName
            cursr.close()
        }

        // Set up FAB click listeners
        fabMain.setOnClickListener {
            toggleFab()
        }

        fabTask.setOnClickListener {
            startActivity(Intent(this, CreateActivity::class.java))
            collapseFab()
        }

        fabNote.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
            collapseFab()
        }

        val taskAdapter = TaskAdapter(taskNames, taskPriorities, taskTime, taskScheduleDate, taskScheduleTime,
            onItemClick = { position ->
                onItemClick(position)
            },
            onItemLongClick = { position ->
                onTaskLongClick(position)
            })

        val noteAdapter = NoteAdapter(notes, { position ->
            onNoteClick(position)
        }) { position ->
            onNoteLongClick(position)
        }

        val concatAdapter = ConcatAdapter(taskAdapter, noteAdapter)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = concatAdapter

        registerForContextMenu(recyclerView)
    }

    private fun toggleFab() {
        if (isFabExpanded) {
            collapseFab()
        } else {
            expandFab()
        }
    }

    private fun expandFab() {
        isFabExpanded = true
        fabMain.shrink()

        // Animation for fabTask
        val taskScaleX = ObjectAnimator.ofFloat(fabTask, "scaleX", 0f, 1f)
        val taskScaleY = ObjectAnimator.ofFloat(fabTask, "scaleY", 0f, 1f)
        val taskAlpha = ObjectAnimator.ofFloat(fabTask, "alpha", 0f, 1f)
        val taskAnimatorSet = AnimatorSet().apply {
            playTogether(taskScaleX, taskScaleY, taskAlpha)
            duration = 300
            startDelay = 50 // Slight delay for staggered effect
        }

        // Animation for fabNote
        val noteScaleX = ObjectAnimator.ofFloat(fabNote, "scaleX", 0f, 1f)
        val noteScaleY = ObjectAnimator.ofFloat(fabNote, "scaleY", 0f, 1f)
        val noteAlpha = ObjectAnimator.ofFloat(fabNote, "alpha", 0f, 1f)
        val noteAnimatorSet = AnimatorSet().apply {
            playTogether(noteScaleX, noteScaleY, noteAlpha)
            duration = 300
        }

        // Show FABs and start animations
        fabTask.visibility = android.view.View.VISIBLE
        fabNote.visibility = android.view.View.VISIBLE
        taskAnimatorSet.start()
        noteAnimatorSet.start()
    }

    private fun collapseFab() {
        isFabExpanded = false

        // Animation for fabTask
        val taskScaleX = ObjectAnimator.ofFloat(fabTask, "scaleX", 1f, 0f)
        val taskScaleY = ObjectAnimator.ofFloat(fabTask, "scaleY", 1f, 0f)
        val taskAlpha = ObjectAnimator.ofFloat(fabTask, "alpha", 1f, 0f)
        val taskAnimatorSet = AnimatorSet().apply {
            playTogether(taskScaleX, taskScaleY, taskAlpha)
            duration = 300
        }

        // Animation for fabNote
        val noteScaleX = ObjectAnimator.ofFloat(fabNote, "scaleX", 1f, 0f)
        val noteScaleY = ObjectAnimator.ofFloat(fabNote, "scaleY", 1f, 0f)
        val noteAlpha = ObjectAnimator.ofFloat(fabNote, "alpha", 1f, 0f)
        val noteAnimatorSet = AnimatorSet().apply {
            playTogether(noteScaleX, noteScaleY, noteAlpha)
            duration = 300
            startDelay = 50 // Slight delay for staggered effect
        }

        // Start animations and hide FABs at the end
        taskAnimatorSet.start()
        noteAnimatorSet.start()
        taskAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                fabTask.visibility = android.view.View.INVISIBLE
            }
        })
        noteAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                fabNote.visibility = android.view.View.INVISIBLE
            }
        })

        fabMain.extend()
    }

    override fun onResume() {
        super.onResume()
        collapseFab()
        refreshDataAndAdapters()
    }

    @SuppressLint("Range")
    private fun refreshDataAndAdapters() {
        taskIds.clear()
        taskNames.clear()
        taskPriorities.clear()
        taskTime.clear()
        taskScheduleDate.clear()
        taskScheduleTime.clear()
        notes.clear()

        val dbHandler = DataBaseHelper(this, null)

        val taskCursor = dbHandler.getTask(userId)
        if (taskCursor != null && taskCursor.moveToFirst()) {
            do {
                taskIds.add(taskCursor.getInt(taskCursor.getColumnIndex(DataBaseHelper.TASK_ID)))
                taskNames.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_TITLE)))
                taskPriorities.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_PRIORITY)))
                taskTime.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_TIME)))
                taskScheduleDate.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_SCHEDULE_DATE)))
                taskScheduleTime.add(taskCursor.getString(taskCursor.getColumnIndex(DataBaseHelper.TASK_SCHEDULE_TIME)))
            } while (taskCursor.moveToNext())
            taskCursor.close()
        }

        val noteCursor = dbHandler.getNotes(userId)
        if (noteCursor != null && noteCursor.moveToFirst()) {
            do {
                val noteId = noteCursor.getInt(noteCursor.getColumnIndex(DataBaseHelper.NOTE_ID))
                val content = noteCursor.getString(noteCursor.getColumnIndex(DataBaseHelper.NOTE_CONTENT))
                val noteUserId = noteCursor.getInt(noteCursor.getColumnIndex(DataBaseHelper.USER_ID))
                notes.add(Note(noteId, content, noteUserId))
            } while (noteCursor.moveToNext())
            noteCursor.close()
        }

        val taskAdapter = TaskAdapter(taskNames, taskPriorities, taskTime, taskScheduleDate, taskScheduleTime,
            onItemClick = { position ->
                onItemClick(position)
            },
            onItemLongClick = { position ->
                onTaskLongClick(position)
            })

        val noteAdapter = NoteAdapter(notes, { position ->
            onNoteClick(position)
        }) { position ->
            onNoteLongClick(position)
        }

        val concatAdapter = ConcatAdapter(taskAdapter, noteAdapter)
        recyclerView.adapter = concatAdapter
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, UpdateActivity::class.java)
        intent.putExtra("id", taskIds[position])
        intent.putExtra("task", taskNames[position])
        intent.putExtra("priority", taskPriorities[position])
        intent.putExtra("time", taskTime[position])
        intent.putExtra("scheduleDate", taskScheduleDate[position])
        intent.putExtra("scheduleTime", taskScheduleTime[position])
        startActivity(intent)
    }

    override fun onItemLongClick(position: Int): Boolean {
        return onTaskLongClick(position)
    }

    fun onTaskLongClick(position: Int): Boolean {
        val taskId = taskIds[position]

        val showMenu = androidx.appcompat.widget.PopupMenu(this, recyclerView.getChildAt(position))
        showMenu.inflate(R.menu.contxt_menu)
        showMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemDelete -> {
                    val dbHandler = DataBaseHelper(this, null)
                    dbHandler.deleteTask(taskId)
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()

                    taskIds.removeAt(position)
                    taskNames.removeAt(position)
                    taskPriorities.removeAt(position)
                    taskTime.removeAt(position)
                    taskScheduleDate.removeAt(position)
                    taskScheduleTime.removeAt(position)

                    recyclerView.adapter?.notifyItemRemoved(position)
                    true
                }
                R.id.itemInfo -> {
                    val taskTime = taskTime[position]
                    Toast.makeText(this, taskTime, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        showMenu.show()
        return true
    }

    fun onNoteClick(position: Int) {
        val note = notes[position]
        val intent = Intent(this, UpdateNoteActivity::class.java)
        intent.putExtra("noteId", note.noteId)
        intent.putExtra("noteContent", note.content)
        startActivity(intent)
    }

    fun onNoteLongClick(position: Int): Boolean {
        val note = notes[position]
        val noteId = note.noteId
        val noteContent = note.content

        val globalPosition = taskIds.size + position
        val showMenu = androidx.appcompat.widget.PopupMenu(this, recyclerView.getChildAt(globalPosition))
        showMenu.inflate(R.menu.contxt_menu_note)
        showMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.itemDeleteNote -> {
                    val dbHandler = DataBaseHelper(this, null)
                    dbHandler.deleteNote(noteId)
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()

                    notes.removeAt(position)
                    recyclerView.adapter?.notifyItemRemoved(globalPosition)
                    true
                }
                R.id.itemEditNote -> {
                    val intent = Intent(this, UpdateNoteActivity::class.java)
                    intent.putExtra("noteId", noteId)
                    intent.putExtra("noteContent", noteContent)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        showMenu.show()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item3 -> Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show()
            R.id.item4 -> Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show()
            R.id.itemLogOut -> {
                sharedPreferences.edit().remove("userId").apply()

                val intent = Intent(this, loginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}