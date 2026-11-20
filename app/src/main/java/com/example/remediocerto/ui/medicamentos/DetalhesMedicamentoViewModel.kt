package com.example.remediocerto.ui.medicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.ResultadoBula
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** O que a área de bula da tela de Detalhes mostra. */
sealed interface EstadoBula {
    object Inicial : EstadoBula
    object Consultando : EstadoBula
    data class Pronta(val resultado: ResultadoBula.Sucesso) : EstadoBula
    data class Falha(val resultado: ResultadoBula.Falha) : EstadoBula
}

class DetalhesMedicamentoViewModel : ViewModel() {

    private val repositorio = Repositorios.medicamentos

    private val _bula = MutableStateFlow<EstadoBula>(EstadoBula.Inicial)
    val bula: StateFlow<EstadoBula> = _bula.asStateFlow()

    /** Emite o medicamento sempre que ele muda (null se não existir mais). */
    fun medicamento(id: String): Flow<Medicamento?> =
        repositorio.observar()
            .map { lista -> lista.firstOrNull { it.id == id } }
            .catch { emit(null) }

    fun apagar(id: String) = repositorio.apagar(id)

    /** Consulta a bula do medicamento. Ignora toques repetidos enquanto uma consulta está em andamento. */
    fun consultarBula(nomeMedicamento: String) {
        if (_bula.value is EstadoBula.Consultando) return
        _bula.value = EstadoBula.Consultando
        viewModelScope.launch {
            _bula.value = when (val resultado = Repositorios.bulario.consultar(nomeMedicamento)) {
                is ResultadoBula.Sucesso -> EstadoBula.Pronta(resultado)
                is ResultadoBula.Falha -> EstadoBula.Falha(resultado)
            }
        }
    }
}
