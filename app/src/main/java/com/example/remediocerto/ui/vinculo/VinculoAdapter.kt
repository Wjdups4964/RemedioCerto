package com.example.remediocerto.ui.vinculo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.VinculoItem
import com.example.remediocerto.databinding.ItemVinculoBinding

/** Lista de familiares vinculados, com botão de revogar (RF30). */
class VinculoAdapter(
    private val itens: List<VinculoItem>,
    private val aoRevogar: (VinculoItem) -> Unit,
) : RecyclerView.Adapter<VinculoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemVinculoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemVinculoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.binding.tvNomeFamiliar.text = item.nome
        holder.binding.btnRevogar.setOnClickListener { aoRevogar(item) }
    }

    override fun getItemCount() = itens.size
}
