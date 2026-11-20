package com.example.remediocerto.ui.historico

import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Linha da lista do histórico: o título de um dia, ou uma dose daquele dia. */
sealed interface ItemHistorico {
    data class Dia(val dia: LocalDate) : ItemHistorico
    data class Dose(val registro: RegistroDose) : ItemHistorico
}

/** Mais recentes primeiro, com um título antes das doses de cada dia. */
fun agruparPorDia(registros: List<RegistroDose>, zona: ZoneId = ZoneId.systemDefault()): List<ItemHistorico> {
    val itens = mutableListOf<ItemHistorico>()
    var diaAtual: LocalDate? = null
    for (registro in registros.sortedByDescending { it.previstoEm }) {
        val dia = Instant.ofEpochMilli(registro.previstoEm).atZone(zona).toLocalDate()
        if (dia != diaAtual) {
            itens += ItemHistorico.Dia(dia)
            diaAtual = dia
        }
        itens += ItemHistorico.Dose(registro)
    }
    return itens
}

/** Doses tomadas e doses registradas nos últimos 7 dias (hoje e os 6 anteriores). */
data class ResumoSemana(val tomadas: Int, val total: Int)

/** Devolve null quando não há nenhuma dose registrada nos últimos 7 dias. */
fun calcularResumoSemana(
    registros: List<RegistroDose>,
    agora: Long = System.currentTimeMillis(),
    zona: ZoneId = ZoneId.systemDefault(),
): ResumoSemana? {
    val hoje = Instant.ofEpochMilli(agora).atZone(zona).toLocalDate()
    val inicio = hoje.minusDays(6).atStartOfDay(zona).toInstant().toEpochMilli()
    val fim = hoje.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
    val daSemana = registros.filter { it.previstoEm in inicio until fim }
    if (daSemana.isEmpty()) return null
    return ResumoSemana(daSemana.count { it.status == StatusDose.TOMADA }, daSemana.size)
}
