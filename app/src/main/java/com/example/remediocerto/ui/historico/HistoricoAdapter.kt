package com.example.remediocerto.ui.historico

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.R
import com.example.remediocerto.databinding.ItemHistoricoBinding
import com.example.remediocerto.util.aplicarStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoricoAdapter : ListAdapter<ItemHistorico, RecyclerView.ViewHolder>(Comparador) {

    class DiaViewHolder(val texto: TextView) : RecyclerView.ViewHolder(texto)
    class DoseViewHolder(val binding: ItemHistoricoBinding) : RecyclerView.ViewHolder(binding.root)

    private val local = Locale.forLanguageTag("pt-BR")
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm", local)
    private val formatoDiaMes = DateTimeFormatter.ofPattern("dd/MM", local)
    private val formatoDiaSemana = DateTimeFormatter.ofPattern("EEEE, dd/MM", local)
    private val formatoDiaSemanaAno = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", local)

    override fun getItemViewType(position: Int) =
        if (getItem(position) is ItemHistorico.Dia) TIPO_DIA else TIPO_DOSE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_DIA) {
            val texto = inflater.inflate(R.layout.item_historico_dia, parent, false) as TextView
            ViewCompat.setAccessibilityHeading(texto, true) // leitores de tela pulam de dia em dia
            DiaViewHolder(texto)
        } else {
            DoseViewHolder(ItemHistoricoBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ItemHistorico.Dia -> (holder as DiaViewHolder).texto.text = rotuloDoDia(holder.itemView, item.dia)
            is ItemHistorico.Dose -> {
                val binding = (holder as DoseViewHolder).binding
                val registro = item.registro
                val hora = Instant.ofEpochMilli(registro.previstoEm).atZone(ZoneId.systemDefault()).format(formatoHora)
                binding.tvNome.text = registro.medicamentoNome
                binding.tvData.text = binding.root.context.getString(R.string.historico_dose_das, hora)
                binding.tvStatus.aplicarStatus(registro.status)
            }
        }
    }

    /** "Hoje, 20/11", "Ontem, 19/11" ou "Segunda-feira, 16/11" (com o ano, se for de outro ano). */
    private fun rotuloDoDia(view: android.view.View, dia: LocalDate): String {
        val hoje = LocalDate.now()
        val contexto = view.context
        return when {
            dia == hoje -> contexto.getString(R.string.historico_hoje, dia.format(formatoDiaMes))
            dia == hoje.minusDays(1) -> contexto.getString(R.string.historico_ontem, dia.format(formatoDiaMes))
            else -> dia.format(if (dia.year == hoje.year) formatoDiaSemana else formatoDiaSemanaAno)
                .replaceFirstChar { it.titlecase(local) }
        }
    }

    private companion object {
        const val TIPO_DIA = 0
        const val TIPO_DOSE = 1
    }

    private object Comparador : DiffUtil.ItemCallback<ItemHistorico>() {
        override fun areItemsTheSame(a: ItemHistorico, b: ItemHistorico) = when {
            a is ItemHistorico.Dia && b is ItemHistorico.Dia -> a.dia == b.dia
            a is ItemHistorico.Dose && b is ItemHistorico.Dose -> a.registro.id == b.registro.id
            else -> false
        }

        override fun areContentsTheSame(a: ItemHistorico, b: ItemHistorico) = a == b
    }
}
