package com.example.remediocerto.ui.hoje

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.data.model.Amostras
import com.example.remediocerto.databinding.FragmentHojeBinding
import com.example.remediocerto.ui.alarme.AlarmeActivity

/** Tela inicial do idoso: lista do dia com doses tomadas e próximas (RF15). */
class HojeFragment : Fragment() {

    private var _binding: FragmentHojeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHojeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvDoses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoses.adapter = DoseDiaAdapter(Amostras.dosesHoje)

        // Atalho de demonstração: abre a tela de alarme sem esperar o horário.
        binding.btnTestarAlarme.setOnClickListener {
            startActivity(Intent(requireContext(), AlarmeActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
