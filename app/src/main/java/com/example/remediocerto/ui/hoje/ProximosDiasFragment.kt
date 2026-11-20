package com.example.remediocerto.ui.hoje

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.databinding.FragmentProximosDiasBinding
import com.example.remediocerto.util.coletar

/** O que vem pela frente: quando cada tratamento termina e, dia a dia, os remédios dos próximos dias. */
class ProximosDiasFragment : Fragment() {

    private var _binding: FragmentProximosDiasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProximosDiasViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProximosDiasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ProximosDiasAdapter()
        binding.rvProximos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProximos.adapter = adapter

        coletar(viewModel.itens) { itens ->
            adapter.submitList(itens)
            binding.tvVazio.isVisible = itens.isEmpty()
        }

        binding.btnVoltar.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
