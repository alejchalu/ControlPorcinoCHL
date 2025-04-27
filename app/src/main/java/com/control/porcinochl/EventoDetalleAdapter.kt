package com.control.porcinochl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class EventoDetalleAdapter(
    private val eventos: List<Evento>,
    private val cerdas: Map<String, Cerda>
) : RecyclerView.Adapter<EventoDetalleAdapter.EventoViewHolder>() {

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
        val txtIdCerda: TextView = itemView.findViewById(R.id.txtIdCerda)
        val txtFechaPreñez: TextView = itemView.findViewById(R.id.txtFechaPreñez)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_detalle, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position] // Obtener el evento actual
        val cerda = cerdas[evento.idCerda]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        holder.txtTipo.text = "Tipo: ${evento.tipo}"
        holder.txtIdCerda.text = "Arete: ${evento.idCerda}" // Texto actualizado
        holder.txtFechaPreñez.text = cerda?.let {
            "Fecha de preñez: ${dateFormat.format(it.fechaPrenez)}"
        } ?: "Fecha de preñez: No disponible"
    }

    override fun getItemCount(): Int = eventos.size
}