package com.example.habits.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habits.R
import com.example.habits.viewmodel.HabitViewModel
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {

    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val totalHabitsTextView = findViewById<TextView>(R.id.totalHabitsTextView)

        val completionPercentageTextView = findViewById<TextView>(R.id.completionPercentageTextView)
        val mostCompletedHabitTextView = findViewById<TextView>(R.id.mostCompletedHabitTextView)
        val todayCompletedHabitsTextView = findViewById<TextView>(R.id.todayCompletedHabitsTextView)

        // Наблюдаем за изменениями в привычках
        habitViewModel.habits.observe(this) { habits ->
            val totalHabits = habits.size
            val completedHabits = habits.count { it.isCompleted }

            // Общее количество привычек
            totalHabitsTextView.text = "Всего привычек: $totalHabits"

            // Процент выполнения привычек
            val percentage = if (totalHabits > 0) (completedHabits * 100 / totalHabits) else 0
            completionPercentageTextView.text = "Выполнено: $percentage%"

            // Самая часто выполняемая привычка
            val mostCompletedHabit = habits.maxByOrNull { it.completionCount }
            mostCompletedHabitTextView.text = mostCompletedHabit?.let {
                "Самая популярная привычка: ${it.name}"
            } ?: "Самая популярная привычка: -"

            // Статистика за текущий день
            // Подсчёт привычек, отмеченных как выполненные
            val todayCompletedHabits = habits.count { it.isCompleted }

            todayCompletedHabitsTextView.text = "Сегодня выполнено привычек: $todayCompletedHabits"


        }
    }
}
