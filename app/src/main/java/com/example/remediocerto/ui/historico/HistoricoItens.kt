package com.example.remediocerto.ui.historico

import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import com.example.remediocerto.ui.hoje.previstoNoDia
import com.example.remediocerto.ui.hoje.valeHoje
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Quantos dias para trás o histórico procura doses que ninguém registrou (hoje e os 13 anteriores). */
const val DIAS_HISTORICO = 14

// Até 30 minutos depois do horário a dose ainda é "hora de tomar"; depois disso, "atrasada".
private const val TOLERANCIA_MS = 30 * 60 * 1000L

/** Como uma dose terminou, do ponto de vista de quem olha o histórico. */
enum class SituacaoDose {
    TOMADA,             // tomada no horário (ou até 30 minutos depois)
    TOMADA_COM_ATRASO,  // tomada, mas mais de 30 minutos depois do horário
    ADIADA,             // hoje: a pessoa adiou e ainda está dentro da tolerância
    ATRASADA,           // hoje: passou do horário e ainda não foi tomada (dá tempo de tomar)
    NAO_TOMADA,         // o dia já passou sem tomar, ou foi marcada como perdida
}

/** Uma dose que aparece no histórico: prevista, com o que aconteceu com ela. */
data class DoseHistorico(
    val medicamentoId: String,
    val nome: String,
    val previstoEm: Long,
    val registradoEm: Long?, // quando a pessoa tocou em Tomei/Adiar; null se nunca registrou
    val situacao: SituacaoDose,
) {
    val chave: String get() = "${medicamentoId}_$previstoEm"
}

/** Linha da lista do histórico: o título de um dia, ou uma dose daquele dia. */
sealed interface ItemHistorico {
    data class Dia(val dia: LocalDate) : ItemHistorico
    data class Dose(val dose: DoseHistorico) : ItemHistorico
}

/**
 * Junta o que estava previsto (medicamentos + horários, dos últimos [dias] dias) com o que foi registrado.
 * Dose prevista e sem registro vira "atrasada" (hoje) ou "não tomada" (dias anteriores): assim o histórico
 * não mostra só o que deu certo. Doses que ainda vão chegar, ou que estão na hora, não entram.
 *
 * Registros sem dose prevista correspondente (remédio apagado ou horário alterado) continuam aparecendo.
 * Medicamentos sem data de início (cadastro antigo) não geram doses previstas, porque não dá para saber
 * desde quando valem.
 */
fun montarHistorico(
    medicamentos: List<Medicamento>,
    registros: List<RegistroDose>,
    agora: Long,
    dias: Int = DIAS_HISTORICO,
): List<DoseHistorico> {
    val zona = ZoneId.systemDefault()
    val hoje = Instant.ofEpochMilli(agora).atZone(zona).toLocalDate()
    // Se houver mais de um registro para a mesma dose (adiou e depois tomou), vale o mais recente.
    val porDose = registros.groupBy { it.medicamentoId to it.previstoEm }
        .mapValues { (_, lista) -> lista.maxBy { it.registradoEm } }

    val doses = mutableListOf<DoseHistorico>()
    val usados = mutableSetOf<Pair<String, Long>>()

    for (m in medicamentos) {
        if (m.inicioEm <= 0L) continue
        for (i in 0 until dias) {
            val dia = hoje.minusDays(i.toLong())
            val referencia = dia.atTime(12, 0).atZone(zona).toInstant().toEpochMilli()
            if (!valeHoje(m, referencia)) continue
            for (horario in m.horarios) {
                val previsto = previstoNoDia(horario, referencia) ?: continue
                val registro = porDose[m.id to previsto]
                if (registro != null) usados += m.id to previsto
                val situacao = situacaoDe(registro, previsto, agora, dia == hoje) ?: continue
                doses += DoseHistorico(m.id, m.nome, previsto, registro?.registradoEm, situacao)
            }
        }
    }

    for ((chave, registro) in porDose) {
        if (chave in usados) continue
        val dia = Instant.ofEpochMilli(registro.previstoEm).atZone(zona).toLocalDate()
        val situacao = situacaoDe(registro, registro.previstoEm, agora, dia == hoje) ?: continue
        doses += DoseHistorico(registro.medicamentoId, registro.medicamentoNome, registro.previstoEm, registro.registradoEm, situacao)
    }
    return doses
}

/** null = dose que ainda não deve aparecer no histórico (futura ou na hora de tomar, sem registro). */
private fun situacaoDe(registro: RegistroDose?, previsto: Long, agora: Long, ehHoje: Boolean): SituacaoDose? {
    val atraso = agora - previsto
    if (registro == null) {
        return when {
            atraso <= TOLERANCIA_MS -> null
            ehHoje -> SituacaoDose.ATRASADA
            else -> SituacaoDose.NAO_TOMADA
        }
    }
    return when (registro.status) {
        StatusDose.TOMADA ->
            if (registro.registradoEm - previsto > TOLERANCIA_MS) SituacaoDose.TOMADA_COM_ATRASO else SituacaoDose.TOMADA
        StatusDose.ADIADA -> when {
            !ehHoje -> SituacaoDose.NAO_TOMADA
            atraso <= TOLERANCIA_MS -> SituacaoDose.ADIADA
            else -> SituacaoDose.ATRASADA
        }
        StatusDose.PERDIDA -> SituacaoDose.NAO_TOMADA
    }
}

/** Mais recentes primeiro, com um título antes das doses de cada dia. */
fun agruparPorDia(doses: List<DoseHistorico>, zona: ZoneId = ZoneId.systemDefault()): List<ItemHistorico> {
    val itens = mutableListOf<ItemHistorico>()
    var diaAtual: LocalDate? = null
    for (dose in doses.sortedByDescending { it.previstoEm }) {
        val dia = Instant.ofEpochMilli(dose.previstoEm).atZone(zona).toLocalDate()
        if (dia != diaAtual) {
            itens += ItemHistorico.Dia(dia)
            diaAtual = dia
        }
        itens += ItemHistorico.Dose(dose)
    }
    return itens
}

/** Doses dos últimos 7 dias (hoje e os 6 anteriores): quantas foram tomadas, não tomadas e atrasadas. */
data class ResumoSemana(val tomadas: Int, val total: Int, val naoTomadas: Int = 0, val atrasadas: Int = 0)

/** Devolve null quando não há nenhuma dose nos últimos 7 dias. */
fun calcularResumoSemana(
    doses: List<DoseHistorico>,
    agora: Long = System.currentTimeMillis(),
    zona: ZoneId = ZoneId.systemDefault(),
): ResumoSemana? {
    val hoje = Instant.ofEpochMilli(agora).atZone(zona).toLocalDate()
    val inicio = hoje.minusDays(6).atStartOfDay(zona).toInstant().toEpochMilli()
    val fim = hoje.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()
    val daSemana = doses.filter { it.previstoEm in inicio until fim }
    if (daSemana.isEmpty()) return null
    return ResumoSemana(
        tomadas = daSemana.count { it.situacao == SituacaoDose.TOMADA || it.situacao == SituacaoDose.TOMADA_COM_ATRASO },
        total = daSemana.size,
        naoTomadas = daSemana.count { it.situacao == SituacaoDose.NAO_TOMADA },
        atrasadas = daSemana.count { it.situacao == SituacaoDose.ATRASADA },
    )
}
