package com.example.remediocerto.ui.historico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentHistoricoBinding
import com.example.remediocerto.util.coletar

class HistoricoFragment : Fragment() {

    private var _binding: FragmentHistoricoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoricoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoricoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = HistoricoAdapter()
        binding.rvHistorico.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorico.adapter = adapter

        coletar(viewModel.registros) { registros ->
            adapter.submitList(registros)
            binding.tvVazio.isVisible = registros.isEmpty()
        }
        coletar(viewModel.adesao) { adesao ->
            binding.tvAdesao.text =
                if (adesao == null) getString(R.string.historico_sem_adesao)
                else getString(R.string.historico_adesao_pct, adesao)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
