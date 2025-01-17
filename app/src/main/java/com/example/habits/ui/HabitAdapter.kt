package com.example.habits.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habits.R
import com.example.habits.data.Habit
import com.example.habits.databinding.ItemHabitBinding

class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onCheckBoxClick: (Habit, Boolean) -> Unit, // Обработчик для чекбокса
    private val onLongClick: (Habit) -> Unit,
    private val onReminderClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = getItem(position) // Получение элемента из списка
        holder.bind(habit)
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: Habit) {
            binding.habitName.text = habit.name
            binding.completionCount.text = "Выполнено раз: ${habit.completionCount} "

            // Напоминание: время
            binding.reminderTime.text = habit.reminderTime?.let { "Напоминание: $it" } ?: "Без напоминания"

            // Напоминание: дни недели
            binding.reminderDays.text = if (!habit.reminderDays.isNullOrEmpty()) {
                "Дни: ${habit.reminderDays.joinToString(", ") { it.name.substring(0, 2) }}"
            } else {
                "Дни: Не выбрано"
            }

            // Устанавливаем фон для выполненной привычки
            val backgroundColor = if (habit.isCompleted) {
                R.color.habit_completed_background // Цвет для выполненной привычки
            } else {
                android.R.color.transparent // Прозрачный фон для невыполненной привычки
            }
            binding.habitContainer.setBackgroundColor(
                binding.root.context.getColor(backgroundColor)
            )

            // Обработчики событий
            binding.deleteButton.setOnClickListener {
                onDeleteClick(habit)
            }

            binding.habitStatus.setOnCheckedChangeListener(null) // Убираем старый слушатель
            binding.habitStatus.isChecked = habit.isCompleted // Устанавливаем текущее состояние

            binding.habitStatus.setOnCheckedChangeListener { _, isChecked ->
                if (binding.habitStatus.isPressed) { // Проверяем, что изменение вызвано пользователем
                    onCheckBoxClick(habit, isChecked)
                }
            }

            binding.reminderButton.setOnClickListener {
                onReminderClick(habit)
            }

            itemView.setOnClickListener {
                onHabitClick(habit)
            }

            itemView.setOnLongClickListener {
                onLongClick(habit)
                true
            }
        }

    }


}
