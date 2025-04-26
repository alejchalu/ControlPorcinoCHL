package com.control.porcinochl

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ListaCerdasActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var adapter: CerdaAdapter // Cambiado a CerdaAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var cerdas = mutableListOf<Cerda>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_cerdas)

        listView = findViewById(R.id.listViewCerdas)
        progressBar = findViewById(R.id.progressBar)
        searchView = findViewById(R.id.searchView)

        // Configurar el adaptador personalizado
        adapter = CerdaAdapter(this, cerdas)
        listView.adapter = adapter

        // Configurar búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filtrarCerdas(it) }
                return true
            }
        })

        cargarCerdas()
    }

    private fun cargarCerdas() {
        lifecycleScope.launch {
            try {
                mostrarCargando(true)
                val db = AppDatabase.getDatabase(this@ListaCerdasActivity)
                cerdas = withContext(Dispatchers.IO) {
                    db.cerdaDao().obtenerTodas()
                }.toMutableList()

                actualizarLista(cerdas)
            } catch (e: Exception) {
                mostrarError("Error al cargar cerdas: ${e.message}")
            } finally {
                mostrarCargando(false)
            }
        }
    }

    private fun filtrarCerdas(query: String) {
        val resultados = if (query.isEmpty()) {
            cerdas
        } else {
            cerdas.filter { it.id.contains(query, ignoreCase = true) }
        }
        actualizarLista(resultados)
    }

    private fun actualizarLista(cerdasLista: List<Cerda>) {
        runOnUiThread {
            adapter.actualizarDatos(cerdasLista) // Usamos el nuevo método del adapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val cerda = cerdasLista[position]
                val intent = Intent(this, DetalleCerdaActivity::class.java).apply {
                    putExtra("id", cerda.id)
                    putExtra("fechaPrenez", cerda.fechaPrenez.time)
                }
                startActivity(intent)
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        listView.visibility = if (mostrar) View.GONE else View.VISIBLE
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}