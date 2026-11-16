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
import com.google.android.material.chip.Chip
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

    private var hora = 8
    private var minuto = 0

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
        }
        binding.rgDuracao.setOnCheckedChangeListener { _, id ->
            binding.tilDuracaoDias.isVisible = id == R.id.rbPorDias
        }

        binding.etNome.doAfterTextChanged { binding.tilNome.error = null }
        binding.etIntervaloDias.doAfterTextChanged { binding.tilIntervaloDias.error = null }
        binding.etDuracaoDias.doAfterTextChanged { binding.tilDuracaoDias.error = null }
        binding.btnSalvar.setOnClickListener { salvar() }
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
        binding.etForma.setText(m.forma)
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
        if (m.duracaoDias > 0) {
            binding.rgDuracao.check(R.id.rbPorDias)
            binding.etDuracaoDias.setText(m.duracaoDias.toString())
        }
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
        // "Uso contínuo" = 0. Em "por N dias", vazio ou 0 vira -1 (inválido).
        val duracaoDias =
            if (binding.rgDuracao.checkedRadioButtonId == R.id.rbPorDias)
                binding.etDuracaoDias.text?.toString()?.toIntOrNull()?.takeIf { it >= 1 } ?: -1
            else 0

        val resultado = viewModel.salvar(
            nome = binding.etNome.text?.toString().orEmpty(),
            dose = binding.etDose.text?.toString().orEmpty(),
            forma = binding.etForma.text?.toString().orEmpty(),
            observacoes = binding.etObservacoes.text?.toString().orEmpty(),
            adiamentoMin = adiamento,
            intervaloDias = intervaloDias,
            duracaoDias = duracaoDias,
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
                binding.tilDuracaoDias.error = getString(R.string.cadastro_dias_invalido)
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
