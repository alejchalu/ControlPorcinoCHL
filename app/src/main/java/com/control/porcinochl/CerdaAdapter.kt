package com.control.porcinochl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class CerdaAdapter(
    private val context: Context,
    private var cerdas: List<Cerda>
) : BaseAdapter() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun actualizarDatos(nuevasCerdas: List<Cerda>) {
        this.cerdas = nuevasCerdas
        notifyDataSetChanged()
    }

    override fun getCount(): Int = cerdas.size

    override fun getItem(position: Int): Cerda = cerdas[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_cerda, parent, false)

        val cerda = getItem(position)

        // Configurar vistas
        view.findViewById<TextView>(R.id.txtIdCerda).text = "ID: ${cerda.id}"
        view.findViewById<TextView>(R.id.txtFechaPrenez).text = "Preñez: ${dateFormat.format(cerda.fechaPrenez)}"

        // Configurar próximo evento
        val (proximoEvento, color) = calcularProximoEvento(cerda)
        view.findViewById<TextView>(R.id.txtProximoEvento).apply {
            text = proximoEvento
            setTextColor(color)
        }

        // Configurar fechas de eventos
        view.findViewById<TextView>(R.id.txtFechaCelo).text = "Celo: ${dateFormat.format(cerda.fechaCelo)}"
        view.findViewById<TextView>(R.id.txtFechaParto).text = "Parto: ${dateFormat.format(cerda.fechaParto)}"

        return view
    }

    private fun calcularProximoEvento(cerda: Cerda): Pair<String, Int> {
        val hoy = Calendar.getInstance()
        val fechaCelo = Calendar.getInstance().apply { time = cerda.fechaCelo }
        val fechaParto = Calendar.getInstance().apply { time = cerda.fechaParto }

        return when {
            hoy.before(fechaCelo) -> Pair("PRÓXIMO: CELO", context.getColor(R.color.celo_color))
            hoy.before(fechaParto) -> Pair("PRÓXIMO: PARTO", context.getColor(R.color.parto_color))
            else -> Pair("PRÓXIMO: DESTETE", context.getColor(R.color.destete_color))
        }
    }
}