package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentCadastroMedicamentoBinding

class CadastroMedicamentoFragment : Fragment() {

    private var _binding: FragmentCadastroMedicamentoBinding? = null
    private val binding get() = _binding!!

    private var hora = 8
    private var minuto = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCadastroMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etHorario.setOnClickListener { abrirSeletorDeHorario() }

        binding.btnSalvar.setOnClickListener {
            if (binding.etHorario.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.cadastro_horario_obrigatorio, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), R.string.cadastro_salvo, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun abrirSeletorDeHorario() {
        val seletor = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hora)
            .setMinute(minuto)
            .setTitleText(R.string.cadastro_horario_titulo)
            .build()

        seletor.addOnPositiveButtonClickListener {
            hora = seletor.hour
            minuto = seletor.minute
            binding.etHorario.setText(String.format(Locale.getDefault(), "%02d:%02d", hora, minuto))
        }
        seletor.show(childFragmentManager, "seletor_horario")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
