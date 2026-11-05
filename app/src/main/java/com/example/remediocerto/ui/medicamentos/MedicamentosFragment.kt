package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Amostras
import com.example.remediocerto.databinding.FragmentMedicamentosBinding

/** Lista de medicamentos cadastrados, com botão para adicionar (RF05). */
class MedicamentosFragment : Fragment() {

    private var _binding: FragmentMedicamentosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMedicamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvMedicamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicamentos.adapter = MedicamentoAdapter(Amostras.medicamentos) {
            findNavController().navigate(R.id.action_medicamentos_to_detalhes)
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
