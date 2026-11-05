package com.example.remediocerto.ui.medicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentCadastroMedicamentoBinding

/** Cadastro e edição de medicamento, com horários (RF01 a RF03). Salvar de verdade vem com o Room. */
class CadastroMedicamentoFragment : Fragment() {

    private var _binding: FragmentCadastroMedicamentoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCadastroMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSalvar.setOnClickListener {
            Toast.makeText(requireContext(), R.string.cadastro_salvo_exemplo, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
