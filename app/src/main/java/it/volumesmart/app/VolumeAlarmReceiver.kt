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

        val percent = if (isStart) {
            schedule.volume
        } else {
            ScheduleStore.getOffVolume(context)
        }

        fun setVolume(stream: Int) {
            val maxVolume = audioManager.getStreamMaxVolume(stream)
            val volume = (maxVolume * percent / 100f).toInt()

            audioManager.setStreamVolume(
                stream,
                volume,
                0
            )
        }

        // Volume multimedia
        setVolume(AudioManager.STREAM_MUSIC)

        // Volume suoneria
        setVolume(AudioManager.STREAM_RING)

        // Volume notifiche
        setVolume(AudioManager.STREAM_NOTIFICATION)

        // Volume chiamate
        setVolume(AudioManager.STREAM_VOICE_CALL)

        // Programma il prossimo cambio di volume
        AlarmScheduler.scheduleAlarm(context, schedule, isStart)
    }
}
