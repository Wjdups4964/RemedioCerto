package com.example.remediocerto.ui.configuracoes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentConfiguracoesBinding
import com.example.remediocerto.ui.entrada.EntradaActivity

/** Acessibilidade, adiamento, vínculo e conta (RF34, RF36, RF25). As preferências são salvas em etapa futura. */
class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchAcessibilidade.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(requireContext(), R.string.config_acessibilidade_aviso, Toast.LENGTH_SHORT).show()
        }
        binding.btnVinculo.setOnClickListener {
            findNavController().navigate(R.id.action_configuracoes_to_vinculo)
        }
        binding.btnFarmacias.setOnClickListener {
            findNavController().navigate(R.id.action_configuracoes_to_farmacias)
        }
        binding.btnSair.setOnClickListener {
            val intent = Intent(requireContext(), EntradaActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
