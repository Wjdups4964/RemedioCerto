package com.example.remediocerto.ui.vinculo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Amostras
import com.example.remediocerto.databinding.FragmentVinculoBinding

/** Gerar e usar código de convite; ver e revogar vínculos (RF28 a RF30). A lógica real vem com o Firebase. */
class VinculoFragment : Fragment() {

    private var _binding: FragmentVinculoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVinculoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnGerarCodigo.setOnClickListener {
            binding.tvCodigo.text = (100000..999999).random().toString()
        }
        binding.btnVincular.setOnClickListener {
            Toast.makeText(requireContext(), R.string.vinculo_vinculado_exemplo, Toast.LENGTH_SHORT).show()
        }
        binding.rvVinculos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVinculos.adapter = VinculoAdapter(Amostras.vinculos) {
            Toast.makeText(requireContext(), R.string.vinculo_revogado_exemplo, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
