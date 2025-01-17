package com.example.habits.data

import java.time.LocalDate
import java.util.Date
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completionCount: Int = 0,
    val reminderTime: String? = null, // Время как строка
    val reminderDays: List<DayOfWeekCompat> = emptyList(), // Используем DayOfWeekCompat
    val lastCompletionDate: Date? = null
)
