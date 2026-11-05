package com.example.remediocerto.ui.historico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.HistoricoItem
import com.example.remediocerto.databinding.ItemHistoricoBinding

/** Lista de doses registradas (RF14, RF16). */
class HistoricoAdapter(private val itens: List<HistoricoItem>) :
    RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHistoricoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemHistoricoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.binding.tvNome.text = item.nome
        holder.binding.tvData.text = item.data
        holder.binding.tvStatus.text = item.status
    }

    override fun getItemCount() = itens.size
}
