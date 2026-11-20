package com.example.remediocerto.ui.hoje

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.R
import com.example.remediocerto.databinding.ItemProximoDoseBinding
import com.example.remediocerto.databinding.ItemProximoTerminoBinding
import com.example.remediocerto.util.formatarDiaMes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Lista da tela "Próximos dias": títulos de seção e de dia, remédios que acabam e doses de cada dia. */
class ProximosDiasAdapter : ListAdapter<ItemProximos, RecyclerView.ViewHolder>(Comparador) {

    class TextoViewHolder(val texto: TextView) : RecyclerView.ViewHolder(texto)
    class TerminoViewHolder(val binding: ItemProximoTerminoBinding) : RecyclerView.ViewHolder(binding.root)
    class DoseViewHolder(val binding: ItemProximoDoseBinding) : RecyclerView.ViewHolder(binding.root)

    private val local = Locale.forLanguageTag("pt-BR")
    private val formatoDiaMes = DateTimeFormatter.ofPattern("dd/MM", local)
    private val formatoDiaSemana = DateTimeFormatter.ofPattern("EEEE, dd/MM", local)

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ItemProximos.Secao, is ItemProximos.Dia -> TIPO_TITULO
        is ItemProximos.Termino -> TIPO_TERMINO
        is ItemProximos.Dose -> TIPO_DOSE
        is ItemProximos.SemDoses -> TIPO_SEM_DOSES
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TIPO_TITULO -> {
                val texto = inflater.inflate(R.layout.item_historico_dia, parent, false) as TextView
                ViewCompat.setAccessibilityHeading(texto, true) // leitores de tela pulam de título em título
                TextoViewHolder(texto)
            }
            TIPO_TERMINO -> TerminoViewHolder(ItemProximoTerminoBinding.inflate(inflater, parent, false))
            TIPO_DOSE -> DoseViewHolder(ItemProximoDoseBinding.inflate(inflater, parent, false))
            else -> TextoViewHolder(inflater.inflate(R.layout.item_proximo_sem_doses, parent, false) as TextView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ItemProximos.Secao -> (holder as TextoViewHolder).texto.setText(
                if (item.secao == SecaoProximos.TERMINAM) R.string.proximos_secao_terminam else R.string.proximos_secao_dia_a_dia
            )
            is ItemProximos.Dia -> (holder as TextoViewHolder).texto.text = rotuloDoDia(holder.itemView, item.dia)
            is ItemProximos.SemDoses -> Unit // o texto fixo já está no layout
            is ItemProximos.Termino -> {
                val binding = (holder as TerminoViewHolder).binding
                val contexto = binding.root.context
                val data = formatarDiaMes(item.fim)
                binding.tvNome.text = item.nome
                binding.tvQuando.text = when {
                    item.comecaEm != null -> contexto.getString(
                        R.string.proximos_comeca_ate, formatarDiaMes(item.comecaEm), data
                    )
                    item.faltam == 0 -> contexto.getString(R.string.proximos_termina_hoje, data)
                    item.faltam == 1 -> contexto.getString(R.string.proximos_termina_amanha, data)
                    else -> contexto.getString(R.string.proximos_faltam_dias, data, item.faltam)
                }
            }
            is ItemProximos.Dose -> {
                val binding = (holder as DoseViewHolder).binding
                binding.tvHora.text = item.hora
                binding.tvNome.text = item.nome
                binding.tvDose.text = item.dose
                binding.tvDose.isVisible = item.dose.isNotBlank()
                binding.tvUltimoDia.isVisible = item.ultimoDia
            }
        }
    }

    /** "Amanhã, 21/11" ou "Segunda-feira, 23/11". */
    private fun rotuloDoDia(view: View, diaMs: Long): String {
        val dia = Instant.ofEpochMilli(diaMs).atZone(ZoneId.systemDefault()).toLocalDate()
        return if (dia == LocalDate.now().plusDays(1)) {
            view.context.getString(R.string.proximos_amanha, dia.format(formatoDiaMes))
        } else {
            dia.format(formatoDiaSemana).replaceFirstChar { it.titlecase(local) }
        }
    }

    private companion object {
        const val TIPO_TITULO = 0
        const val TIPO_TERMINO = 1
        const val TIPO_DOSE = 2
        const val TIPO_SEM_DOSES = 3
    }

    private object Comparador : DiffUtil.ItemCallback<ItemProximos>() {
        override fun areItemsTheSame(a: ItemProximos, b: ItemProximos) = when {
            a is ItemProximos.Secao && b is ItemProximos.Secao -> a.secao == b.secao
            a is ItemProximos.Termino && b is ItemProximos.Termino -> a.medicamentoId == b.medicamentoId
            a is ItemProximos.Dia && b is ItemProximos.Dia -> a.dia == b.dia
            a is ItemProximos.Dose && b is ItemProximos.Dose ->
                a.medicamentoId == b.medicamentoId && a.previstoEm == b.previstoEm
            a is ItemProximos.SemDoses && b is ItemProximos.SemDoses -> a.dia == b.dia
            else -> false
        }

        override fun areContentsTheSame(a: ItemProximos, b: ItemProximos) = a == b
    }
}
