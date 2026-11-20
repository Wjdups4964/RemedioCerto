package com.example.remediocerto.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodoTest {

    private fun dia(ano: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(ano, mes - 1, dia, 0, 0, 0)
        }.timeInMillis

    @Test
    fun `semanal lista um dia por semana`() {
        val dias = diasDeTomar(dia(2026, 11, 20), intervaloDias = 7, fim = 0, quantidade = 3)
        assertEquals(listOf("20/11", "27/11", "04/12"), dias.map(::formatarDiaMes))
    }

    @Test
    fun `nao passa do ultimo dia do tratamento`() {
        // 20/11 a 27/11, a cada 3 dias: 20, 23, 26 (29 já passou do fim)
        val dias = diasDeTomar(dia(2026, 11, 20), intervaloDias = 3, fim = dia(2026, 11, 27))
        assertEquals(listOf("20/11", "23/11", "26/11"), dias.map(::formatarDiaMes))
    }

    @Test
    fun `o dia do fim conta`() {
        val dias = diasDeTomar(dia(2026, 11, 20), intervaloDias = 1, fim = dia(2026, 11, 22))
        assertEquals(3, dias.size)
    }

    @Test
    fun `intervalo invalido nao gera dias`() {
        assertEquals(emptyList<Long>(), diasDeTomar(dia(2026, 11, 20), intervaloDias = 0, fim = 0))
    }

    @Test
    fun `converter entre utc e local nao muda o dia`() {
        val meiaNoiteUtc = 1_795_132_800_000L // 20/11/2026 00:00 UTC
        assertEquals(meiaNoiteUtc, localParaUtc(utcParaLocal(meiaNoiteUtc)))
        assertEquals("20/11/2026", formatarDiaLocal(utcParaLocal(meiaNoiteUtc)))
    }

    @Test
    fun `somar dias mantem a meia noite`() {
        val inicio = dia(2026, 11, 20)
        assertEquals(dia(2026, 11, 27), somarDias(inicio, 7))
        assertEquals(dia(2026, 11, 13), somarDias(inicio, -7))
    }
}
