package com.example.remediocerto.ui.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Adesão = doses tomadas ÷ doses registradas, em %. Adiadas e perdidas contam como não tomadas.
 * Devolve null enquanto não há nenhum registro (a tela mostra um aviso em vez de "0%").
 */
fun calcularAdesao(registros: List<RegistroDose>): Int? {
    if (registros.isEmpty()) return null
    val tomadas = registros.count { it.status == StatusDose.TOMADA }
    return tomadas * 100 / registros.size
}

class HistoricoViewModel : ViewModel() {

    val registros: StateFlow<List<RegistroDose>> = Repositorios.doses.observar()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val adesao: StateFlow<Int?> = registros
        .map { calcularAdesao(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
