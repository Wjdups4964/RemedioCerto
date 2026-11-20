package com.example.remediocerto.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar

/*
 * Datas do tratamento. Tudo aqui trabalha com "meia-noite do dia, no fuso do aparelho" (milissegundos),
 * que é como o medicamento guarda o início. O seletor de datas do Material usa meia-noite UTC, então
 * existem duas funções para converter de um formato para o outro.
 */

private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val FORMATO_DIA_MES = DateTimeFormatter.ofPattern("dd/MM")

/** Soma (ou subtrai, se negativo) dias de calendário, mantendo a meia-noite local mesmo na troca do horário de verão. */
fun somarDias(dia: Long, dias: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = dia
        add(Calendar.DAY_OF_YEAR, dias)
    }.timeInMillis

/** "20/11/2026". */
fun formatarDiaLocal(dia: Long): String =
    FORMATO_DATA.format(Instant.ofEpochMilli(dia).atZone(ZoneId.systemDefault()))

/** "20/11", para listas curtas. */
fun formatarDiaMes(dia: Long): String =
    FORMATO_DIA_MES.format(Instant.ofEpochMilli(dia).atZone(ZoneId.systemDefault()))

/** Data escolhida no seletor (meia-noite UTC) -> meia-noite do mesmo dia no fuso do aparelho. */
fun utcParaLocal(diaUtc: Long): Long =
    Instant.ofEpochMilli(diaUtc).atZone(ZoneOffset.UTC).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Meia-noite local -> meia-noite UTC do mesmo dia, para marcar a data no seletor. */
fun localParaUtc(diaLocal: Long): Long =
    Instant.ofEpochMilli(diaLocal).atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * Os próximos dias em que o remédio deve ser tomado, a partir do início e de quantos em quantos dias.
 * Se [fim] for maior que zero, não passa do último dia do tratamento.
 * Exemplo: início 20/11, a cada 7 dias -> 20/11, 27/11, 04/12, 11/12.
 */
fun diasDeTomar(inicio: Long, intervaloDias: Int, fim: Long, quantidade: Int = 4): List<Long> {
    if (intervaloDias < 1 || quantidade < 1) return emptyList()
    return generateSequence(0) { it + 1 }
        .map { somarDias(inicio, it * intervaloDias) }
        .takeWhile { fim <= 0L || it <= fim }
        .take(quantidade)
        .toList()
}
