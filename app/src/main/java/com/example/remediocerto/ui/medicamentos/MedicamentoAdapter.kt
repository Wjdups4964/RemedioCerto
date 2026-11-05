package com.example.remediocerto.ui.medicamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.MedicamentoItem
import com.example.remediocerto.databinding.ItemMedicamentoBinding

/** Lista de medicamentos cadastrados (RF05). */
class MedicamentoAdapter(
    private val itens: List<MedicamentoItem>,
    private val aoClicar: (MedicamentoItem) -> Unit,
) : RecyclerView.Adapter<MedicamentoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMedicamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMedicamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.binding.tvNome.text = item.nome
        holder.binding.tvDoseHorarios.text = "${item.dose} · ${item.horarios}"
        holder.binding.root.setOnClickListener { aoClicar(item) }
    }

    override fun getItemCount() = itens.size
}
