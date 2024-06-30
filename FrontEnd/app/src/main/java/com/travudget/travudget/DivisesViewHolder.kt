package com.travudget.travudget

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DivisesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val txtCodi: TextView = itemView.findViewById(R.id.txtCodiDivisa)
    private val txtNom: TextView = itemView.findViewById(R.id.txtNomDivisa)

    fun bind(pair: Pair<String, String>) {
        txtCodi.text = pair.first
        txtNom.text = pair.second
    }
}
