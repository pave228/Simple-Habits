package com.example.habits.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.habits.data.Habit
import com.example.habits.data.HabitRepository
import com.example.habits.ui.MainActivity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.example.habits.ui.HabitsWidget
import android.util.Log
import com.example.habits.R
import java.time.LocalDate
import java.util.Date
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application)
    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> get() = _habits

    init {
        loadHabits()
    }

    // Загрузка привычек из SharedPreferences
    private fun loadHabits() {
        val loadedHabits = repository.loadHabits()
        val updatedHabits = resetHabitsIfNeeded(loadedHabits)
        _habits.value = updatedHabits
        repository.saveHabits(updatedHabits) // Сохраняем обновленные привычки
    }


    // Удаление привычки
    fun removeHabit(habit: Habit) {
        repository.removeHabit(habit)
        loadHabits()

        // Обновляем виджет
        val appWidgetManager = AppWidgetManager.getInstance(getApplication())
        val componentName = ComponentName(getApplication(), HabitsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_habits_list)
        Log.d("WidgetDebug", "notifyAppWidgetViewDataChanged called after deleting habit")
    }

    fun addHabit(habit: Habit) {
        repository.addHabit(habit)
        loadHabits()

        // Обновляем виджет
        val appWidgetManager = AppWidgetManager.getInstance(getApplication())
        val componentName = ComponentName(getApplication(), HabitsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_habits_list)
        Log.d("WidgetDebug", "notifyAppWidgetViewDataChanged called after adding habit")
    }

    fun updateHabit(updatedHabit: Habit) {
        val currentHabits = _habits.value?.toMutableList() ?: mutableListOf()
        val index = currentHabits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            currentHabits[index] = updatedHabit
            repository.saveHabits(currentHabits)
            _habits.value = currentHabits
        }

        // Обновляем виджет
        val appWidgetManager = AppWidgetManager.getInstance(getApplication())
        val componentName = ComponentName(getApplication(), HabitsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_habits_list)
        Log.d("WidgetDebug", "notifyAppWidgetViewDataChanged called after updating habit: ${updatedHabit.name}")
    }
    fun deleteHabit(habit: Habit) {
        val currentHabits = _habits.value?.toMutableList() ?: mutableListOf()
        currentHabits.remove(habit)
        repository.saveHabits(currentHabits)
        _habits.value = currentHabits

        // Обновляем виджет
        val appWidgetManager = AppWidgetManager.getInstance(getApplication())
        val componentName = ComponentName(getApplication(), HabitsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_habits_list)
        Log.d("WidgetDebug", "notifyAppWidgetViewDataChanged called after deleting habit: ${habit.name}")
    }


    private fun updateWidget() {
        val intent = Intent(getApplication<Application>(), HabitsWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val appWidgetManager = AppWidgetManager.getInstance(getApplication())
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(getApplication(), HabitsWidget::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        getApplication<Application>().sendBroadcast(intent)
        Log.d("WidgetDebug", "Widget updated from ViewModel")
    }

    // Сортировка привычек по количеству выполнений
    fun sortHabitsByCompletion() {
        _habits.value = _habits.value?.sortedByDescending { it.completionCount }
    }

    // Сортировка привычек по названию
    fun sortHabitsByName() {
        _habits.value = _habits.value?.sortedBy { it.name }
    }

    private val handler = android.os.Handler()
    private val updateRunnable = Runnable {
        updateWidget()
    }

    private fun scheduleWidgetUpdate() {
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, 500) // Обновляем виджет через 500 мс
    }

    private fun resetHabitsIfNeeded(habits: List<Habit>): List<Habit> {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        return habits.map { habit ->
            val habitDate = habit.lastCompletionDate?.let { date ->
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar
            }

            if (habitDate == null || habitDate.timeInMillis != today.timeInMillis) {
                habit.copy(
                    isCompleted = false,
                    lastCompletionDate = today.time
                )
            } else {
                habit
            }
        }

    }
}
