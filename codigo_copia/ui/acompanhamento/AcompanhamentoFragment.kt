package com.example.remediocerto.ui.acompanhamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.databinding.FragmentAcompanhamentoBinding
import com.example.remediocerto.ui.hoje.DoseDiaAdapter
import com.example.remediocerto.ui.hoje.HojeViewModel
import com.example.remediocerto.util.coletar

/**
 * Visão do familiar: as doses do dia em modo somente leitura. Por enquanto lê os dados da própria
 * conta; ver os dados do idoso vinculado chega com o web service e o vínculo (entrega de 24/11).
 */
class AcompanhamentoFragment : Fragment() {

    private var _binding: FragmentAcompanhamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HojeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAcompanhamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DoseDiaAdapter(somenteLeitura = true)
        binding.rvDoses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoses.adapter = adapter
        coletar(viewModel.dosesDeHoje) { adapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
