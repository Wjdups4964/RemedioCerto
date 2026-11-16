package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentMedicamentosBinding
import com.example.remediocerto.util.coletar

class MedicamentosFragment : Fragment() {

    private var _binding: FragmentMedicamentosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MedicamentosViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMedicamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MedicamentoAdapter { medicamento ->
            findNavController().navigate(
                R.id.action_medicamentos_to_detalhes,
                bundleOf(ARG_MEDICAMENTO_ID to medicamento.id),
            )
        }
        binding.rvMedicamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicamentos.adapter = adapter

        coletar(viewModel.medicamentos) { lista ->
            adapter.submitList(lista)
            binding.tvVazio.isVisible = lista.isEmpty()
        }

        binding.fabAdicionar.setOnClickListener {
            findNavController().navigate(R.id.action_medicamentos_to_cadastro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
