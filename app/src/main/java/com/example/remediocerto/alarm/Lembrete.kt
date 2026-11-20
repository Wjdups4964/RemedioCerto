package com.example.remediocerto.alarm

import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.ui.hoje.previstoNoDia
import com.example.remediocerto.ui.hoje.valeHoje
import com.example.remediocerto.util.inicioDoDia
import com.example.remediocerto.util.somarDias

/** Quantos minutos antes da dose o app avisa. O 0 é o aviso na hora de tomar. Para mudar os avisos, é só mexer aqui. */
val AVISOS_ANTES_DA_DOSE = listOf(10, 5, 0)

/** Quantos dias à frente (contando hoje) os avisos ficam agendados. Eles são renovados a cada aviso e a cada vez que o app abre. */
const val DIAS_A_FRENTE = 2

/** Um aviso de uma dose: "daqui a [minutosAntes] minutos você deve tomar [nome]". */
data class Lembrete(
    val medicamentoId: String,
    val nome: String,
    val dose: String,
    val hora: String,
    val previstoEm: Long,
    val minutosAntes: Int,
) {
    /** Instante em que a notificação deve aparecer. */
    val disparaEm: Long get() = previstoEm - minutosAntes * 60_000L

    /** Identifica este aviso no AlarmManager (um por medicamento, dose e antecedência). */
    val codigo: Int get() = "$medicamentoId|$previstoEm|$minutosAntes".hashCode()

    /** Os três avisos da mesma dose usam a mesma notificação, então o novo troca o anterior em vez de empilhar. */
    val idNotificacao: Int get() = "$medicamentoId|$previstoEm".hashCode()
}

/**
 * Todos os avisos que ainda vão acontecer nos próximos [diasAFrente] dias, do mais próximo ao mais distante.
 * Respeita os mesmos dias do tratamento da tela Hoje ("a cada N dias" e fim do tratamento).
 * Função pura, fácil de testar.
 */
fun proximosLembretes(
    medicamentos: List<Medicamento>,
    agora: Long,
    diasAFrente: Int = DIAS_A_FRENTE,
    minutosAntes: List<Int> = AVISOS_ANTES_DA_DOSE,
): List<Lembrete> {
    val hoje = inicioDoDia(agora)
    val lembretes = mutableListOf<Lembrete>()
    for (d in 0 until diasAFrente) {
        val dia = somarDias(hoje, d)
        for (m in medicamentos) {
            if (!valeHoje(m, dia)) continue
            for (horario in m.horarios) {
                val previsto = previstoNoDia(horario, dia) ?: continue
                for (min in minutosAntes) {
                    val lembrete = Lembrete(m.id, m.nome, m.dose, horario, previsto, min)
                    if (lembrete.disparaEm > agora) lembretes += lembrete
                }
            }
        }
    }
    return lembretes.sortedBy { it.disparaEm }
}
