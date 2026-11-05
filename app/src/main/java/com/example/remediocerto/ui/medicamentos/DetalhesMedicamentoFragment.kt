package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentDetalhesMedicamentoBinding

/** Dados do medicamento e bula pela bulário-api (RF19 a RF21). A consulta real vem na etapa da API. */
class DetalhesMedicamentoFragment : Fragment() {

    private var _binding: FragmentDetalhesMedicamentoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalhesMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvNome.text = "Losartana"
        binding.tvDoseHorarios.text = "50 mg · 08:00 e 20:00"

        binding.btnConsultarBula.setOnClickListener {
            binding.tvInfoBula.setText(R.string.detalhes_bula_pendente)
        }
        binding.btnEditar.setOnClickListener {
            findNavController().navigate(R.id.action_detalhes_to_cadastro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
