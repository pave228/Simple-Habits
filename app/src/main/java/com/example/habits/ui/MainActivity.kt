package com.example.habits.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habits.databinding.ActivityMainBinding
import com.example.habits.data.Habit
import com.example.habits.data.DayOfWeekCompat
import com.example.habits.viewmodel.HabitViewModel
import androidx.appcompat.app.AlertDialog
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.app.TimePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.content.Intent
import android.app.AlarmManager
import android.os.Build
import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import com.example.habits.ui.ReminderReceiver
import android.provider.Settings
import android.util.Log
import com.example.habits.R
import java.util.TimeZone
import com.google.android.material.bottomnavigation.BottomNavigationView

import java.time.DayOfWeek
import android.widget.Button
import android.widget.CheckBox



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val habitViewModel: HabitViewModel by viewModels()
    private lateinit var notificationManager: NotificationManager
    private val habitAdapter = HabitAdapter(
        onHabitClick = { habit -> showHabitDescriptionDialog(habit) },
        onDeleteClick = { habit -> deleteHabit(habit) },
        onCheckBoxClick = { habit, isChecked -> updateHabitStatus(habit, isChecked) },
        onLongClick = { habit -> showEditHabitDialog(habit) },
        onReminderClick = { habit -> showTimePickerDialog(habit) } // Новый обработчик
    )
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkExactAlarmPermission()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Устанавливаем обработчик нажатий
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_stats -> {
                    // Запуск StatisticsActivity
                    showStatisticsScreen()
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = habitAdapter

        // Наблюдаем за изменениями в данных привычек
        habitViewModel.habits.observe(this) { habits ->
            habitAdapter.submitList(habits)
        }

        // Добавление новой привычки
        binding.addButton.setOnClickListener {
            // Показываем диалоговое окно для ввода данных
            showAddHabitDialog()
        }

        // Очистка всех привычек
        binding.clearButton.setOnClickListener {
            val habits = habitViewModel.habits.value.orEmpty()
            habits.forEach { habit ->
                habitViewModel.removeHabit(habit)
            }
        }

        binding.sortByCompletionButton.setOnClickListener {
            habitViewModel.sortHabitsByCompletion()
        }

        binding.sortByNameButton.setOnClickListener {
            habitViewModel.sortHabitsByName()
        }

    }

    private fun showTimePickerDialog(habit: Habit) {
        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            // После выбора времени вызываем метод для установки напоминания
            setReminder(habit, hourOfDay, minute)
        }, 12, 0, true) // По умолчанию время: 12:00
        timePicker.show()
    }

    private fun showHomeScreen() {
        Toast.makeText(this, "Главная страница", Toast.LENGTH_SHORT).show()
    }

    private fun showStatisticsScreen() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun setReminder(habit: Habit, hourOfDay: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("HABIT_NAME", habit.name)
            putExtra("HABIT_DESCRIPTION", habit.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем московский часовой пояс
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Лог для проверки
        Log.d("AlarmManager", "Reminder set for (Moscow Time): ${calendar.time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Напоминание для ${habit.name} установлено на $hourOfDay:$minute (МСК)", Toast.LENGTH_SHORT).show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val nameInput = EditText(this).apply {
            hint = "Название привычки"
            setText(habit.name)
        }
        layout.addView(nameInput)

        val descriptionInput = EditText(this).apply {
            hint = "Описание привычки"
            setText(habit.description)
        }
        layout.addView(descriptionInput)

        val timeButton = Button(this).apply {
            text = habit.reminderTime ?: "Выбрать время"
            setOnClickListener {
                val timePicker = TimePickerDialog(
                    this@MainActivity,
                    { _, hour, minute ->
                        text = String.format("%02d:%02d", hour, minute)
                    },
                    12, 0, true
                )
                timePicker.show()
            }
        }
        layout.addView(timeButton)

        val selectedDays = habit.reminderDays.toMutableSet()
        DayOfWeekCompat.values().forEach { day ->
            val checkBox = CheckBox(this).apply {
                text = day.name
                isChecked = selectedDays.contains(day)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDays.add(day)
                    } else {
                        selectedDays.remove(day)
                    }
                }
            }
            layout.addView(checkBox)
        }

        AlertDialog.Builder(this)
            .setTitle("Редактировать привычку")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val updatedHabit = habit.copy(
                    name = nameInput.text.toString().trim(),
                    description = descriptionInput.text.toString().trim(),
                    reminderTime = if (timeButton.text == "Выбрать время") null else timeButton.text.toString(),
                    reminderDays = selectedDays.toList()
                )
                habitViewModel.updateHabit(updatedHabit)
                scheduleHabitReminder(updatedHabit) // Обновляем напоминания
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "habit_reminder_channel",
                "Напоминания о привычках",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для уведомлений о привычках"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(habit: Habit) {
        val notification = NotificationCompat.Builder(this, "habit_reminder_channel")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Напоминание: ${habit.name}")
            .setContentText("Не забудьте выполнить: ${habit.description}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(habit.id.hashCode(), notification)
    }

    private fun scheduleHabitReminder(habit: Habit) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Сначала отменяем напоминания для дней, которые больше не выбраны
        DayOfWeekCompat.values().forEach { dayOfWeek ->
            if (!habit.reminderDays.contains(dayOfWeek)) {
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    "${habit.id}_${dayOfWeek.value}".hashCode(),
                    Intent(this, ReminderReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                Log.d("AlarmManager", "Cancelled reminder for ${habit.name} on $dayOfWeek")
            }
        }

        // Создаём напоминания для выбранных дней
        habit.reminderDays.forEach { dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_WEEK, dayOfWeek.value)
                val timeParts = habit.reminderTime?.split(":") ?: return
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("HABIT_NAME", habit.name)
                putExtra("HABIT_DESCRIPTION", habit.description)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                "${habit.id}_${dayOfWeek.value}".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("AlarmManager", "Reminder set for ${habit.name} on $dayOfWeek at ${habit.reminderTime}")
        }
    }

    private fun updateHabitStatus(habit: Habit, isCompleted: Boolean) {
        val updatedHabit = habit.copy(
            isCompleted = isCompleted,
            completionCount = if (isCompleted && !habit.isCompleted) {
                habit.completionCount + 1
            } else if (!isCompleted && habit.isCompleted) {
                habit.completionCount - 1
            } else {
                habit.completionCount
            }
        )
        habitViewModel.updateHabit(updatedHabit) // Обновляем данные
    }

    private fun loadHabits(): List<Habit> {
        val sharedPreferences = getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("habits_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList() // Если нет данных, возвращаем пустой список
        }
    }

    private fun saveHabits(habits: List<Habit>) {
        val sharedPreferences = getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(habits)
        editor.putString("habits_list", json)
        editor.apply()
    }

    private fun showHabitDescriptionDialog(habit: Habit) {
        // Создаем диалоговое окно, которое покажет описание привычки
        val dialog = AlertDialog.Builder(this)
            .setTitle(habit.name)
            .setMessage(habit.description)  // Отображаем описание привычки
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }

    fun deleteHabit(habit: Habit) {
        habitViewModel.deleteHabit(habit)
        cancelHabitReminders(habit) // Отмена напоминаний, если это нужно
    }

    private fun cancelHabitReminders(habit: Habit) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        habit.reminderDays.forEach { dayOfWeek ->
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                "${habit.id}_${dayOfWeek.value}".hashCode(),
                Intent(this, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmManager", "Reminder cancelled for ${habit.name} on $dayOfWeek")
        }

    }

    private fun showAddHabitDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val nameInput = EditText(this).apply {
            hint = "Название привычки"
        }
        layout.addView(nameInput)

        val descriptionInput = EditText(this).apply {
            hint = "Описание привычки"
        }
        layout.addView(descriptionInput)

        val timeButton = Button(this).apply {
            text = "Выбрать время"
            setOnClickListener {
                val timePicker = TimePickerDialog(
                    this@MainActivity,
                    { _, hour, minute ->
                        text = String.format("%02d:%02d", hour, minute)
                    },
                    12, 0, true
                )
                timePicker.show()
            }
        }
        layout.addView(timeButton)

        val selectedDays = mutableSetOf<DayOfWeekCompat>()
        DayOfWeekCompat.values().forEach { day ->
            val checkBox = CheckBox(this).apply {
                text = day.name
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDays.add(day)
                    } else {
                        selectedDays.remove(day)
                    }
                }
            }
            layout.addView(checkBox)
        }

        AlertDialog.Builder(this)
            .setTitle("Добавить привычку")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val habitName = nameInput.text.toString().trim()
                val habitDescription = descriptionInput.text.toString().trim()
                val reminderTime = if (timeButton.text == "Выбрать время") null else timeButton.text.toString()

                if (habitName.isNotEmpty() && habitDescription.isNotEmpty()) {
                    val newHabit = Habit(
                        name = habitName,
                        description = habitDescription,
                        reminderTime = reminderTime,
                        reminderDays = selectedDays.toList()
                    )
                    habitViewModel.addHabit(newHabit)
                    scheduleHabitReminder(newHabit)
                } else {
                    Toast.makeText(this, "Введите название и описание привычки", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showSettingsScreen() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

}
