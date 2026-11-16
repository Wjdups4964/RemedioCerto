package com.example.remediocerto.ui.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

class ConfiguracoesViewModel : ViewModel() {

    private val repositorio = Repositorios.configuracao

    /** Estado salvo da chave de acessibilidade (o efeito visual no app é da entrega de 30/11). */
    val acessibilidade: StateFlow<Boolean> = repositorio.observarAcessibilidade()
        .catch { emit(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun alterarAcessibilidade(ativo: Boolean) = repositorio.salvarAcessibilidade(ativo)
}
