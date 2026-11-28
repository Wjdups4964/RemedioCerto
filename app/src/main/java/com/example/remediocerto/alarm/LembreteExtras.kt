package com.example.remediocerto.alarm

import android.content.Intent

/** Quanto tempo a dose é adiada quando o medicamento não informa (e no teste da tela de alarme). */
const val ADIAMENTO_PADRAO_MIN = 10

/** Guarda os dados de um aviso dentro de um Intent (alarme, notificação, tela do alarme, botões). */
fun Intent.comLembrete(lembrete: Lembrete, adiamentoMin: Int? = null): Intent {
    putExtra(AlarmeScheduler.EXTRA_MEDICAMENTO_ID, lembrete.medicamentoId)
    putExtra(AlarmeScheduler.EXTRA_NOME, lembrete.nome)
    putExtra(AlarmeScheduler.EXTRA_DOSE, lembrete.dose)
    putExtra(AlarmeScheduler.EXTRA_HORA, lembrete.hora)
    putExtra(AlarmeScheduler.EXTRA_PREVISTO_EM, lembrete.previstoEm)
    putExtra(AlarmeScheduler.EXTRA_MINUTOS_ANTES, lembrete.minutosAntes)
    putExtra(AlarmeScheduler.EXTRA_ADIADO_PARA, lembrete.adiadoPara)
    if (adiamentoMin != null) putExtra(AlarmeScheduler.EXTRA_ADIAMENTO_MIN, adiamentoMin)
    return this
}

/** Lê de volta o que [comLembrete] guardou. Devolve null se o Intent não traz um medicamento. */
fun Intent.lerLembrete(): Lembrete? {
    val id = getStringExtra(AlarmeScheduler.EXTRA_MEDICAMENTO_ID).orEmpty()
    if (id.isBlank()) return null
    return Lembrete(
        medicamentoId = id,
        nome = getStringExtra(AlarmeScheduler.EXTRA_NOME).orEmpty(),
        dose = getStringExtra(AlarmeScheduler.EXTRA_DOSE).orEmpty(),
        hora = getStringExtra(AlarmeScheduler.EXTRA_HORA).orEmpty(),
        previstoEm = getLongExtra(AlarmeScheduler.EXTRA_PREVISTO_EM, 0L),
        minutosAntes = getIntExtra(AlarmeScheduler.EXTRA_MINUTOS_ANTES, 0),
        adiadoPara = getLongExtra(AlarmeScheduler.EXTRA_ADIADO_PARA, 0L),
    )
}

fun Intent.adiamentoMin(): Int = getIntExtra(AlarmeScheduler.EXTRA_ADIAMENTO_MIN, ADIAMENTO_PADRAO_MIN)
