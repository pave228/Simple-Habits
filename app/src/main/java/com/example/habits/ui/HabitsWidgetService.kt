package com.example.habits.ui

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.habits.R
import com.example.habits.data.HabitRepository
import android.util.Log

class HabitsWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HabitsRemoteViewsFactory(this.applicationContext)
    }
}

class HabitsRemoteViewsFactory(private val context: android.content.Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val habitRepository = HabitRepository(context)
    private var habits = listOf<com.example.habits.data.Habit>()

    override fun onCreate() {
        loadData()
    }

    override fun onDestroy() {}

    override fun getCount(): Int = habits.size

    override fun getViewAt(position: Int): RemoteViews {
        val habit = habits[position]
        val views = RemoteViews(context.packageName, R.layout.widget_habit_item)
        views.setTextViewText(R.id.habit_name, habit.name)
        views.setTextViewText(
            R.id.habit_status,
            if (habit.isCompleted) "Выполнено" else "Не выполнено"
        )
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDataSetChanged() {
        Log.d("WidgetDebug", "onDataSetChanged called")
        loadData()
    }

    private fun loadData() {
        habits = habitRepository.loadHabits().filter { it.reminderDays.isNotEmpty() }
        Log.d("WidgetDebug", "Habits loaded in loadData: ${habits.size}")
        habits.forEach { habit ->
            Log.d("WidgetDebug", "Loaded habit: ${habit.name}, Reminder days: ${habit.reminderDays}, Completed: ${habit.isCompleted}")
        }
    }


}
