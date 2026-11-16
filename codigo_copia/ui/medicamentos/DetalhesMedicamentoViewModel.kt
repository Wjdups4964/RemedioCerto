package com.example.remediocerto.ui.medicamentos

import androidx.lifecycle.ViewModel
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Medicamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DetalhesMedicamentoViewModel : ViewModel() {

    private val repositorio = Repositorios.medicamentos

    /** Emite o medicamento sempre que ele muda (null se não existir mais). */
    fun medicamento(id: String): Flow<Medicamento?> =
        repositorio.observar()
            .map { lista -> lista.firstOrNull { it.id == id } }
            .catch { emit(null) }

    fun apagar(id: String) = repositorio.apagar(id)
}
