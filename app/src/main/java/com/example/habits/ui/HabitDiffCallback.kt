package com.example.habits.ui

import androidx.recyclerview.widget.DiffUtil
import com.example.habits.data.Habit

class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
    override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
        // Сравниваем элементы по id, чтобы понять, это одинаковые элементы или нет
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
        // Сравниваем все содержимое объектов
        return oldItem == newItem
    }
}
