package com.control.porcinochl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventoAdapter(
    private val eventos: List<Evento>,  // Ahora usa la clase Evento directamente
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewColor: View = view.findViewById(R.id.viewColor)
        val txtTipoEvento: TextView = view.findViewById(R.id.txtTipoEvento)
        val txtIdCerda: TextView = view.findViewById(R.id.txtIdCerda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]
        holder.viewColor.setBackgroundColor(evento.color)
        holder.txtTipoEvento.text = evento.tipo
        holder.txtIdCerda.text = "Arete: ${evento.idCerda}"
        holder.itemView.setOnClickListener { onItemClick(evento) }
    }

    override fun getItemCount() = eventos.size
}