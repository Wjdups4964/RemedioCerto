package com.example.remediocerto.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.DadosPessoais
import com.example.remediocerto.data.model.Sexo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/** "Meu perfil": lê e grava os dados pessoais. Só o nome é obrigatório. */
class PerfilViewModel : ViewModel() {

    private val repositorio = Repositorios.dadosPessoais

    val dados: StateFlow<DadosPessoais?> = repositorio.observar()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Data escolhida no seletor, ainda não salva.
    private val _nascimento = MutableStateFlow(0L)
    val nascimento: StateFlow<Long> = _nascimento.asStateFlow()

    /** A tela marca true depois de preencher os campos, para não sobrescrever o que o usuário já digitou. */
    var formularioPreenchido = false

    fun definirNascimento(utcMs: Long) { _nascimento.value = utcMs }

    /** Devolve false (sem gravar) se o nome estiver vazio. */
    fun salvar(nome: String, sexo: Sexo, contatoEmergencia: String): Boolean {
        if (nome.isBlank()) return false
        repositorio.salvar(
            DadosPessoais(
                nome = nome.trim(),
                nascimentoEm = _nascimento.value,
                sexo = sexo,
                contatoEmergencia = contatoEmergencia.trim(),
            )
        )
        return true
    }
}
