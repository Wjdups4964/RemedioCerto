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

        coletar(viewModel.itens) { itens ->
            adapter.submitList(itens)
            binding.tvVazio.isVisible = itens.isEmpty()
        }
        coletar(viewModel.resumo) { resumo ->
            binding.tvAdesao.text = when {
                resumo == null -> getString(R.string.historico_sem_semana)
                resumo.naoTomadas + resumo.atrasadas == 0 ->
                    resources.getQuantityString(R.plurals.historico_semana, resumo.total, resumo.tomadas, resumo.total)
                else -> resources.getQuantityString(R.plurals.historico_semana, resumo.total, resumo.tomadas, resumo.total) +
                    "\n" + getString(R.string.historico_semana_falhas, resumo.naoTomadas, resumo.atrasadas)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
