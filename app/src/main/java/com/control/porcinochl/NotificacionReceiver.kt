package com.control.porcinochl

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class NotificacionReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val tipoEvento = intent.getStringExtra("tipoEvento") ?: "Evento"
        val idCerda = intent.getStringExtra("idCerda") ?: ""
        val channelId = "notificaciones_cerdas"

        createNotificationChannel(context, channelId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio: $tipoEvento")
            .setContentText(buildNotificationText(tipoEvento, idCerda))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            idCerda.hashCode(), // ID único basado en el ID de la cerda
            notification
        )
    }

    private fun buildNotificationText(tipoEvento: String, idCerda: String): String {
        return when (tipoEvento) {
            "Parto" -> "La cerda $idCerda tendrá parto en 10 días"
            "Celo" -> "La cerda $idCerda está en celo hoy"
            "Destete" -> "Hoy es el destete de la cerda $idCerda"
            else -> "Recordatorio importante para la cerda $idCerda"
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Eventos Porcinos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para fechas clave"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}