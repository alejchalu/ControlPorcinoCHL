package com.control.porcinochl

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class NotificacionReceiver : BroadcastReceiver() {
    private val TAG = "NotificacionReceiver"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Recibido intent: ${intent.action}")

        if (intent.action == "com.control.porcinochl.ACTION_NOTIFICATION") {
            if (!hasNotificationPermission(context)) {
                Log.w(TAG, "No se tienen los permisos necesarios")
                return
            }
            showNotification(context)
        } else if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Dispositivo reiniciado, reprogramando notificación")
            reprogramarNotificacion(context)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Para versiones anteriores no se necesita permiso explícito
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context) {
        try {
            val channelId = "notificaciones_simples"
            createNotificationChannel(context, channelId)

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🐖 Eventos de hoy")
                .setContentText("Revisa los eventos programados para tus cerdas")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 200, 100, 200))
                .build()

            val notificationId = Calendar.getInstance().run {
                get(Calendar.DAY_OF_YEAR) + get(Calendar.HOUR_OF_DAY)
            }

            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Notificación mostrada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Recordatorios",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de eventos diarios"
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    vibrationPattern = longArrayOf(0, 200, 100, 200)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Canal de notificación creado")
            }
        }
    }

    // 🚀 AGREGADO para reprogramar notificación después de reinicio
    @SuppressLint("ScheduleExactAlarm")
    private fun reprogramarNotificacion(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificacionReceiver::class.java).apply {
                action = "com.control.porcinochl.ACTION_NOTIFICATION"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 7) // 🕖 Hora de la notificación diaria: 7 AM
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1) // Si ya pasó, programa para mañana
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Notificación reprogramada para las 7:30 AM")
        } catch (e: Exception) {
            Log.e(TAG, "Error al reprogramar notificación: ${e.message}")
        }
    }
}
