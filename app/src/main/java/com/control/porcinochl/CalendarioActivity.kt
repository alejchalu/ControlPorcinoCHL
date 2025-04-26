package com.control.porcinochl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates

class CalendarioActivity : AppCompatActivity() {

    private var COLOR_PREÑEZ by Delegates.notNull<Int>()
    private var COLOR_CELO by Delegates.notNull<Int>()
    private var COLOR_PARTO by Delegates.notNull<Int>()
    private var COLOR_DESTETE by Delegates.notNull<Int>()

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var rvEventos: RecyclerView
    private lateinit var txtEventosTitle: TextView
    private lateinit var btnVerDetalles: Button

    private val eventosMap = mutableMapOf<CalendarDay, MutableList<Evento>>()
    private var fechaSeleccionada: CalendarDay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        COLOR_PREÑEZ = ContextCompat.getColor(this, R.color.prenhez_color)
        COLOR_CELO = ContextCompat.getColor(this, R.color.celo_color)
        COLOR_PARTO = ContextCompat.getColor(this, R.color.parto_color)
        COLOR_DESTETE = ContextCompat.getColor(this, R.color.destete_color)

        calendarView = findViewById(R.id.calendarView)
        rvEventos = findViewById(R.id.rvEventos)
        txtEventosTitle = findViewById(R.id.txtEventosTitle)
        btnVerDetalles = findViewById(R.id.btnVerDetalles)

        rvEventos.layoutManager = LinearLayoutManager(this)
        rvEventos.setHasFixedSize(true)

        calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                fechaSeleccionada = date
                mostrarEventosFecha(date)
            }
        }

        btnVerDetalles.setOnClickListener {
            fechaSeleccionada?.let { fecha ->
                val eventos = eventosMap[fecha] ?: emptyList()
                if (eventos.isNotEmpty()) {
                    // Formateo correcto de la fecha (mes +1 porque CalendarDay usa 0-11)
                    val fechaISO = String.format(Locale.US, "%04d-%02d-%02d",
                        fecha.year,
                        fecha.month + 1,  // ¡Corrección clave aquí!
                        fecha.day)

                    Log.d("CALENDARIO_DEBUG", "Enviando fecha formateada: $fechaISO")

                    val intent = Intent(this, DetalleEventosActivity::class.java).apply {
                        putExtra("fecha", fechaISO)
                        putParcelableArrayListExtra("eventos", ArrayList(eventos))
                    }
                    startActivity(intent)
                }
            }
        }

        cargarEventos()
    }

    private fun cargarEventos() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@CalendarioActivity)
            val cerdas = db.cerdaDao().obtenerTodas()

            calendarView.removeDecorators()
            eventosMap.clear()

            cerdas.forEach { cerda ->
                listOf(
                    Evento(cerda.fechaPrenez, "Preñez", cerda.id, COLOR_PREÑEZ),
                    Evento(cerda.fechaCelo, "Celo", cerda.id, COLOR_CELO),
                    Evento(cerda.fechaParto, "Parto", cerda.id, COLOR_PARTO),
                    Evento(cerda.fechaDestete, "Destete", cerda.id, COLOR_DESTETE)
                ).forEach { evento ->
                    val calendarDay = CalendarDay.from(evento.fecha)
                    eventosMap.getOrPut(calendarDay) { mutableListOf() }.add(evento)
                    calendarView.addDecorator(EventDecorator(calendarDay, evento.color))
                }
            }

            calendarView.invalidateDecorators()
        }
    }

    private fun mostrarEventosFecha(fecha: CalendarDay) {
        val eventos = eventosMap[fecha] ?: emptyList()

        if (eventos.isNotEmpty()) {
            txtEventosTitle.visibility = View.VISIBLE
            rvEventos.visibility = View.VISIBLE
            btnVerDetalles.visibility = View.VISIBLE

            rvEventos.adapter = EventoAdapter(eventos) { evento ->
                startActivity(Intent(this, DetalleCerdaActivity::class.java).apply {
                    putExtra("id", evento.idCerda)
                })
            }
        } else {
            txtEventosTitle.visibility = View.GONE
            rvEventos.visibility = View.GONE
            btnVerDetalles.visibility = View.GONE
        }
    }

    class EventDecorator(private val day: CalendarDay, private val color: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = this.day == day
        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(12f, color))
        }
    }
}