package com.example.remediocerto.ui.hoje

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.DoseDoDia
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela Hoje: doses do dia (medicamentos + registros + relógio) e as ações Tomei/Adiar. */
class HojeViewModel : ViewModel() {

    private val medicamentos = Repositorios.medicamentos
    private val doses = Repositorios.doses

    // Dá um "tique" por minuto para a lista passar de "Próxima" para "Hora de tomar" e "Atrasada" sozinha.
    private val relogio = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    val dosesDeHoje: StateFlow<List<DoseDoDia>> = combine(
        medicamentos.observar().catch { emit(emptyList()) },
        doses.observar().catch { emit(emptyList()) },
        relogio,
    ) { meds, registros, agora -> montarDosesDoDia(meds, registros, agora) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Primeiro nome do usuário para o cumprimento ("Olá, Maria"); vazio enquanto não houver nome. */
    val primeiroNome: StateFlow<String> = Repositorios.dadosPessoais.observar()
        .map { it?.nome?.trim()?.substringBefore(" ").orEmpty() }
        .catch { emit("") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /** Devolve false (e não grava nada) se ainda não é a hora da dose. */
    fun tomei(dose: DoseDoDia): Boolean = registrar(dose, StatusDose.TOMADA)

    /** Devolve false (e não grava nada) se ainda não é a hora da dose. */
    fun adiar(dose: DoseDoDia): Boolean = registrar(dose, StatusDose.ADIADA)

    // A regra vale aqui e não só nos botões da tela: a hora é conferida de novo no momento do toque.
    // Grava no cache do celular na hora (funciona offline) e o Firestore sincroniza depois.
    private fun registrar(dose: DoseDoDia, status: StatusDose): Boolean {
        val agora = System.currentTimeMillis()
        if (agora < dose.previstoEm) return false
        doses.registrar(
            RegistroDose(
                medicamentoId = dose.medicamentoId,
                medicamentoNome = dose.nome,
                previstoEm = dose.previstoEm,
                registradoEm = agora,
                status = status,
            )
        )
        return true
    }
}
