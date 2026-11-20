package com.example.remediocerto.ui.hoje

import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.util.diasEntre
import com.example.remediocerto.util.inicioDoDia
import com.example.remediocerto.util.somarDias

/** Quantos dias, a partir de amanhã, a tela "Próximos dias" mostra no mínimo (mesmo sem nenhum tratamento com fim). */
const val DIAS_PROXIMOS = 7

/** Limite de segurança: tratamentos muito longos não deixam a lista com centenas de dias. */
const val DIAS_PROXIMOS_MAXIMO = 90

enum class SecaoProximos { TERMINAM, DIA_A_DIA }

/** Linha da lista "Próximos dias". Os dias são sempre a meia-noite local (como em [inicioDoDia]). */
sealed interface ItemProximos {
    data class Secao(val secao: SecaoProximos) : ItemProximos

    /**
     * Remédio com data para acabar: [faltam] é quantos dias faltam a partir de hoje (0 = termina hoje).
     * [comecaEm] só vem preenchido se o tratamento ainda não começou (início em um dia futuro).
     */
    data class Termino(
        val medicamentoId: String,
        val nome: String,
        val fim: Long,
        val faltam: Int,
        val comecaEm: Long? = null,
    ) : ItemProximos

    data class Dia(val dia: Long) : ItemProximos

    /** Uma dose futura. [ultimoDia] avisa que depois dela o tratamento acaba. */
    data class Dose(
        val medicamentoId: String,
        val previstoEm: Long,
        val hora: String,
        val nome: String,
        val dose: String,
        val ultimoDia: Boolean,
    ) : ItemProximos

    data class SemDoses(val dia: Long) : ItemProximos
}

/**
 * Monta a lista da tela "Próximos dias":
 *  1. primeiro, os tratamentos que têm data para acabar (do que acaba antes para o que acaba depois);
 *  2. depois, dia a dia a partir de amanhã, com os remédios e horários de cada dia. A lista vai até o dia em
 *     que termina o último tratamento (no mínimo [diasMinimos] dias e no máximo [DIAS_PROXIMOS_MAXIMO]).
 * Respeita "a cada N dias" e o fim do tratamento, igual à tela Hoje. Função pura, fácil de testar.
 */
fun montarProximosDias(
    medicamentos: List<Medicamento>,
    agora: Long,
    diasMinimos: Int = DIAS_PROXIMOS,
): List<ItemProximos> {
    if (medicamentos.isEmpty()) return emptyList()
    val hoje = inicioDoDia(agora)
    val itens = mutableListOf<ItemProximos>()

    val terminos = medicamentos
        .mapNotNull { m ->
            val fim = fimDoTratamento(m) ?: return@mapNotNull null
            if (fim < hoje) return@mapNotNull null
            val comeca = m.inicioEm.takeIf { it > hoje }
            ItemProximos.Termino(m.id, m.nome, fim, diasEntre(hoje, fim), comeca?.let(::inicioDoDia))
        }
        .sortedWith(compareBy({ it.fim }, { it.nome }))
    if (terminos.isNotEmpty()) {
        itens += ItemProximos.Secao(SecaoProximos.TERMINAM)
        itens += terminos
    }

    val dias = maxOf(diasMinimos, terminos.maxOfOrNull { it.faltam } ?: 0).coerceAtMost(DIAS_PROXIMOS_MAXIMO)
    itens += ItemProximos.Secao(SecaoProximos.DIA_A_DIA)
    for (d in 1..dias) {
        val dia = somarDias(hoje, d)
        itens += ItemProximos.Dia(dia)
        val doses = medicamentos
            .filter { valeHoje(it, dia) }
            .flatMap { m ->
                m.horarios.mapNotNull { horario ->
                    val previsto = previstoNoDia(horario, dia) ?: return@mapNotNull null
                    ItemProximos.Dose(m.id, previsto, horario, m.nome, m.dose, ehUltimoDia(m, dia))
                }
            }
            .sortedBy { it.previstoEm }
        if (doses.isEmpty()) itens += ItemProximos.SemDoses(dia) else itens += doses
    }
    return itens
}

/** Último dia do tratamento (meia-noite local), ou null se é uso contínuo ou o medicamento não tem data de início. */
fun fimDoTratamento(m: Medicamento): Long? =
    if (m.duracaoDias > 0 && m.inicioEm > 0L) somarDias(inicioDoDia(m.inicioEm), m.duracaoDias - 1) else null

// Depois deste dia, o próximo dia de tomar já cairia fora do tratamento.
private fun ehUltimoDia(m: Medicamento, dia: Long): Boolean {
    if (m.duracaoDias <= 0 || m.inicioEm <= 0L) return false
    return diasEntre(m.inicioEm, dia) + m.intervaloDias.coerceAtLeast(1) >= m.duracaoDias
}
