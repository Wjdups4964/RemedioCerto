package com.example.remediocerto.ui.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.RegistroDose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoricoViewModel : ViewModel() {

    private val registros: StateFlow<List<RegistroDose>> = Repositorios.doses.observar()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Doses agrupadas por dia, com um título antes de cada dia. */
    val itens: StateFlow<List<ItemHistorico>> = registros
        .map { agruparPorDia(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Resumo dos últimos 7 dias; null enquanto não há doses nesse período. */
    val resumo: StateFlow<ResumoSemana?> = registros
        .map { calcularResumoSemana(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
