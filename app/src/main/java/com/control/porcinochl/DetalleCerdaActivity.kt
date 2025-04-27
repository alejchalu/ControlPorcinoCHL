package com.control.porcinochl

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetalleCerdaActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var idCerda: String
    private var fechaPrenez: Date = Date()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cerda)
        db = AppDatabase.getDatabase(this)

        // Obtener datos del Intent
        idCerda = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Error: ID no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // En onCreate(), después de obtener el ID:
        title = "Detalle: Arete $idCerda" // Antes: "Detalle: ID $idCerda"

        fechaPrenez = Date(intent.getLongExtra("fechaPrenez", -1)).takeIf { it.time > 0 } ?: run {
            Toast.makeText(this, "Error: Fecha no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mostrarDetalles()
        configurarBotones()
    }

    private fun mostrarDetalles() {
        val fechaCelo = calcularFecha(fechaPrenez, 21)
        val fechaParto = calcularFecha(fechaPrenez, 114)
        val fechaDestete = calcularFecha(fechaParto, 21)

        findViewById<TextView>(R.id.textDetalle).text = """
        Fecha de preñez: ${dateFormat.format(fechaPrenez)}
        Repetición de celo: ${dateFormat.format(fechaCelo)}
        Parto estimado: ${dateFormat.format(fechaParto)}
        Destete estimado: ${dateFormat.format(fechaDestete)}
    """.trimIndent()
    }

    private fun configurarBotones() {
        // Botón Eliminar
        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            // Mostrar diálogo de confirmación
            AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar esta cerda?")
                .setPositiveButton("Eliminar") { dialog, _ ->
                    lifecycleScope.launch {
                        db.cerdaDao().eliminar(Cerda(idCerda, fechaPrenez))
                        runOnUiThread {
                            Toast.makeText(
                                this@DetalleCerdaActivity,
                                "Cerda eliminada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        finish()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        // Botón Editar (CORRECCIÓN AQUÍ)
        findViewById<Button>(R.id.btnEditar).setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = fechaPrenez }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val nuevaFecha = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time

                    lifecycleScope.launch {
                        // Actualizar la fecha en la base de datos
                        db.cerdaDao().actualizar(Cerda(idCerda, nuevaFecha))

                        // Actualizar la variable local y mostrar los nuevos datos
                        fechaPrenez = nuevaFecha
                        mostrarDetalles()

                        Toast.makeText(
                            this@DetalleCerdaActivity,
                            "Fecha actualizada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun calcularFecha(base: Date, dias: Int): Date {
        return Calendar.getInstance().apply {
            time = base
            add(Calendar.DAY_OF_YEAR, dias)
        }.time
    }
}