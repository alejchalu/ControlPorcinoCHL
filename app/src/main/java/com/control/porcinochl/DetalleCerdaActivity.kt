package com.control.porcinochl

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        idCerda = intent.getStringExtra("id") ?: run {
            Toast.makeText(this, "Error: ID no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        title = "Detalle: Arete $idCerda"

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
        val fechaJaulaMaternidad = calcularFecha(fechaPrenez, 104)
        val fechaParto = calcularFecha(fechaPrenez, 114)
        val fechaDestete = calcularFecha(fechaParto, 21)

        findViewById<TextView>(R.id.textDetalle).text = """
            Fecha de preñez: ${dateFormat.format(fechaPrenez)}
            Repetición de celo: ${dateFormat.format(fechaCelo)}
            Cambio jaula maternidad: ${dateFormat.format(fechaJaulaMaternidad)}
            Parto estimado: ${dateFormat.format(fechaParto)}
            Destete estimado: ${dateFormat.format(fechaDestete)}
        """.trimIndent()
    }

    private fun configurarBotones() {
        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
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
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()

            // Cambiar colores de los botones después de mostrar el diálogo
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.button_danger)) // Botón "Eliminar" en rojo
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.surface)) // Botón "Cancelar" en color normal

            val messageView = alertDialog.findViewById<TextView>(android.R.id.message)
            messageView?.setTextColor(getColor(android.R.color.white))

            val titleView = alertDialog.window?.decorView?.findViewById<TextView>(
                resources.getIdentifier("alertTitle", "id", packageName)
            )
            titleView?.setTextColor(getColor(android.R.color.white))


        }

        findViewById<Button>(R.id.btnEditar).setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = fechaPrenez }

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val nuevaFecha = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time

                    lifecycleScope.launch {
                        db.cerdaDao().actualizar(Cerda(idCerda, nuevaFecha))
                        runOnUiThread {
                            fechaPrenez = nuevaFecha
                            mostrarDetalles()
                            Toast.makeText(
                                this@DetalleCerdaActivity,
                                "Fecha actualizada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            setResult(RESULT_OK)
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
    }

    private fun calcularFecha(base: Date, dias: Int): Date {
        return Calendar.getInstance().apply {
            time = base
            add(Calendar.DAY_OF_YEAR, dias)
        }.time
    }
}
