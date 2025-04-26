package com.control.porcinochl

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
        programarNotificacionDiaria()
    }

    private fun programarNotificacionDiaria() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificacionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configurar para que se ejecute diariamente a las 7:00 AM
        // CAMBIA ESTOS VALORES SI NECESITAS MODIFICAR LA HORA:
        val hora = 20  // Hora en formato 24h (7 = 7:00 a.m.)
        val minuto = 2 // Minutos (0 = en punto)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}