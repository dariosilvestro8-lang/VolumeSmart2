package it.volumesmart.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class VolumeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(
            AlarmScheduler.EXTRA_SCHEDULE_ID, -1
        )
        val isStart = intent.getBooleanExtra(
            AlarmScheduler.EXTRA_IS_START, false
        )

        if (id == -1) return

        val schedule = ScheduleStore.getSchedules(context)
            .firstOrNull { it.id == id && it.enabled }
            ?: return

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val maxVolume = audioManager.getStreamMaxVolume(
            AudioManager.STREAM_MUSIC
        )

        val percent = if (isStart) {
            schedule.volume
        } else {
            ScheduleStore.getOffVolume(context)
        }

        val volume = (maxVolume * percent / 100f).toInt()

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            0
        )

        AlarmScheduler.scheduleAlarm(context, schedule, isStart)
    }
}
