package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentDetalhesMedicamentoBinding
import com.example.remediocerto.util.coletar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetalhesMedicamentoFragment : Fragment() {

    private var _binding: FragmentDetalhesMedicamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalhesMedicamentoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalhesMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = arguments?.getString(ARG_MEDICAMENTO_ID)
        if (id == null) {
            findNavController().popBackStack()
            return
        }

        coletar(viewModel.medicamento(id)) { medicamento ->
            if (medicamento != null) {
                binding.tvNome.text = medicamento.nome
                binding.tvDoseHorarios.text = medicamento.resumo()
            }
        }

        binding.btnConsultarBula.setOnClickListener {
            binding.tvInfoBula.setText(R.string.detalhes_bula_consultando)
        }
        binding.btnEditar.setOnClickListener {
            findNavController().navigate(R.id.action_detalhes_to_cadastro, bundleOf(ARG_MEDICAMENTO_ID to id))
        }
        binding.btnApagar.setOnClickListener { confirmarExclusao(id) }
    }

    private fun confirmarExclusao(id: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.detalhes_apagar_titulo)
            .setMessage(R.string.detalhes_apagar_msg)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.detalhes_apagar_sim) { _, _ ->
                viewModel.apagar(id)
                Toast.makeText(requireContext(), R.string.detalhes_apagado, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
