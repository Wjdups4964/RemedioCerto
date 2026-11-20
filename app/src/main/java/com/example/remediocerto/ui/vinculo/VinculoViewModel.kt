package com.example.remediocerto.ui.vinculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Convite
import com.example.remediocerto.data.model.MotivoFalhaVinculo
import com.example.remediocerto.data.model.ResultadoVinculo
import com.example.remediocerto.data.model.Vinculo
import com.example.remediocerto.data.model.normalizarCodigo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Avisos pontuais para a tela mostrar (cada um aparece uma vez). */
sealed interface EventoVinculo {
    data object CodigoGerado : EventoVinculo
    data object ErroAoGerar : EventoVinculo
    data object Vinculado : EventoVinculo
    data class FalhaAoVincular(val motivo: MotivoFalhaVinculo) : EventoVinculo
    data class Encerrado(val confirmado: Boolean) : EventoVinculo
}

/**
 * Tela de Vínculo. O idoso gera o código e vê quem o acompanha; o familiar digita o código
 * e vê quem ele acompanha. Qual dos dois vale é decidido pelo perfil de quem está logado.
 */
class VinculoViewModel : ViewModel() {

    private val vinculos = Repositorios.vinculos

    private val _convite = MutableStateFlow<Convite?>(null)
    val convite: StateFlow<Convite?> = _convite

    private val _ocupado = MutableStateFlow(false)
    val ocupado: StateFlow<Boolean> = _ocupado

    private val _eventos = Channel<EventoVinculo>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    /** Familiares que acompanham o idoso (usado quando o perfil é idoso). */
    val doIdoso: StateFlow<List<Vinculo>> = vinculos.observarDoIdoso()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Idosos que o familiar acompanha (usado quando o perfil é familiar). */
    val doFamiliar: StateFlow<List<Vinculo>> = vinculos.observarDoFamiliar()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun gerarCodigo() {
        if (_ocupado.value) return
        viewModelScope.launch {
            _ocupado.value = true
            val novo = vinculos.gerarConvite(nomeDoUsuario(padrao = "Idoso"))
            _ocupado.value = false
            if (novo != null) {
                _convite.value = novo
                _eventos.send(EventoVinculo.CodigoGerado)
            } else {
                _eventos.send(EventoVinculo.ErroAoGerar)
            }
        }
    }

    fun vincular(textoDigitado: String) {
        if (_ocupado.value) return
        viewModelScope.launch {
            _ocupado.value = true
            val resultado = vinculos.usarConvite(normalizarCodigo(textoDigitado), nomeDoUsuario(padrao = "Familiar"))
            _ocupado.value = false
            _eventos.send(
                when (resultado) {
                    is ResultadoVinculo.Sucesso -> EventoVinculo.Vinculado
                    is ResultadoVinculo.Falha -> EventoVinculo.FalhaAoVincular(resultado.motivo)
                }
            )
        }
    }

    fun encerrar(vinculo: Vinculo) {
        viewModelScope.launch {
            _eventos.send(EventoVinculo.Encerrado(vinculos.encerrar(vinculo.id)))
        }
    }

    // O nome é copiado para o vínculo, então a outra pessoa se reconhece na lista.
    private suspend fun nomeDoUsuario(padrao: String): String =
        Repositorios.dadosPessoais.observar().catch { emit(null) }.firstOrNull()?.nome?.trim()
            ?.takeIf { it.isNotEmpty() } ?: padrao
}
