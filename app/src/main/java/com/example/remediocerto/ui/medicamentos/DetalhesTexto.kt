package com.example.remediocerto.ui.medicamentos

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.TipoFrequencia
import com.example.remediocerto.util.formatarDiaLocal
import com.example.remediocerto.util.somarDias

/**
 * Detalhes do medicamento, uma informação por linha, com o nome dela em negrito:
 * Dose, Horários, Dias, Período, Forma e Observações. Linhas sem informação não aparecem.
 */
fun Medicamento.detalhes(context: Context): CharSequence {
    val linhas = mutableListOf<Pair<String, String>>()

    if (dose.isNotBlank()) linhas += context.getString(R.string.detalhe_dose) to dose.trim()

    if (horarios.isNotEmpty()) {
        val lista = horarios.joinToString(", ")
        val quando = if (tipoFrequencia == TipoFrequencia.INTERVALO_HORAS && intervaloHoras > 0) {
            context.getString(R.string.detalhe_intervalo_horas, intervaloHoras, lista)
        } else {
            lista
        }
        linhas += context.getString(R.string.detalhe_horarios) to quando
    }

    linhas += context.getString(R.string.detalhe_dias) to
        if (intervaloDias > 1) context.getString(R.string.detalhe_a_cada_dias, intervaloDias)
        else context.getString(R.string.detalhe_todo_dia)

    val periodo = when {
        duracaoDias > 0 && inicioEm > 0 -> context.getString(
            R.string.detalhe_periodo_fim,
            formatarDiaLocal(inicioEm),
            formatarDiaLocal(somarDias(inicioEm, duracaoDias - 1)),
        )
        duracaoDias > 0 -> context.getString(R.string.detalhe_periodo_so_dias, duracaoDias)
        inicioEm > 0 -> context.getString(R.string.detalhe_periodo_continuo_desde, formatarDiaLocal(inicioEm))
        else -> context.getString(R.string.detalhe_periodo_continuo)
    }
    linhas += context.getString(R.string.detalhe_periodo) to periodo

    if (forma.isNotBlank()) linhas += context.getString(R.string.detalhe_forma) to forma.trim()
    if (observacoes.isNotBlank()) linhas += context.getString(R.string.detalhe_observacoes) to observacoes.trim()

    val texto = SpannableStringBuilder()
    linhas.forEachIndexed { i, (nome, valor) ->
        if (i > 0) texto.append("\n")
        val inicio = texto.length
        texto.append(nome).append(": ")
        texto.setSpan(StyleSpan(Typeface.BOLD), inicio, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        texto.append(valor)
    }
    return texto
}
