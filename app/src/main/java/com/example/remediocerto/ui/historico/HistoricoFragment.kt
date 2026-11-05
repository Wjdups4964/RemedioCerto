package com.example.remediocerto.ui.historico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.data.model.Amostras
import com.example.remediocerto.databinding.FragmentHistoricoBinding

/** Doses registradas por período e resumo de adesão (RF14, RF16, RF17). */
class HistoricoFragment : Fragment() {

    private var _binding: FragmentHistoricoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoricoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvHistorico.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorico.adapter = HistoricoAdapter(Amostras.historico)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
