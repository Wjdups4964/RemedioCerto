package com.example.remediocerto.ui.configuracoes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.data.repository.AuthRepository
import com.example.remediocerto.databinding.FragmentConfiguracoesBinding
import com.example.remediocerto.ui.entrada.EntradaActivity
import com.example.remediocerto.util.ModoTema
import com.example.remediocerto.util.Tema
import com.example.remediocerto.util.coletar

class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfiguracoesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Mostra o valor salvo. Só ajusta a chave se estiver diferente, para não entrar em laço.
        coletar(viewModel.acessibilidade) { ativo ->
            if (binding.switchAcessibilidade.isChecked != ativo) {
                binding.switchAcessibilidade.isChecked = ativo
            }
        }
        // setOnClickListener (e não OnCheckedChange): só dispara quando a PESSOA toca na chave.
        binding.switchAcessibilidade.setOnClickListener {
            viewModel.alterarAcessibilidade(binding.switchAcessibilidade.isChecked)
            Toast.makeText(requireContext(), R.string.config_acessibilidade_alterada, Toast.LENGTH_SHORT).show()
        }

        mostrarTemaEscolhido()
        // addOnButtonCheckedListener também dispara ao marcar por código; por isso só agimos se mudou de verdade.
        binding.toggleTema.addOnButtonCheckedListener { _, botao, marcado ->
            if (!marcado) return@addOnButtonCheckedListener
            val modo = when (botao) {
                R.id.btnTemaClaro -> ModoTema.CLARO
                R.id.btnTemaEscuro -> ModoTema.ESCURO
                else -> ModoTema.AUTOMATICO
            }
            if (modo != Tema.salvo(requireContext())) Tema.escolher(requireContext(), modo)
        }

        binding.btnPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_configuracoes_to_perfil)
        }
        binding.btnVinculo.setOnClickListener {
            findNavController().navigate(R.id.action_configuracoes_to_vinculo)
        }
        binding.btnFarmacias.setOnClickListener {
            findNavController().navigate(R.id.action_configuracoes_to_farmacias)
        }
        // Sair encerra a sessão: para voltar, é preciso entrar de novo com e-mail e senha.
        // Os dados continuam salvos na conta.
        binding.btnSair.setOnClickListener {
            AuthRepository().sair()
            val intent = Intent(requireContext(), EntradaActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun mostrarTemaEscolhido() {
        binding.toggleTema.check(
            when (Tema.salvo(requireContext())) {
                ModoTema.CLARO -> R.id.btnTemaClaro
                ModoTema.ESCURO -> R.id.btnTemaEscuro
                ModoTema.AUTOMATICO -> R.id.btnTemaAutomatico
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
