package com.example.remediocerto.ui.acompanhamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.DoseDoDia
import com.example.remediocerto.data.model.Vinculo
import com.example.remediocerto.ui.hoje.montarDosesDoDia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Tela do familiar: as doses de hoje do idoso escolhido (o primeiro, se acompanhar mais de um). */
@OptIn(ExperimentalCoroutinesApi::class)
class AcompanhamentoViewModel : ViewModel() {

    private val dados = Repositorios.acompanhamento

    /** Todos os idosos que este familiar acompanha. Null enquanto a primeira leitura não chegou. */
    val idosos: StateFlow<List<Vinculo>?> = Repositorios.vinculos.observarDoFamiliar()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val escolhido = MutableStateFlow<String?>(null)

    /** O idoso que está na tela: o escolhido ou, se ainda não escolheu (ou o vínculo acabou), o primeiro. */
    val idosoAtual: StateFlow<Vinculo?> = combine(idosos, escolhido) { lista, id ->
        lista?.firstOrNull { it.idosoUid == id } ?: lista?.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val relogio = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    val dosesDeHoje: StateFlow<List<DoseDoDia>> = idosoAtual
        .map { it?.idosoUid }
        .distinctUntilChanged() // só troca de leitura quando o idoso muda, não a cada atualização da lista
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                combine(
                    dados.medicamentosDe(uid).catch { emit(emptyList()) },
                    dados.registrosDe(uid).catch { emit(emptyList()) },
                    relogio,
                ) { meds, registros, agora -> montarDosesDoDia(meds, registros, agora) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun escolher(idosoUid: String) {
        escolhido.value = idosoUid
    }
}
