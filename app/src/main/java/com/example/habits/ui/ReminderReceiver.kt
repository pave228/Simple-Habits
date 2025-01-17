package com.example.habits.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import com.example.habits.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("HABIT_NAME")
        val habitDescription = intent.getStringExtra("HABIT_DESCRIPTION")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Уведомления запрещены, не отправляем
            return
        }

        val notification = NotificationCompat.Builder(context, "habit_reminder_channel")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Напоминание: $habitName")
            .setContentText("Не забудьте выполнить: $habitDescription")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(habitName.hashCode(), notification)

    }
}
