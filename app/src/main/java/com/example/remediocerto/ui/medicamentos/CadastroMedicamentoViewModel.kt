package com.example.remediocerto.ui.medicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.TipoFrequencia
import com.example.remediocerto.util.inicioDoDia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/** Resultado da validação ao salvar: a tela decide a mensagem de cada caso. */
enum class Validacao { OK, SEM_NOME, SEM_HORARIO, DIAS_INVALIDOS, DURACAO_INVALIDA }

/**
 * Gera os horários de um dia a partir da primeira dose e do intervalo: 06:00 de 8 em 8 horas
 * vira 06:00, 14:00 e 22:00. O intervalo precisa dividir 24 (2, 3, 4, 6, 8 ou 12) para o dia
 * seguinte repetir os mesmos horários.
 */
fun gerarHorarios(primeiraDose: String, intervaloHoras: Int): List<String> {
    if (intervaloHoras <= 0) return emptyList()
    val partes = primeiraDose.split(":")
    val hora = partes.getOrNull(0)?.toIntOrNull() ?: return emptyList()
    val minuto = partes.getOrNull(1)?.toIntOrNull() ?: return emptyList()
    val inicio = hora * 60 + minuto
    val passo = intervaloHoras * 60
    return (0 until (24 * 60) / passo)
        .map { (inicio + it * passo) % (24 * 60) }
        .sorted()
        .map { String.format(Locale.US, "%02d:%02d", it / 60, it % 60) }
}

/**
 * Estado do cadastro/edição. A agenda tem três partes: os horários do dia (fixos ou de X em X horas),
 * os dias (todo dia ou a cada N dias) e a duração. Em edição, [carregar] preenche tudo.
 */
class CadastroMedicamentoViewModel : ViewModel() {

    private val repositorio = Repositorios.medicamentos

    // Horários escolhidos à mão (modo "horários fixos").
    private val _horarios = MutableStateFlow<List<String>>(emptyList())
    val horarios: StateFlow<List<String>> = _horarios.asStateFlow()

    private val _tipo = MutableStateFlow(TipoFrequencia.HORARIOS_FIXOS)
    val tipo: StateFlow<TipoFrequencia> = _tipo.asStateFlow()

    // Modo "de X em X horas".
    private val _intervaloHoras = MutableStateFlow(8)
    val intervaloHoras: StateFlow<Int> = _intervaloHoras.asStateFlow()

    private val _primeiraDose = MutableStateFlow("08:00")
    val primeiraDose: StateFlow<String> = _primeiraDose.asStateFlow()

    /** Prévia dos horários do modo de intervalo, para a pessoa conferir antes de salvar. */
    val previa: Flow<List<String>> = combine(_intervaloHoras, _primeiraDose) { h, p -> gerarHorarios(p, h) }

    private val _carregado = MutableStateFlow<Medicamento?>(null)
    val carregado: StateFlow<Medicamento?> = _carregado.asStateFlow()

    /** A tela marca true depois de preencher os campos, para não sobrescrever o que o usuário já digitou. */
    var formularioPreenchido = false

    private var idEmEdicao = ""
    private var carregando = false

    fun carregar(id: String) {
        if (carregando) return
        carregando = true
        idEmEdicao = id
        viewModelScope.launch {
            val lista = repositorio.observar().catch { emit(emptyList()) }.first()
            val m = lista.firstOrNull { it.id == id } ?: return@launch
            _tipo.value = m.tipoFrequencia
            if (m.tipoFrequencia == TipoFrequencia.INTERVALO_HORAS) {
                _intervaloHoras.value = m.intervaloHoras.takeIf { it > 0 } ?: 8
                _primeiraDose.value = m.primeiraDose
            } else {
                _horarios.value = m.horarios.sorted()
            }
            _carregado.value = m
        }
    }

    fun definirTipo(tipo: TipoFrequencia) { _tipo.value = tipo }

    fun definirIntervalo(horas: Int) { _intervaloHoras.value = horas }

    fun definirPrimeiraDose(horario: String) { _primeiraDose.value = horario }

    /** Devolve false se o horário já estava na lista. */
    fun adicionarHorario(horario: String): Boolean {
        if (horario in _horarios.value) return false
        _horarios.value = (_horarios.value + horario).sorted()
        return true
    }

    fun removerHorario(horario: String) {
        _horarios.value = _horarios.value - horario
    }

    /**
     * @param intervaloDias 1 = todo dia; 7 = uma vez por semana; etc. (1 a 365)
     * @param duracaoDias 0 = uso contínuo; 1 a 365 = por esse número de dias; -1 = valor inválido digitado
     */
    fun salvar(
        nome: String,
        dose: String,
        forma: String,
        observacoes: String,
        adiamentoMin: Int,
        intervaloDias: Int,
        duracaoDias: Int,
    ): Validacao {
        if (nome.isBlank()) return Validacao.SEM_NOME

        val tipo = _tipo.value
        val horariosDoDia =
            if (tipo == TipoFrequencia.INTERVALO_HORAS) gerarHorarios(_primeiraDose.value, _intervaloHoras.value)
            else _horarios.value
        if (horariosDoDia.isEmpty()) return Validacao.SEM_HORARIO
        if (intervaloDias !in 1..365) return Validacao.DIAS_INVALIDOS
        if (duracaoDias !in 0..365) return Validacao.DURACAO_INVALIDA

        // Ao editar, o tratamento continua contando do dia em que começou.
        val inicio = _carregado.value?.inicioEm?.takeIf { it > 0 } ?: inicioDoDia(System.currentTimeMillis())

        repositorio.salvar(
            Medicamento(
                id = idEmEdicao,
                nome = nome.trim(),
                dose = dose.trim(),
                forma = forma.trim(),
                observacoes = observacoes.trim(),
                adiamentoMin = adiamentoMin,
                tipoFrequencia = tipo,
                horarios = horariosDoDia,
                intervaloHoras = if (tipo == TipoFrequencia.INTERVALO_HORAS) _intervaloHoras.value else 0,
                primeiraDose = _primeiraDose.value,
                intervaloDias = intervaloDias,
                duracaoDias = duracaoDias,
                inicioEm = inicio,
            )
        )
        return Validacao.OK
    }
}
