package com.example.remediocerto.util

import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val DIA_MS = 24 * 60 * 60 * 1000.0

/** Meia-noite do dia a que o instante [instante] pertence, no fuso do aparelho. */
fun inicioDoDia(instante: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = instante
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/** Quantos dias de calendário separam [desde] de [ate] (0 = mesmo dia, negativo = [ate] é antes). */
fun diasEntre(desde: Long, ate: Long): Int =
    ((inicioDoDia(ate) - inicioDoDia(desde)) / DIA_MS).roundToInt()

/**
 * Idade em anos completos a partir de uma data de nascimento guardada como meia-noite UTC.
 * Devolve null se a data não foi informada (0) ou está no futuro.
 */
fun idadeEm(nascimentoUtcMs: Long): Int? {
    if (nascimentoUtcMs <= 0L) return null
    val nascimento = Instant.ofEpochMilli(nascimentoUtcMs).atZone(ZoneOffset.UTC).toLocalDate()
    val hoje = LocalDate.now()
    if (nascimento.isAfter(hoje)) return null
    return Period.between(nascimento, hoje).years
}

/** "dd/MM/aaaa" para uma data guardada como meia-noite UTC. */
fun formatarData(nascimentoUtcMs: Long): String {
    val d = Instant.ofEpochMilli(nascimentoUtcMs).atZone(ZoneOffset.UTC).toLocalDate()
    return String.format(Locale.US, "%02d/%02d/%04d", d.dayOfMonth, d.monthValue, d.year)
}
