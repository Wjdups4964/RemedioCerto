package com.example.remediocerto.ui.vinculo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.Vinculo
import com.example.remediocerto.databinding.ItemVinculoBinding

/**
 * Lista de vínculos. [nomeDe] escolhe quem aparece (o familiar, na tela do idoso; o idoso, na do familiar)
 * e [rotuloBotao] é o texto do botão de encerrar.
 */
class VinculoAdapter(
    private val nomeDe: (Vinculo) -> String,
    private val rotuloBotao: Int,
    private val aoEncerrar: (Vinculo) -> Unit,
) : ListAdapter<Vinculo, VinculoAdapter.ViewHolder>(Comparador) {

    class ViewHolder(val binding: ItemVinculoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemVinculoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvNomeFamiliar.text = nomeDe(item)
        holder.binding.btnRevogar.setText(rotuloBotao)
        holder.binding.btnRevogar.setOnClickListener { aoEncerrar(item) }
    }

    private object Comparador : DiffUtil.ItemCallback<Vinculo>() {
        override fun areItemsTheSame(a: Vinculo, b: Vinculo) = a.id == b.id
        override fun areContentsTheSame(a: Vinculo, b: Vinculo) = a == b
    }
}
