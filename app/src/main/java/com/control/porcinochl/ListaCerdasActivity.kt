package com.control.porcinochl

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ListaCerdasActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var adapter: ArrayAdapter<String>
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var listaCompleta = mutableListOf<String>()
    private var cerdas = mutableListOf<Cerda>()
    private val LIMITE_PAGINA = 20
    private var paginaActual = 0
    private var busquedaActual = ""
    private var isLoading = false
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_cerdas)

        listView = findViewById(R.id.listViewCerdas)
        progressBar = findViewById(R.id.progressBar)
        searchView = findViewById(R.id.searchView)

        configurarBusqueda()
        configurarScrollInfinito()
        cargarTodasLasCerdas()
    }

    private fun configurarBusqueda() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                newText?.let { textoBusqueda ->
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        paginaActual = 0
                        busquedaActual = textoBusqueda
                        if (busquedaActual.isEmpty()) {
                            cargarTodasLasCerdas()
                        } else {
                            buscarCerdasPaginado(busquedaActual, LIMITE_PAGINA, 0)
                        }
                    }
                }
                return true
            }
        })
    }

    private fun configurarScrollInfinito() {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (!isLoading && firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount >= LIMITE_PAGINA) {
                    if (busquedaActual.isNotEmpty()) {
                        paginaActual++
                        buscarCerdasPaginado(busquedaActual, LIMITE_PAGINA, paginaActual * LIMITE_PAGINA)
                    }
                }
            }
        })
    }

    private fun cargarTodasLasCerdas() {
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

    private fun buscarCerdasPaginado(query: String, limit: Int, offset: Int) {
        if (isLoading) return

        isLoading = true
        lifecycleScope.launch {
            try {
                if (offset == 0) mostrarCargando(true)

                val db = AppDatabase.getDatabase(this@ListaCerdasActivity)
                val nuevosResultados = withContext(Dispatchers.IO) {
                    db.cerdaDao().buscarPorIdPaginado("%$query%", limit, offset)
                }

                runOnUiThread {
                    if (offset == 0) {
                        cerdas = nuevosResultados.toMutableList()
                    } else {
                        cerdas.addAll(nuevosResultados)
                    }
                    actualizarLista(cerdas)
                }
            } catch (e: Exception) {
                mostrarError("Error al cargar más cerdas: ${e.message}")
                paginaActual--
            } finally {
                isLoading = false
                if (offset == 0) mostrarCargando(false)
            }
        }
    }

    private fun actualizarLista(cerdasLista: List<Cerda>) {
        val nuevosItems = cerdasLista.map { cerda ->
            "ID: ${cerda.id} \nFecha de preñez: ${dateFormat.format(cerda.fechaPrenez)}"
        }

        runOnUiThread {
            if (paginaActual == 0) {
                listaCompleta = nuevosItems.toMutableList()
                adapter = ArrayAdapter(
                    this@ListaCerdasActivity,
                    android.R.layout.simple_list_item_1,
                    listaCompleta
                )
                listView.adapter = adapter
            } else {
                listaCompleta.addAll(nuevosItems)
                adapter.notifyDataSetChanged()
            }
            configurarClickListener(cerdasLista)
        }
    }

    private fun configurarClickListener(cerdasLista: List<Cerda>) {
        listView.setOnItemClickListener { _, _, position, _ ->
            val cerda = if (paginaActual == 0) {
                cerdasLista[position]
            } else {
                cerdas[position]
            }
            val intent = Intent(this, DetalleCerdaActivity::class.java).apply {
                putExtra("id", cerda.id)
                putExtra("fechaPrenez", cerda.fechaPrenez.time)
            }
            startActivity(intent)
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        }
    }

    private fun mostrarError(mensaje: String) {
        runOnUiThread {
            Toast.makeText(this@ListaCerdasActivity, mensaje, Toast.LENGTH_LONG).show()
        }
    }
}