package com.example.remediocerto.ui.hoje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** Estado da tela "Próximos dias": acompanha os medicamentos e vira o dia sozinho (relógio de 1 minuto). */
class ProximosDiasViewModel : ViewModel() {

    private val relogio = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    val itens: StateFlow<List<ItemProximos>> = combine(
        Repositorios.medicamentos.observar().catch { emit(emptyList()) },
        relogio,
    ) { medicamentos, agora -> montarProximosDias(medicamentos, agora) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
