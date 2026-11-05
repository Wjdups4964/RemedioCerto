package com.example.remediocerto.ui.hoje

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.DoseDia
import com.example.remediocerto.databinding.ItemDoseDiaBinding

/** Lista de doses do dia (usada em Hoje e em Acompanhamento). */
class DoseDiaAdapter(private val doses: List<DoseDia>) :
    RecyclerView.Adapter<DoseDiaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDoseDiaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDoseDiaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dose = doses[position]
        holder.binding.tvHora.text = dose.hora
        holder.binding.tvNome.text = dose.nome
        holder.binding.tvDose.text = dose.dose
        holder.binding.tvStatus.text = dose.status
    }

    override fun getItemCount() = doses.size
}
