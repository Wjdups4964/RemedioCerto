package com.example.remediocerto.ui.historico

import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoricoItensTest {

    private val zona = ZoneId.of("America/Sao_Paulo")

    private fun ms(ano: Int, mes: Int, dia: Int, hora: Int = 8) =
        LocalDateTime.of(ano, mes, dia, hora, 0).atZone(zona).toInstant().toEpochMilli()

    private fun dose(id: String, previstoEm: Long, status: StatusDose = StatusDose.TOMADA) =
        RegistroDose(id = id, medicamentoNome = "Losartana", previstoEm = previstoEm, status = status)

    @Test
    fun `agrupa por dia, do mais recente para o mais antigo`() {
        val itens = agruparPorDia(
            listOf(
                dose("a", ms(2026, 11, 19, 20)),
                dose("b", ms(2026, 11, 20, 8)),
                dose("c", ms(2026, 11, 19, 8)),
            ),
            zona,
        )
        assertEquals(
            listOf("dia 2026-11-20", "b", "dia 2026-11-19", "a", "c"),
            itens.map { if (it is ItemHistorico.Dia) "dia ${it.dia}" else (it as ItemHistorico.Dose).registro.id },
        )
    }

    @Test
    fun `lista vazia nao gera titulos`() {
        assertEquals(emptyList<ItemHistorico>(), agruparPorDia(emptyList(), zona))
    }

    @Test
    fun `resumo conta so os ultimos 7 dias`() {
        val agora = ms(2026, 11, 20, 12)
        val resumo = calcularResumoSemana(
            listOf(
                dose("hoje", ms(2026, 11, 20, 8)),                         // dentro, tomada
                dose("ontem", ms(2026, 11, 19, 20), StatusDose.PERDIDA),   // dentro, perdida
                dose("limite", ms(2026, 11, 14, 8)),                       // 6 dias atrás: ainda dentro
                dose("fora", ms(2026, 11, 13, 8)),                         // 7 dias atrás: fora
            ),
            agora,
            zona,
        )
        assertEquals(ResumoSemana(tomadas = 2, total = 3), resumo)
    }

    @Test
    fun `resumo e nulo sem doses na semana`() {
        val agora = ms(2026, 11, 20, 12)
        assertNull(calcularResumoSemana(listOf(dose("antiga", ms(2026, 10, 1))), agora, zona))
        assertNull(calcularResumoSemana(emptyList(), agora, zona))
    }

    @Test
    fun `dias diferentes nao se misturam perto da meia-noite`() {
        val itens = agruparPorDia(listOf(dose("a", ms(2026, 11, 20, 0)), dose("b", ms(2026, 11, 19, 23))), zona)
        assertEquals(
            listOf(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 19)),
            itens.filterIsInstance<ItemHistorico.Dia>().map { it.dia },
        )
    }
}
