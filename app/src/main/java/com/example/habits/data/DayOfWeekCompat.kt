package com.example.habits.data

import java.util.Calendar

enum class DayOfWeekCompat(val value: Int) {
    SUNDAY(Calendar.SUNDAY),
    MONDAY(Calendar.MONDAY),
    TUESDAY(Calendar.TUESDAY),
    WEDNESDAY(Calendar.WEDNESDAY),
    THURSDAY(Calendar.THURSDAY),
    FRIDAY(Calendar.FRIDAY),
    SATURDAY(Calendar.SATURDAY);

    companion object {
        fun fromCalendarValue(value: Int): DayOfWeekCompat? {
            return values().find { it.value == value }
        }
    }
}
