package com.control.porcinochl

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.control.porcinochl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val channelId = "notificaciones_porcinos"

    // Registros para permisos
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("Permisos", "Permiso de notificaciones denegado")
            showRationaleDialog(
                "Las notificaciones son esenciales para recordarte los eventos importantes",
                { openAppSettings() }
            )
        }
    }

    private val alarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permiso de alarmas exactas concedido", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("Permisos", "Permiso de alarmas exactas denegado")
            showRationaleDialog(
                "Las alarmas exactas aseguran que las notificaciones lleguen a tiempo",
                { openAlarmSettings() }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        setupButtons()
        checkAndRequestPermissions()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Porcinos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de la aplicación"
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupButtons() {
        binding.btnVerCerdas.setOnClickListener {
            startActivity(Intent(this, ListaCerdasActivity::class.java))
        }

        binding.btnRegistrarNueva.setOnClickListener {
            startActivity(Intent(this, RegistrarCerdaActivity::class.java))
        }

        binding.btnVerCalendario.setOnClickListener {
            startActivity(Intent(this, CalendarioActivity::class.java))
        }

        binding.btnTestNotif.setOnClickListener {
            showTestNotification()
        }
    }

    private fun showTestNotification() {
        // Verificar permiso primero (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "Primero concede permiso de notificaciones",
                    Toast.LENGTH_SHORT
                ).show()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Construir y mostrar notificación de prueba
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Prueba de notificación")
            .setContentText("¡Esta es una notificación de prueba exitosa!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(999, notification) // ID único para notificación de prueba
                Toast.makeText(
                    this@MainActivity,
                    "Notificación de prueba enviada",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("Notificación", "Error al mostrar notificación: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error al mostrar notificación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // Permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    showRationaleDialog(
                        "Necesitamos permiso para mostrar notificaciones importantes",
                        { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    )
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // Permiso de alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                    showRationaleDialog(
                        "Las alarmas exactas aseguran que recibas recordatorios a tiempo",
                        { alarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM) }
                    )
                } else {
                    alarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
                }
            }
        }
    }

    private fun showRationaleDialog(message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage(message)
            .setPositiveButton("Aceptar") { _, _ -> action() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openAppSettings() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Abre Configuración manualmente", Toast.LENGTH_LONG).show()
        }
    }

    private fun openAlarmSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            } else {
                openAppSettings()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Abre Configuración > Alarmas exactas", Toast.LENGTH_LONG).show()
        }
    }
}