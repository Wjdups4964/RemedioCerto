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
import com.example.remediocerto.ui.tutorial.TutorialActivity
import com.example.remediocerto.util.ModoTema
import com.example.remediocerto.util.PermissoesHelper
import com.example.remediocerto.util.TamanhoFonte
import com.example.remediocerto.util.Tema

class ConfiguracoesFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfiguracoesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Os avisos dos remédios são só do modo "Uso próprio"; no modo Acompanhante essa seção some.
        val perfil = Perfil.valueOf(
            requireActivity().intent.getStringExtra(MainActivity.EXTRA_PERFIL) ?: Perfil.IDOSO.name
        )
        binding.secaoAvisos.isVisible = perfil == Perfil.IDOSO

        // A mesma conta tem os dois modos. Trocar reabre o app já no modo escolhido.
        val proprio = perfil == Perfil.IDOSO
        binding.toggleModo.check(if (proprio) R.id.btnModoProprio else R.id.btnModoAcompanhante)
        binding.tvModoAjuda.setText(if (proprio) R.string.config_modo_ajuda_proprio else R.string.config_modo_ajuda_acompanhante)
        // Também dispara ao marcar por código; por isso só troca se o modo escolhido é outro.
        binding.toggleModo.addOnButtonCheckedListener { _, botao, marcado ->
            if (!marcado) return@addOnButtonCheckedListener
            val novo = if (botao == R.id.btnModoProprio) Perfil.IDOSO else Perfil.FAMILIAR
            if (novo != perfil) trocarModo(novo)
        }
        binding.btnLigarNotif.setOnClickListener {
            runCatching { startActivity(PermissoesHelper.intentAjusteNotificacoes(requireContext())) }
        }
        binding.btnPermitirExato.setOnClickListener {
            PermissoesHelper.intentAjusteAlarmeExato(requireContext())?.let { runCatching { startActivity(it) } }
        }

        // Modo de acessibilidade = letras grandes. Vale neste aparelho; ao mudar, a tela é recriada com o novo tamanho.
        binding.switchAcessibilidade.isChecked = TamanhoFonte.acessibilidadeAtiva(requireContext())
        // setOnClickListener (e não OnCheckedChange): só dispara quando a PESSOA toca na chave.
        binding.switchAcessibilidade.setOnClickListener {
            val ativa = binding.switchAcessibilidade.isChecked
            TamanhoFonte.escolher(requireContext(), ativa)
            viewModel.alterarAcessibilidade(ativa)
            requireActivity().recreate()
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
        binding.btnTutorial.setOnClickListener {
            startActivity(Intent(requireContext(), TutorialActivity::class.java))
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

    // Guarda o modo na conta (vale também no próximo login) e reabre o app nele. Os alarmes agendados continuam.
    private fun trocarModo(novo: Perfil) {
        AuthRepository().salvarPerfil(novo)
        val aviso = if (novo == Perfil.IDOSO) R.string.config_modo_trocado_proprio else R.string.config_modo_trocado_acompanhante
        Toast.makeText(requireContext().applicationContext, aviso, Toast.LENGTH_SHORT).show()
        startActivity(
            Intent(requireContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_PERFIL, novo.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
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
