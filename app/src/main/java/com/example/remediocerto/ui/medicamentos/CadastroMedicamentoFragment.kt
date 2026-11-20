package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.TipoFrequencia
import com.example.remediocerto.databinding.FragmentCadastroMedicamentoBinding
import com.example.remediocerto.util.coletar
import com.example.remediocerto.util.diasDeTomar
import com.example.remediocerto.util.diasEntre
import com.example.remediocerto.util.formatarDiaLocal
import com.example.remediocerto.util.formatarDiaMes
import com.example.remediocerto.util.inicioDoDia
import com.example.remediocerto.util.localParaUtc
import com.example.remediocerto.util.somarDias
import com.example.remediocerto.util.utcParaLocal
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

/**
 * Cadastro e edição de medicamento. Se chegar o argumento [ARG_MEDICAMENTO_ID], é edição.
 *
 * A agenda é montada em três perguntas: quando tomar (horários fixos ou de X em X horas),
 * em quais dias (todo dia ou a cada N dias) e por quanto tempo (contínuo ou por N dias).
 */
class CadastroMedicamentoFragment : Fragment() {

    private var _binding: FragmentCadastroMedicamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CadastroMedicamentoViewModel by viewModels()


    // Período do tratamento: dia de início (meia-noite local) e dia de término previsto (0 = sem término).
    private var inicioMs = inicioDoDia(System.currentTimeMillis())
    private var fimMs = 0L

    // Forma escolhida (texto que será salvo). Medicamentos antigos podem ter uma forma digitada à mão,
    // que não está nos botões: ela é mantida até a pessoa escolher outra.
    private var forma = ""

    private var hora = 8
    private var minuto = 0

    // Chip da forma -> texto do botão. O mapa guarda o id do texto (R.string) para trocar de idioma sem quebrar.
    private val formasPorChip = mapOf(
        R.id.chipFormaComprimido to R.string.forma_comprimido,
        R.id.chipFormaCapsula to R.string.forma_capsula,
        R.id.chipFormaGotas to R.string.forma_gotas,
        R.id.chipFormaXarope to R.string.forma_xarope,
        R.id.chipFormaInjecao to R.string.forma_injecao,
        R.id.chipFormaPomada to R.string.forma_pomada,
        R.id.chipFormaOutro to R.string.forma_outro,
    )

    // Chip escolhido -> intervalo em horas. Só valores que dividem 24, para os horários se repetirem todo dia.
    private val intervalos = mapOf(
        R.id.chip2h to 2, R.id.chip3h to 3, R.id.chip4h to 4,
        R.id.chip6h to 6, R.id.chip8h to 8, R.id.chip12h to 12,
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCadastroMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idParaEditar = arguments?.getString(ARG_MEDICAMENTO_ID)
        if (idParaEditar != null) {
            binding.tvTitulo.setText(R.string.cadastro_editar_titulo)
            viewModel.carregar(idParaEditar)
        }

        // Estado -> tela
        coletar(viewModel.horarios) { mostrarHorarios(it) }
        coletar(viewModel.tipo) { mostrarModo(it) }
        coletar(viewModel.intervaloHoras) { horas ->
            intervalos.entries.firstOrNull { it.value == horas }?.let { binding.chipsIntervalo.check(it.key) }
        }
        coletar(viewModel.primeiraDose) { binding.etPrimeiraDose.setText(it) }
        coletar(viewModel.previa) {
            binding.tvPrevia.text = getString(R.string.cadastro_previa, it.joinToString(" · "))
        }
        coletar(viewModel.carregado) { medicamento ->
            if (medicamento != null && !viewModel.formularioPreenchido) {
                preencher(medicamento)
                viewModel.formularioPreenchido = true
            }
        }

        // Tela -> estado
        binding.toggleModo.addOnButtonCheckedListener { _, id, marcado ->
            if (marcado) {
                viewModel.definirTipo(
                    if (id == R.id.btnModoIntervalo) TipoFrequencia.INTERVALO_HORAS
                    else TipoFrequencia.HORARIOS_FIXOS
                )
            }
        }
        binding.chipsIntervalo.setOnCheckedStateChangeListener { _, ids ->
            ids.firstOrNull()?.let { intervalos[it] }?.let { viewModel.definirIntervalo(it) }
        }
        binding.etHorario.setOnClickListener {
            abrirSeletorDeHorario { horario ->
                if (!viewModel.adicionarHorario(horario)) {
                    Toast.makeText(requireContext(), R.string.cadastro_horario_repetido, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.etPrimeiraDose.setOnClickListener {
            abrirSeletorDeHorario { viewModel.definirPrimeiraDose(it) }
        }

        binding.rgDias.setOnCheckedChangeListener { _, id ->
            binding.tilIntervaloDias.isVisible = id == R.id.rbACadaDias
            atualizarPeriodo()
        }
        binding.rgDuracao.setOnCheckedChangeListener { _, id ->
            // Ao escolher "tem data para terminar", sugere 7 dias (o mais comum em antibiótico).
            if (id == R.id.rbPorDias && fimMs < inicioMs) fimMs = somarDias(inicioMs, 6)
            binding.tilTermino.isVisible = id == R.id.rbPorDias
            atualizarPeriodo()
        }

        configurarSugestoesDeNome()
        binding.chipsForma.setOnCheckedStateChangeListener { _, ids ->
            forma = ids.firstOrNull()?.let { formasPorChip[it] }?.let { getString(it) }.orEmpty()
        }
        binding.etIntervaloDias.doAfterTextChanged {
            binding.tilIntervaloDias.error = null
            atualizarPeriodo()
        }
        binding.etInicio.setOnClickListener { escolherInicio() }
        binding.etTermino.setOnClickListener { escolherTermino() }
        atualizarPeriodo()
        binding.btnSalvar.setOnClickListener { salvar() }
    }

    /**
     * Mostra nomes de medicamentos enquanto a pessoa digita, para ela escolher em vez de escrever tudo
     * (e errar). A lista vem do servidor; sem internet não aparece nada e o campo segue aceitando texto livre.
     * Quando o texto digitado já é um nome completo da lista, a lista fecha.
     */
    private fun configurarSugestoesDeNome() {
        val adaptador = SugestoesAdapter(requireContext())
        binding.etNome.setAdapter(adaptador)

        // Quando a pessoa toca numa sugestão, o texto muda sozinho: isso não deve abrir a lista de novo.
        var escolhido: String? = null
        binding.etNome.setOnItemClickListener { _, _, posicao, _ ->
            escolhido = adaptador.getItem(posicao)
        }
        binding.etNome.doAfterTextChanged { texto ->
            binding.tilNome.error = null
            viewModel.digitouNome(texto?.toString().orEmpty())
        }
        coletar(viewModel.sugestoes) { lista ->
            adaptador.trocar(lista)
            val atual = binding.etNome.text?.toString().orEmpty()
            val nomeCompleto = lista.any { mesmoNome(it, atual) }
            val mostrar = lista.isNotEmpty() && !nomeCompleto && atual != escolhido &&
                binding.etNome.hasFocus() && atual.trim().length >= 2
            if (mostrar) binding.etNome.showDropDown() else binding.etNome.dismissDropDown()
        }
    }

    private fun escolherInicio() {
        val seletor = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.cadastro_periodo_inicio)
            .setSelection(localParaUtc(inicioMs))
            .build()
        seletor.addOnPositiveButtonClickListener {
            inicioMs = utcParaLocal(it)
            if (fimMs in 1 until inicioMs) fimMs = inicioMs // o término nunca fica antes do início
            binding.tilTermino.error = null
            atualizarPeriodo()
        }
        seletor.show(childFragmentManager, "seletor_inicio")
    }

    private fun escolherTermino() {
        val limite = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(localParaUtc(inicioMs))) // só do início em diante
            .build()
        val seletor = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.cadastro_periodo_termino)
            .setCalendarConstraints(limite)
            .setSelection(localParaUtc(maxOf(fimMs, inicioMs)))
            .build()
        seletor.addOnPositiveButtonClickListener {
            fimMs = utcParaLocal(it)
            binding.tilTermino.error = null
            atualizarPeriodo()
        }
        seletor.show(childFragmentManager, "seletor_termino")
    }

    /** Mostra as datas nos campos, uma frase com o resumo do período e, se for o caso, os próximos dias de tomar. */
    private fun atualizarPeriodo() {
        val comFim = binding.rgDuracao.checkedRadioButtonId == R.id.rbPorDias
        binding.etInicio.setText(formatarDiaLocal(inicioMs))
        if (fimMs > 0) binding.etTermino.setText(formatarDiaLocal(fimMs))

        binding.tvPeriodo.text = if (comFim && fimMs >= inicioMs) {
            val dias = diasEntre(inicioMs, fimMs) + 1
            getString(
                R.string.cadastro_periodo_resumo_fim,
                formatarDiaLocal(inicioMs),
                formatarDiaLocal(fimMs),
                resources.getQuantityString(R.plurals.cadastro_periodo_dias, dias, dias),
            )
        } else {
            getString(R.string.cadastro_periodo_resumo_continuo, formatarDiaLocal(inicioMs))
        }

        // "De vez em quando": mostra quais são os dias para a pessoa conferir (ex.: 20/11, 27/11, 04/12…).
        val intervalo = binding.etIntervaloDias.text?.toString()?.toIntOrNull() ?: 0
        val aCada = binding.rgDias.checkedRadioButtonId == R.id.rbACadaDias && intervalo in 2..365
        binding.tvDiasPrevia.isVisible = aCada
        if (aCada) {
            val fim = if (comFim && fimMs >= inicioMs) fimMs else 0L
            val dias = diasDeTomar(inicioMs, intervalo, fim, quantidade = 4)
            val acabou = fim > 0 && dias.size < 4
            binding.tvDiasPrevia.text = getString(
                if (acabou) R.string.cadastro_dias_previa_fim else R.string.cadastro_dias_previa,
                dias.joinToString(", ") { formatarDiaMes(it) },
            )
        }
    }

    private fun mostrarModo(tipo: TipoFrequencia) {
        val intervalo = tipo == TipoFrequencia.INTERVALO_HORAS
        binding.layoutFixos.isVisible = !intervalo
        binding.layoutIntervalo.isVisible = intervalo
        val esperado = if (intervalo) R.id.btnModoIntervalo else R.id.btnModoFixos
        if (binding.toggleModo.checkedButtonId != esperado) binding.toggleModo.check(esperado)
    }

    private fun preencher(m: Medicamento) {
        binding.etNome.setText(m.nome)
        binding.etDose.setText(m.dose)
        forma = m.forma
        formasPorChip.entries.firstOrNull { semAcentoMinusculo(getString(it.value)) == semAcentoMinusculo(m.forma) }
            ?.let { binding.chipsForma.check(it.key) }
        binding.etObservacoes.setText(m.observacoes)
        binding.rgAdiamento.check(
            when (m.adiamentoMin) {
                5 -> R.id.rb5
                15 -> R.id.rb15
                else -> R.id.rb10
            }
        )
        if (m.intervaloDias > 1) {
            binding.rgDias.check(R.id.rbACadaDias)
            binding.etIntervaloDias.setText(m.intervaloDias.toString())
        }
        inicioMs = if (m.inicioEm > 0) inicioDoDia(m.inicioEm) else inicioDoDia(System.currentTimeMillis())
        if (m.duracaoDias > 0) {
            fimMs = somarDias(inicioMs, m.duracaoDias - 1)
            binding.rgDuracao.check(R.id.rbPorDias)
        }
        atualizarPeriodo()
    }

    private fun mostrarHorarios(horarios: List<String>) {
        val grupo = binding.chipsHorarios
        grupo.removeAllViews()
        horarios.forEach { horario ->
            val chip = Chip(requireContext()).apply {
                text = horario
                textSize = 18f
                isCloseIconVisible = true
                closeIconContentDescription = getString(R.string.cadastro_remover_horario, horario)
                setOnCloseIconClickListener { viewModel.removerHorario(horario) }
            }
            grupo.addView(chip)
        }
    }

    private fun salvar() {
        val adiamento = when (binding.rgAdiamento.checkedRadioButtonId) {
            R.id.rb5 -> 5
            R.id.rb15 -> 15
            else -> 10
        }
        // "Todos os dias" = repetir a cada 1 dia. Campo vazio ou 0 vira 0 (inválido) e a validação reclama.
        val intervaloDias =
            if (binding.rgDias.checkedRadioButtonId == R.id.rbACadaDias)
                binding.etIntervaloDias.text?.toString()?.toIntOrNull() ?: 0
            else 1
        // "Uso contínuo" = 0. Com data de término: dias do início ao término, contando os dois. Término antes do início = -1.
        val duracaoDias =
            if (binding.rgDuracao.checkedRadioButtonId == R.id.rbPorDias)
                if (fimMs < inicioMs) -1 else diasEntre(inicioMs, fimMs) + 1
            else 0

        val resultado = viewModel.salvar(
            nome = binding.etNome.text?.toString().orEmpty(),
            dose = binding.etDose.text?.toString().orEmpty(),
            forma = forma,
            observacoes = binding.etObservacoes.text?.toString().orEmpty(),
            adiamentoMin = adiamento,
            intervaloDias = intervaloDias,
            duracaoDias = duracaoDias,
            inicioEm = inicioMs,
        )
        when (resultado) {
            Validacao.SEM_NOME -> {
                binding.tilNome.error = getString(R.string.cadastro_nome_obrigatorio)
                binding.etNome.requestFocus()
            }
            Validacao.SEM_HORARIO ->
                Toast.makeText(requireContext(), R.string.cadastro_horario_obrigatorio, Toast.LENGTH_SHORT).show()
            Validacao.DIAS_INVALIDOS ->
                binding.tilIntervaloDias.error = getString(R.string.cadastro_dias_invalido)
            Validacao.DURACAO_INVALIDA ->
                binding.tilTermino.error = getString(R.string.cadastro_termino_invalido)
            Validacao.OK -> {
                binding.btnSalvar.isEnabled = false // evita toque duplo
                Toast.makeText(requireContext(), R.string.cadastro_salvo, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun abrirSeletorDeHorario(aoEscolher: (String) -> Unit) {
        val seletor = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(hora)
            .setMinute(minuto)
            .setTitleText(R.string.cadastro_horario_titulo)
            .build()

        seletor.addOnPositiveButtonClickListener {
            hora = seletor.hour
            minuto = seletor.minute
            aoEscolher(String.format(Locale.US, "%02d:%02d", hora, minuto))
        }
        seletor.show(childFragmentManager, "seletor_horario")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** "Cápsula" e "capsula" contam como a mesma forma. */
private fun semAcentoMinusculo(texto: String): String =
    java.text.Normalizer.normalize(texto.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
