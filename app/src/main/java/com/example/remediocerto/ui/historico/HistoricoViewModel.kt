package com.example.remediocerto.ui.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoricoViewModel : ViewModel() {

    // O relógio faz uma dose passar de "na hora" para "atrasada" sem precisar reabrir a tela.
    private val relogio = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    /** Previsto (medicamentos e horários) + registrado (Tomei, Adiar): o que foi tomado e o que ficou para trás. */
    private val doses: StateFlow<List<DoseHistorico>> = combine(
        Repositorios.medicamentos.observar().catch { emit(emptyList()) },
        Repositorios.doses.observar().catch { emit(emptyList()) },
        relogio,
    ) { medicamentos, registros, agora -> montarHistorico(medicamentos, registros, agora) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Doses agrupadas por dia, com um título antes de cada dia. */
    val itens: StateFlow<List<ItemHistorico>> = doses
        .map { agruparPorDia(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Resumo dos últimos 7 dias; null enquanto não há doses nesse período. */
    val resumo: StateFlow<ResumoSemana?> = doses
        .map { calcularResumoSemana(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
