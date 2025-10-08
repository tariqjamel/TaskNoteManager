package com.example.tododemo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.tododemo.databinding.ActivityCreateBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    lateinit var create_title : EditText
    lateinit var create_priority : ChipGroup
    lateinit var tvScheduleDate: TextView
    lateinit var tvScheduleTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btn_save = findViewById<Button>(R.id.save_button)
        create_title = findViewById(R.id.create_title)
        create_priority = findViewById(R.id.create_priority)
        val tvTime = findViewById<TextView>(R.id.idTime)
        tvScheduleDate = findViewById<TextView>(R.id.tvSchedule)
        val tvCalendar = findViewById<CalendarView>(R.id.tvCalendar)
        tvScheduleTime = findViewById(R.id.tvClock)
        var imageCalendar = findViewById<ImageView>(R.id.calendarView)

        tvCalendar.visibility = View.GONE
        tvScheduleDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this, R.style.DatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val date = "$dayOfMonth/${month + 1}/$year"
                    tvScheduleDate.text = "Schedule: $date"

                    // ⏰ Now open time picker after picking date
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    val timePicker = TimePickerDialog(
                        this, R.style.TimePickerTheme,
                        { _, hourOfDay, minuteOfHour ->
                            tvScheduleTime.text = "$hourOfDay:$minuteOfHour"
                        }, hour, minute, false
                    )
                    timePicker.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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

        imageCalendar.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this, R.style.DatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val date = "$dayOfMonth/${month + 1}/$year"
                    tvScheduleDate.text = "Schedule: $date"

                    // ⏰ Now open time picker after picking date
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    val timePicker = TimePickerDialog(
                        this, R.style.TimePickerTheme,
                        { _, hourOfDay, minuteOfHour ->
                            tvScheduleTime.text = "$hourOfDay:$minuteOfHour"
                        }, hour, minute, false
                    )
                    timePicker.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }


        tvCalendar.setOnDateChangeListener(CalendarView.OnDateChangeListener
        { view, year, month, dayOfMonth ->
            val date = (dayOfMonth.toString() + "/" + (month + 1) + "/" + year)
            tvScheduleDate.setText("Schedule: "+date )
            tvCalendar.visibility = View.GONE

            val clock = Calendar.getInstance()
            val hour = clock.get(Calendar.HOUR_OF_DAY)
            val minute = clock.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                this, R.style.TimePickerTheme,
                { view, hourOfDay, minute ->
                    tvScheduleTime.setText("$hourOfDay:$minute")
                }, hour, minute, false
            )
            timePicker.show()
        })

        val sdf = SimpleDateFormat("'Date : 'dd-MM-yyyy ' Time : 'HH:mm ")
        val currentTime = sdf.format(Date())
        tvTime.text = "Created on: " + currentTime

        btn_save.setOnClickListener {
            val dbHandler = DataBaseHelper(this, null)

            val selectedChipId = create_priority.checkedChipId
            val selectedChip = findViewById<Chip>(selectedChipId)
            val priority = selectedChip?.text.toString() ?: ""

            val sharedPreferences = getSharedPreferences("tododemo", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)

            val user = Task(
                1, create_title.text.toString(), priority,
                tvTime.text.toString(), tvScheduleDate.text.toString(), tvScheduleTime.text.toString(), userId
            )
            dbHandler.addTask(user)
            Toast.makeText(this, "Task created ", Toast.LENGTH_SHORT).show()
            create_title.text.clear()
            priority
            create_title.requestFocus()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            }
            scheduleNotification()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    private fun scheduleNotification() {
        val selectedChipId = create_priority.checkedChipId
        val selectedChip = findViewById<Chip>(selectedChipId)

        val title = create_title.text.toString()
        val message = selectedChip?.text.toString() ?: ""

        val extras = PersistableBundle()
        extras.putString(NOTIFICATION_TITLE, title)
        extras.putString(NOTIFICATION_MESSAGE, message)

        val scheduledTimeMillis = calculateScheduledTimeMillis()
        val currentTimeMillis = System.currentTimeMillis()
        val delayMillis = scheduledTimeMillis - currentTimeMillis

        val jobScheduler = applicationContext.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
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

    @SuppressLint("SuspiciousIndentation")
    private fun calculateScheduledTimeMillis(): Long {
        val minute = tvScheduleTime.text.toString().split(":")[1].toInt()
        val hour = tvScheduleTime.text.toString().split(":")[0].toInt()
        val selectedDateMillis = binding.tvCalendar.date

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateMillis

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "ToDo"
        val description = "Priority"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANEL_ID, name, importance)
        channel.description = description

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
