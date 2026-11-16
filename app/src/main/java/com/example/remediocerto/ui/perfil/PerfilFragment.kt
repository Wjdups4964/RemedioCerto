package com.example.remediocerto.ui.perfil

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
import com.example.remediocerto.data.model.DadosPessoais
import com.example.remediocerto.data.model.Sexo
import com.example.remediocerto.data.repository.AuthRepository
import com.example.remediocerto.databinding.FragmentPerfilBinding
import com.example.remediocerto.util.coletar
import com.example.remediocerto.util.formatarData
import com.example.remediocerto.util.idadeEm
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PerfilViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvEmail.text = getString(R.string.perfil_conta, AuthRepository().emailAtual.orEmpty())

        coletar(viewModel.dados) { dados ->
            if (dados != null && !viewModel.formularioPreenchido) {
                preencher(dados)
                viewModel.formularioPreenchido = true
            }
        }
        coletar(viewModel.nascimento) { mostrarNascimento(it) }

        binding.etNome.doAfterTextChanged { binding.tilNome.error = null }
        binding.etNascimento.setOnClickListener { abrirSeletorDeData() }
        binding.btnSalvar.setOnClickListener { salvar() }
    }

    private fun preencher(d: DadosPessoais) {
        binding.etNome.setText(d.nome)
        binding.etContato.setText(d.contatoEmergencia)
        binding.rgSexo.check(
            when (d.sexo) {
                Sexo.FEMININO -> R.id.rbFeminino
                Sexo.MASCULINO -> R.id.rbMasculino
                Sexo.OUTRO -> R.id.rbOutro
                Sexo.NAO_INFORMADO -> R.id.rbNaoInformar
            }
        )
        viewModel.definirNascimento(d.nascimentoEm)
    }

    private fun mostrarNascimento(utcMs: Long) {
        binding.etNascimento.setText(if (utcMs > 0L) formatarData(utcMs) else "")
        val idade = idadeEm(utcMs)
        binding.tvIdade.isVisible = idade != null
        if (idade != null) binding.tvIdade.text = getString(R.string.perfil_idade, idade)
    }

    // Modo de digitar a data (dd/mm/aaaa): mais fácil que rolar o calendário até o ano de nascimento.
    private fun abrirSeletorDeData() {
        val limite = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val selecaoInicial = viewModel.nascimento.value.takeIf { it > 0L }
        val seletor = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.perfil_nascimento)
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            .setCalendarConstraints(limite)
            .setSelection(selecaoInicial)
            .build()
        // O seletor devolve a data como meia-noite UTC, o mesmo formato que guardamos.
        seletor.addOnPositiveButtonClickListener { viewModel.definirNascimento(it) }
        seletor.show(childFragmentManager, "seletor_nascimento")
    }

    private fun salvar() {
        val sexo = when (binding.rgSexo.checkedRadioButtonId) {
            R.id.rbFeminino -> Sexo.FEMININO
            R.id.rbMasculino -> Sexo.MASCULINO
            R.id.rbOutro -> Sexo.OUTRO
            else -> Sexo.NAO_INFORMADO
        }
        val ok = viewModel.salvar(
            nome = binding.etNome.text?.toString().orEmpty(),
            sexo = sexo,
            contatoEmergencia = binding.etContato.text?.toString().orEmpty(),
        )
        if (!ok) {
            binding.tilNome.error = getString(R.string.erro_nome)
            binding.etNome.requestFocus()
            return
        }
        binding.btnSalvar.isEnabled = false // evita toque duplo
        Toast.makeText(requireContext(), R.string.perfil_salvo, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
