package it.volumesmart.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_SCHEDULE_ID = "schedule_id"
    const val EXTRA_IS_START = "is_start"

    fun scheduleAll(context: Context) {
        val schedules = ScheduleStore.getSchedules(context)
        schedules.forEach { schedule ->
            if (schedule.enabled) {
                scheduleAlarm(context, schedule, true)
                scheduleAlarm(context, schedule, false)
            } else {
                cancelAlarm(context, schedule.id, true)
                cancelAlarm(context, schedule.id, false)
            }
        }
    }

    fun scheduleAlarm(
        context: Context,
        schedule: Schedule,
        isStart: Boolean
    ) {
        val time = if (isStart) schedule.start else schedule.end
        val parts = time.split(":")
        if (parts.size != 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, VolumeAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_IS_START, isStart)
        }

        val requestCode = schedule.id * 2 + if (isStart) 0 else 1

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, id: Int, isStart: Boolean) {
        val intent = Intent(context, VolumeAlarmReceiver::class.java)
        val requestCode = id * 2 + if (isStart) 0 else 1

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
    }
}
