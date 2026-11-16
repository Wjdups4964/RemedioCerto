package com.example.remediocerto.ui.medicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Medicamento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

/** Lista de medicamentos do usuário, sempre atualizada (inclusive quando algo é salvo offline). */
class MedicamentosViewModel : ViewModel() {

    val medicamentos: StateFlow<List<Medicamento>> = Repositorios.medicamentos.observar()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
