package com.example.remediocerto.util

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Momento
import com.example.remediocerto.data.model.StatusDose
import com.example.remediocerto.ui.historico.SituacaoDose

/**
 * Escreve o status na "pílula" e pinta texto e fundo: verde = tomada, amarelo = adiada,
 * vermelho = perdida/atrasada, verde-água = ainda por vir. Cor nunca vem sozinha: o texto diz o mesmo.
 */
fun TextView.aplicarStatus(status: StatusDose?, momento: Momento = Momento.FUTURA) {
    val (texto, cor, fundo) = when {
        status == StatusDose.TOMADA -> Triple(R.string.status_tomada, R.color.rc_tomada, R.color.rc_tomada_fundo)
        status == StatusDose.ADIADA -> Triple(R.string.status_adiada, R.color.rc_adiada, R.color.rc_adiada_fundo)
        status == StatusDose.PERDIDA -> Triple(R.string.status_perdida, R.color.rc_perdida, R.color.rc_perdida_fundo)
        momento == Momento.ATRASADA -> Triple(R.string.status_atrasada, R.color.rc_perdida, R.color.rc_perdida_fundo)
        momento == Momento.AGORA -> Triple(R.string.status_agora, R.color.rc_adiada, R.color.rc_adiada_fundo)
        else -> Triple(R.string.status_proxima, R.color.rc_primaria_escura, R.color.rc_primaria_clara)
    }
    setText(texto)
    setTextColor(ContextCompat.getColor(context, cor))
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, fundo))
}

/**
 * Mesma ideia de [aplicarStatus], para o histórico: verde = tomada, amarelo = tomada com atraso ou adiada,
 * vermelho = atrasada ou não tomada. O texto sempre diz o mesmo que a cor.
 */
fun TextView.aplicarSituacao(situacao: SituacaoDose) {
    val (texto, cor, fundo) = when (situacao) {
        SituacaoDose.TOMADA -> Triple(R.string.status_tomada, R.color.rc_tomada, R.color.rc_tomada_fundo)
        SituacaoDose.TOMADA_COM_ATRASO -> Triple(R.string.status_tomada_atraso, R.color.rc_adiada, R.color.rc_adiada_fundo)
        SituacaoDose.ADIADA -> Triple(R.string.status_adiada, R.color.rc_adiada, R.color.rc_adiada_fundo)
        SituacaoDose.ATRASADA -> Triple(R.string.status_atrasada, R.color.rc_perdida, R.color.rc_perdida_fundo)
        SituacaoDose.NAO_TOMADA -> Triple(R.string.status_nao_tomada, R.color.rc_perdida, R.color.rc_perdida_fundo)
    }
    setText(texto)
    setTextColor(ContextCompat.getColor(context, cor))
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, fundo))
}
