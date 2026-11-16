package com.example.remediocerto.ui.historico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.databinding.ItemHistoricoBinding
import com.example.remediocerto.util.aplicarStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoAdapter : ListAdapter<RegistroDose, HistoricoAdapter.ViewHolder>(Comparador) {

    class ViewHolder(val binding: ItemHistoricoBinding) : RecyclerView.ViewHolder(binding.root)

    private val formato = SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemHistoricoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvNome.text = item.medicamentoNome
        holder.binding.tvData.text = formato.format(Date(item.previstoEm))
        holder.binding.tvStatus.aplicarStatus(item.status)
    }

    private object Comparador : DiffUtil.ItemCallback<RegistroDose>() {
        override fun areItemsTheSame(a: RegistroDose, b: RegistroDose) = a.id == b.id
        override fun areContentsTheSame(a: RegistroDose, b: RegistroDose) = a == b
    }
}
