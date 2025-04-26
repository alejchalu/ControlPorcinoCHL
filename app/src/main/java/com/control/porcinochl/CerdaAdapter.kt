package com.control.porcinochl

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CerdaAdapter(
    private val context: Context,
    private val cerdas: List<Cerda>
) : ArrayAdapter<Cerda>(context, R.layout.item_cerda, cerdas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_cerda, parent, false)

        val cerda = getItem(position) ?: return view

        // Configurar vistas
        view.findViewById<TextView>(R.id.txtIdCerda).text = "ID: ${cerda.id}"

        val fechaPrenez = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cerda.fechaPrenez)
        view.findViewById<TextView>(R.id.txtFechaPrenez).text = "Preñez: $fechaPrenez"

        // Calcular próximo evento
        val (proximoEvento, color) = calcularProximoEvento(cerda)
        view.findViewById<TextView>(R.id.txtProximoEvento).apply {
            text = proximoEvento
            setTextColor(color)
        }

        return view
    }

    private fun calcularProximoEvento(cerda: Cerda): Pair<String, Int> {
        val hoy = Calendar.getInstance()
        val fechaCelo = Calendar.getInstance().apply { time = cerda.fechaCelo }
        val fechaParto = Calendar.getInstance().apply { time = cerda.fechaParto }

        return when {
            hoy.before(fechaCelo) -> Pair("Próximo: Celo", Color.parseColor("#D32F2F"))
            hoy.before(fechaParto) -> Pair("Próximo: Parto", Color.parseColor("#388E3C"))
            else -> Pair("Próximo: Destete", Color.parseColor("#1976D2"))
        }
    }
}