package com.control.porcinochl

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DetalleEventosActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_eventos)

        val fecha = intent.getStringExtra("fecha") ?: "Fecha desconocida"
        val eventos = intent.getParcelableArrayListExtra<Evento>("eventos") ?: emptyList()

        Log.d("DETALLE_DEBUG", "Fecha recibida: $fecha")

        val titulo = try {
            // Parseo directo del formato ISO (yyyy-MM-dd)
            val fechaLocal = LocalDate.parse(fecha)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val fechaFormateada = fechaLocal.format(formatter)

            Log.d("DETALLE_DEBUG", "Fecha formateada: $fechaFormateada")
            "Eventos del día $fechaFormateada"
        } catch (e: DateTimeParseException) {
            Log.e("DETALLE_ERROR", "Error al parsear fecha: ${e.message}")
            "Eventos del día $fecha"
        } catch (e: Exception) {
            Log.e("DETALLE_ERROR", "Error inesperado: ${e.message}")
            "Eventos del día $fecha"
        }

        findViewById<TextView>(R.id.txtTituloFecha).text = titulo

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@DetalleEventosActivity)
            val cerdas = db.cerdaDao().getCerdasByIds(eventos.map { it.idCerda }).associateBy { it.id }

            findViewById<RecyclerView>(R.id.rvEventosDetalle).apply {
                layoutManager = LinearLayoutManager(this@DetalleEventosActivity)
                adapter = EventoDetalleAdapter(eventos, cerdas)
            }
        }
    }
}