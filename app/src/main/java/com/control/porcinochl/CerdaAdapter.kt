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

        // Mostrar arete
        view.findViewById<TextView>(R.id.txtIdCerda).text = "Arete: ${cerda.id}"


        return view
    }

}