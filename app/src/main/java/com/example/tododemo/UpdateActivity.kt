package com.example.tododemo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.tododemo.databinding.ActivityUpdateBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar
import android.app.DatePickerDialog // Import DatePickerDialog

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding

    lateinit var btnUpdate: Button
    lateinit var tvUpdate_Title: EditText
    lateinit var tvUpdate_Priority: ChipGroup
    lateinit var btnDelete: Button
    lateinit var tvScheduleDate: TextView
    lateinit var tvScheduleTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnUpdate = findViewById(R.id.update_button)
        tvUpdate_Title = findViewById(R.id.create_title)
        tvUpdate_Priority = findViewById(R.id.create_priority)
        btnDelete = findViewById(R.id.delete_button)
        tvScheduleDate = findViewById(R.id.tvSchedule)
        tvScheduleTime = findViewById(R.id.tvClock)

        // Remove tvCalendar visibility toggling.
        // tvCalendar.visibility = View.GONE

        tvScheduleDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Parse existing date if available
            val existingDate = tvScheduleDate.text.toString().replace("Schedule: ", "").split("/")
            if (existingDate.size == 3) {
                try {
                    calendar.set(existingDate[2].toInt(), existingDate[1].toInt() - 1, existingDate[0].toInt())
                } catch (e: NumberFormatException) {
                    // Handle parsing error, use current date
                }
            }

            val datePickerDialog = DatePickerDialog(
                this, R.style.DatePickerTheme,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val date = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                    tvScheduleDate.text = "Schedule: $date"
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        tvScheduleTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Parse existing time if available
            val existingTime = tvScheduleTime.text.toString().split(":")
            if (existingTime.size == 2) {
                try {
                    calendar.set(Calendar.HOUR_OF_DAY, existingTime[0].toInt())
                    calendar.set(Calendar.MINUTE, existingTime[1].toInt())
                } catch (e: NumberFormatException) {
                    // Handle parsing error, use current time
                }
            }

            val timePickerDialog = TimePickerDialog(
                this, R.style.TimePickerTheme,
                { _, hourOfDay, selectedMinute ->
                    tvScheduleTime.text = "$hourOfDay:$selectedMinute"
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
            )
            timePickerDialog.show()
        }

        // Remove tvCalendar.setOnDateChangeListener
        /*tvCalendar.setOnDateChangeListener(CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
            val date = (dayOfMonth.toString() + "/" + (month + 1) + "/" + year)
            tvScheduleDate.setText("Schedule: " + date)
            tvCalendar.visibility = View.GONE

            val clock = Calendar.getInstance()
            val hour = clock.get(Calendar.HOUR_OF_DAY)
            val minute = clock.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this, R.style.TimePickerTheme,
                { view, hourOfDay, minute ->
                    tvScheduleTime.setText("$hourOfDay:$minute")
                },
                hour, minute, false
            )
            timePickerDialog.show()
        })*/

        val task = intent.getStringExtra("task")
        val priority = intent.getStringExtra("priority")
        val taskId = intent.getIntExtra("id", -1)
        val time = intent.getStringExtra("time")
        var scheduleDate = intent.getStringExtra("scheduleDate")
        var sheduleTime = intent.getStringExtra("scheduleTime")


        tvUpdate_Title.setText(task)
        // tvTime.setText(time) // This line is removed as tv_time is no longer in the layout

        // Safely set tvScheduleDate and tvScheduleTime
        if (!scheduleDate.isNullOrEmpty()) {
            tvScheduleDate.text = scheduleDate
        } else {
            tvScheduleDate.text = "Schedule: " // Default or empty state
        }

        if (!sheduleTime.isNullOrEmpty()) {
            tvScheduleTime.text = sheduleTime
        } else {
            tvScheduleTime.text = "" // Default or empty state
        }

        btnUpdate.setOnClickListener {
            val dbHandler = DataBaseHelper(this, null)

            val selectedChipId = tvUpdate_Priority.checkedChipId
            val selectedChip = findViewById<Chip>(selectedChipId)

            val priority = selectedChip?.text.toString() ?: ""


            val sharedPreferences = getSharedPreferences("tododemo", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)
            val user = Task(
                taskId, tvUpdate_Title.text.toString(), priority, time,
                tvScheduleDate.text.toString(), tvScheduleTime.text.toString(), userId
            )
            dbHandler.updateTask(user)
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
            tvUpdate_Title.text.clear()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            }
            scheduleNotification()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDelete.setOnClickListener {
            val dbHandler = DataBaseHelper(this, null)
            dbHandler.deleteTask(taskId)
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            tvUpdate_Title.text.clear()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    private fun scheduleNotification() {

        val selectedChipId = tvUpdate_Priority.checkedChipId
        val selectedChip = findViewById<Chip>(selectedChipId)

        val title = tvUpdate_Title.text.toString()
        val message = selectedChip?.text.toString() ?: ""

        val extras = PersistableBundle()
        extras.putString(NOTIFICATION_TITLE, title)
        extras.putString(NOTIFICATION_MESSAGE, message)

        val scheduledTimeMillis = calculateScheduledTimeMillis()
        val currentTimeMillis = System.currentTimeMillis()
        val delayMillis = scheduledTimeMillis - currentTimeMillis

        val jobScheduler = applicationContext.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(this, MyJobScheduler::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setMinimumLatency(delayMillis)
            .setExtras(extras)
            .build()

        jobScheduler.schedule(jobInfo)

        AlertDialog.Builder(this)
            .setTitle("ToDo")
            .setMessage("You will be notified on scheduled time.")
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun calculateScheduledTimeMillis(): Long {
        var minute = 0
        var hour = 0
        
        try {
            val timeParts = tvScheduleTime.text.toString().split(":")
            if (timeParts.size == 2) {
                hour = timeParts[0].toInt()
                minute = timeParts[1].toInt()
            }
        } catch (e: NumberFormatException) {
            // Handle parsing error, use default 00:00
        }

        var day = 0
        var month = 0
        var year = 0

        try {
            val dateParts = tvScheduleDate.text.toString().replace("Schedule: ", "").split("/")
            if (dateParts.size == 3) {
                day = dateParts[0].toInt()
                month = dateParts[1].toInt() - 1 // Calendar months start at 0
                year = dateParts[2].toInt()
            }
        } catch (e: NumberFormatException) {
            // Handle parsing error, use current date or 01/01/1970
            val calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
        }

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)

        return calendar.timeInMillis
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "ToDo"
        val message = "Priority"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANEL_ID, name, importance)
        channel.description = message

        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}