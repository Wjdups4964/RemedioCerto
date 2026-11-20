package com.example.remediocerto.ui.hoje

import com.example.remediocerto.data.model.DoseDoDia
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.Momento
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.util.diasEntre
import java.util.Calendar

// Até 30 minutos depois do horário a dose ainda é "hora de tomar"; depois disso, "atrasada".
private const val TOLERANCIA_MS = 30 * 60 * 1000L

/**
 * Monta a lista de hoje: cada horário de cada medicamento que vale para hoje vira uma dose, ligada ao
 * registro correspondente (se o usuário já tocou em Tomei/Adiar). Função pura, fácil de testar.
 */
fun montarDosesDoDia(
    medicamentos: List<Medicamento>,
    registros: List<RegistroDose>,
    agora: Long,
): List<DoseDoDia> {
    val porDose = registros.associateBy { it.medicamentoId to it.previstoEm }
    return medicamentos.filter { valeHoje(it, agora) }.flatMap { m ->
        m.horarios.mapNotNull { horario ->
            val previsto = previstoNoDia(horario, agora) ?: return@mapNotNull null
            val momento = when {
                previsto > agora -> Momento.FUTURA
                agora - previsto <= TOLERANCIA_MS -> Momento.AGORA
                else -> Momento.ATRASADA
            }
            DoseDoDia(
                medicamentoId = m.id,
                nome = m.nome,
                dose = m.dose,
                hora = horario,
                previstoEm = previsto,
                adiamentoMin = m.adiamentoMin,
                status = porDose[m.id to previsto]?.status,
                momento = momento,
            )
        }
    }.sortedBy { it.previstoEm }
}

/**
 * Hoje é dia de tomar? Conta os dias desde o início do tratamento: respeita "a cada N dias"
 * e o fim do tratamento ("por N dias"). Sem data de início (medicamento antigo), vale todo dia.
 */
fun valeHoje(m: Medicamento, agora: Long): Boolean {
    if (m.inicioEm <= 0L) return true
    val dias = diasEntre(m.inicioEm, agora)
    if (dias < 0) return false
    if (m.duracaoDias > 0 && dias >= m.duracaoDias) return false
    return dias % m.intervaloDias.coerceAtLeast(1) == 0
}

internal fun previstoNoDia(horario: String, agora: Long): Long? {
    val partes = horario.split(":")
    val hora = partes.getOrNull(0)?.toIntOrNull() ?: return null
    val minuto = partes.getOrNull(1)?.toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        timeInMillis = agora
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
