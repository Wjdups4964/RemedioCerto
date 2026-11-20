package com.example.remediocerto.ui.configuracoes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.alarm.AlarmeScheduler
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.data.repository.AuthRepository
import com.example.remediocerto.databinding.FragmentConfiguracoesBinding
import com.example.remediocerto.ui.entrada.EntradaActivity
import com.example.remediocerto.ui.main.MainActivity
import com.example.remediocerto.util.ModoTema
import com.example.remediocerto.util.PermissoesHelper
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
        // Os avisos dos remédios são só do idoso; o familiar não vê essa seção.
        val perfil = Perfil.valueOf(
            requireActivity().intent.getStringExtra(MainActivity.EXTRA_PERFIL) ?: Perfil.IDOSO.name
        )
        binding.secaoAvisos.isVisible = perfil == Perfil.IDOSO
        binding.btnLigarNotif.setOnClickListener {
            runCatching { startActivity(PermissoesHelper.intentAjusteNotificacoes(requireContext())) }
        }
        binding.btnPermitirExato.setOnClickListener {
            PermissoesHelper.intentAjusteAlarmeExato(requireContext())?.let { runCatching { startActivity(it) } }
        }

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
            // Sem login não deve chegar aviso de remédio de ninguém: cancela os alarmes e apaga a cópia local.
            AlarmeScheduler.cancelarTudo(requireContext())
            AuthRepository().sair()
            val intent = Intent(requireContext(), EntradaActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    // Ao voltar da tela de ajustes do Android, a situação dos avisos é conferida de novo.
    override fun onResume() {
        super.onResume()
        atualizarSituacaoDosAvisos()
    }

    private fun atualizarSituacaoDosAvisos() {
        if (!binding.secaoAvisos.isVisible) return
        val contexto = requireContext()

        val notificacoesOk = !PermissoesHelper.notificacoesDesligadas(contexto)
        binding.tvStatusNotif.setText(if (notificacoesOk) R.string.config_notif_ligadas else R.string.config_notif_desligadas)
        binding.tvStatusNotif.setTextColor(corDaSituacao(notificacoesOk))
        binding.btnLigarNotif.isVisible = !notificacoesOk

        val exatoOk = PermissoesHelper.podeAgendarExato(contexto)
        binding.tvStatusExato.setText(if (exatoOk) R.string.config_exato_ok else R.string.config_exato_falta)
        binding.tvStatusExato.setTextColor(corDaSituacao(exatoOk))
        binding.btnPermitirExato.isVisible = !exatoOk
    }

    private fun corDaSituacao(ok: Boolean) =
        ContextCompat.getColor(requireContext(), if (ok) R.color.rc_tomada else R.color.rc_perdida)

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
