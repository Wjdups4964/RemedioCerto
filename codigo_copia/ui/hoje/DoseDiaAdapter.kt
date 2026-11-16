package com.example.remediocerto.ui.hoje

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.R
import com.example.remediocerto.data.model.DoseDoDia
import com.example.remediocerto.data.model.Momento
import com.example.remediocerto.data.model.StatusDose
import com.example.remediocerto.databinding.ItemDoseDiaBinding
import com.example.remediocerto.util.aplicarStatus

/**
 * Lista de doses do dia. Os botões Tomei/Adiar só aparecem quando já é a hora da dose e ela ainda
 * não foi tomada; antes disso aparece "Você poderá confirmar a dose às HH:mm".
 * No modo [somenteLeitura] (tela do familiar) nada disso aparece.
 */
class DoseDiaAdapter(
    private val somenteLeitura: Boolean = false,
    private val aoTomar: (DoseDoDia) -> Unit = {},
    private val aoAdiar: (DoseDoDia) -> Unit = {},
) : ListAdapter<DoseDoDia, DoseDiaAdapter.ViewHolder>(Comparador) {

    class ViewHolder(val binding: ItemDoseDiaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDoseDiaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dose = getItem(position)
        with(holder.binding) {
            tvHora.text = dose.hora
            tvNome.text = dose.nome
            tvDose.text = dose.dose
            tvStatus.aplicarStatus(dose.status, dose.momento)

            val pendente = !somenteLeitura && dose.status != StatusDose.TOMADA
            val jaEhHora = dose.momento != Momento.FUTURA
            layoutAcoes.isVisible = pendente && jaEhHora
            tvDisponivel.isVisible = pendente && !jaEhHora
            tvDisponivel.text = root.context.getString(R.string.hoje_disponivel_as, dose.hora)

            btnTomei.setOnClickListener { aoTomar(dose) }
            btnAdiar.setOnClickListener { aoAdiar(dose) }
        }
    }

    private object Comparador : DiffUtil.ItemCallback<DoseDoDia>() {
        override fun areItemsTheSame(a: DoseDoDia, b: DoseDoDia) = a.chave == b.chave
        override fun areContentsTheSame(a: DoseDoDia, b: DoseDoDia) = a == b
    }
}
