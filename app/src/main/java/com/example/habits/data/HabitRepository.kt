package com.example.habits.data

import LocalDateAdapter
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.Date

class HabitRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)

    // Сохранение привычек
    fun saveHabits(habits: List<Habit>) {
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder()
            .registerTypeAdapter(DayOfWeekCompat::class.java, DayOfWeekCompatAdapter())
            .registerTypeAdapter(Date::class.java, LocalDateAdapter()) // Регистрация адаптера
            .create()
        val json = gson.toJson(habits)
        editor.putString("habits_list", json)
        editor.apply()
    }

    // Загрузка привычек
    fun loadHabits(): List<Habit> {
        val json = sharedPreferences.getString("habits_list", null)
        return if (json != null) {
            val gson = GsonBuilder()
                .registerTypeAdapter(DayOfWeekCompat::class.java, DayOfWeekCompatAdapter())
                .registerTypeAdapter(Date::class.java, LocalDateAdapter()) // Регистрация адаптера
                .create()
            val type = object : TypeToken<List<Habit>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Добавление привычки
    fun addHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }

    // Удаление привычки
    fun removeHabit(habit: Habit) {
        val habits = loadHabits().toMutableList()
        habits.remove(habit)
        saveHabits(habits)
    }

    // Сортировка привычек по выполнению
    fun getSortedHabits(byCompletion: Boolean = true): List<Habit> {
        val habits = loadHabits()
        return if (byCompletion) {
            habits.sortedByDescending { it.completionCount }
        } else {
            habits.sortedBy { it.name }
        }
    }
}

// Адаптер для работы с DayOfWeekCompat
class DayOfWeekCompatAdapter : JsonSerializer<DayOfWeekCompat>, JsonDeserializer<DayOfWeekCompat> {
    override fun serialize(src: DayOfWeekCompat, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.name) // Сохраняем день недели как строку
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DayOfWeekCompat {
        return DayOfWeekCompat.valueOf(json.asString) // Парсим строку обратно в DayOfWeekCompat
    }
}
