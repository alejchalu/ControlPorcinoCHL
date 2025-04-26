package com.control.porcinochl

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MyApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
        programarNotificacionesDiarias()
    }

    private fun programarNotificacionesDiarias() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificacionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configurar para que se ejecute diariamente a las 8:00 AM
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun verificarEventosProximos(context: Context) {
        applicationScope.launch {
            val db = AppDatabase.getDatabase(context)
            val cerdas = db.cerdaDao().obtenerCerdasConEventosProximos()

            cerdas.forEach { cerda ->
                val hoy = Calendar.getInstance()
                val fechaCelo = Calendar.getInstance().apply { time = cerda.fechaCelo }
                val fechaParto = Calendar.getInstance().apply { time = cerda.fechaParto }
                val fechaDestete = Calendar.getInstance().apply { time = cerda.fechaDestete }

                // Notificación para parto (10 días antes)
                if (diasEntre(hoy, fechaParto) == 10) {
                    enviarNotificacion(context, "Parto", cerda.id)
                }

                // Notificación para celo (mismo día)
                if (esMismoDia(hoy, fechaCelo)) {
                    enviarNotificacion(context, "Celo", cerda.id)
                }

                // Notificación para destete (mismo día)
                if (esMismoDia(hoy, fechaDestete)) {
                    enviarNotificacion(context, "Destete", cerda.id)
                }
            }
        }
    }

    private fun enviarNotificacion(context: Context, tipoEvento: String, idCerda: String) {
        val intent = Intent(context, NotificacionReceiver::class.java).apply {
            putExtra("tipoEvento", tipoEvento)
            putExtra("idCerda", idCerda)
        }
        context.sendBroadcast(intent)
    }

    private fun diasEntre(cal1: Calendar, cal2: Calendar): Int {
        val milis1 = cal1.timeInMillis
        val milis2 = cal2.timeInMillis
        return ((milis2 - milis1) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}