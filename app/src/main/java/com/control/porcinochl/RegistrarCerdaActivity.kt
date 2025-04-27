package com.control.porcinochl

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistrarCerdaActivity : AppCompatActivity() {

    private lateinit var edtId: EditText
    private lateinit var btnFecha: Button
    private lateinit var btnGuardar: Button
    private lateinit var fechaSeleccionada: Date
    private val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_cerda)

        edtId = findViewById(R.id.edtIdCerda)
        btnFecha = findViewById(R.id.btnFechaPreñez)
        btnGuardar = findViewById(R.id.btnGuardarCerda)

        // Configuración del selector de fecha para la preñez
        btnFecha.setOnClickListener {
            val ahora = Calendar.getInstance()
            DatePickerDialog(
                this@RegistrarCerdaActivity,
                R.style.DatePickerTheme,
                { _, year, month, dayOfMonth ->
                    val fecha = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time
                    fechaSeleccionada = fecha
                    btnFecha.text = formato.format(fecha)
                },
                ahora.get(Calendar.YEAR),
                ahora.get(Calendar.MONTH),
                ahora.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnGuardar.setOnClickListener {
            val id = edtId.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(this, "El arete de la cerda no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!::fechaSeleccionada.isInitialized) {
                Toast.makeText(this, "Debe seleccionar la fecha de preñez", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val cerda = Cerda(id, fechaSeleccionada)
                    val db = AppDatabase.getDatabase(this@RegistrarCerdaActivity)

                    val existe = db.cerdaDao().getCerdaById(id) != null
                    if (existe) {
                        runOnUiThread {
                            Toast.makeText(
                                this@RegistrarCerdaActivity,
                                "Ya existe una cerda con ese arete",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }

                    db.cerdaDao().insertar(cerda)

                    runOnUiThread {
                        Toast.makeText(
                            this@RegistrarCerdaActivity,
                            "Cerda registrada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegistrarCerdaActivity,
                            "Error al registrar la cerda: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}