package com.example.remediocerto.ui.acompanhamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.data.model.Amostras
import com.example.remediocerto.databinding.FragmentAcompanhamentoBinding
import com.example.remediocerto.ui.hoje.DoseDiaAdapter

class AcompanhamentoFragment : Fragment() {

    private var _binding: FragmentAcompanhamentoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAcompanhamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvDoses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoses.adapter = DoseDiaAdapter(Amostras.dosesHoje)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
