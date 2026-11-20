package com.example.remediocerto.ui.main

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.remediocerto.R
import com.example.remediocerto.alarm.AgendaLocal
import com.example.remediocerto.alarm.AlarmeScheduler
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.databinding.ActivityMainBinding
import com.example.remediocerto.util.PermissoesHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Hospeda o NavHostFragment e a navegação inferior. As abas mudam conforme o
 * perfil: idoso (Hoje, Medicamentos, Histórico, Configurações) ou familiar
 * (Acompanhamento, Configurações).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Altura da barra de botões do sistema (voltar, início, recentes), informada pelo aparelho.
    private var espacoInferiorDoSistema = 0

    // Android 13 ou mais novo: a pessoa precisa permitir as notificações. Depois da resposta, vem a
    // pergunta sobre "Alarmes e lembretes" (que faz o aviso chegar no minuto exato).
    private val pedirNotificacoes =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { perguntarAlarmeExato() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reserva o topo e as laterais. O espaço de baixo é tratado em ajustarEspacoInferior().
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(barras.left, barras.top, barras.right, 0)
            espacoInferiorDoSistema = barras.bottom
            ajustarEspacoInferior()
            insets
        }

        val perfil = Perfil.valueOf(intent.getStringExtra(EXTRA_PERFIL) ?: Perfil.IDOSO.name)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        val (menu, inicio, abas) = when (perfil) {
            Perfil.IDOSO -> Triple(
                R.menu.menu_bottom_idoso,
                R.id.hojeFragment,
                setOf(R.id.hojeFragment, R.id.medicamentosFragment, R.id.historicoFragment, R.id.configuracoesFragment),
            )
            Perfil.FAMILIAR -> Triple(
                R.menu.menu_bottom_familiar,
                R.id.acompanhamentoFragment,
                setOf(R.id.acompanhamentoFragment, R.id.configuracoesFragment),
            )
        }

        val grafo = navController.navInflater.inflate(R.navigation.nav_graph)
        grafo.setStartDestination(inicio)
        navController.setGraph(grafo, null)

        binding.bottomNav.inflateMenu(menu)
        binding.bottomNav.setupWithNavController(navController)

        if (perfil == Perfil.IDOSO) configurarAvisosDeDose(primeiraVez = savedInstanceState == null)

        // A barra inferior só aparece nas telas principais de cada perfil.
        navController.addOnDestinationChangedListener { _, destino, _ ->
            binding.bottomNav.isVisible = destino.id in abas
            ajustarEspacoInferior()
        }
    }

    /**
     * Avisos de dose (10 min antes, 5 min antes e na hora). Enquanto o app está aberto, qualquer mudança nos
     * medicamentos (novo, editado, apagado) reagenda os avisos; eles continuam valendo com o app fechado.
     */
    private fun configurarAvisosDeDose(primeiraVez: Boolean) {
        if (primeiraVez) {
            if (PermissoesHelper.faltaPermissaoDeNotificacao(this)) {
                pedirNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                perguntarAlarmeExato()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Repositorios.medicamentos.observar()
                    .catch { /* sem lista nova: os avisos já agendados continuam valendo */ }
                    .collect { AlarmeScheduler.atualizar(applicationContext, it) }
            }
        }
    }

    // Pergunta uma única vez. Quem não permitir continua recebendo os avisos, só que o Android pode
    // atrasá-los em alguns minutos.
    private fun perguntarAlarmeExato() {
        val ajuste = PermissoesHelper.intentAjusteAlarmeExato(this) ?: return
        if (PermissoesHelper.podeAgendarExato(this) || AgendaLocal.jaPerguntouAlarmeExato(this)) return
        AgendaLocal.marcarPerguntouAlarmeExato(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.notif_exato_titulo)
            .setMessage(R.string.notif_exato_msg)
            .setPositiveButton(R.string.notif_permitir) { _, _ -> startActivity(ajuste) }
            .setNegativeButton(R.string.notif_agora_nao, null)
            .show()
    }

    // Com a barra de abas visível, ela cuida do espaço de baixo. Sem ela (cadastro, edição),
    // o conteúdo precisa desse espaço para não ficar sob os botões do celular.
    private fun ajustarEspacoInferior() {
        binding.navHost.updatePadding(
            bottom = if (binding.bottomNav.isVisible) 0 else espacoInferiorDoSistema
        )
    }

    companion object {
        const val EXTRA_PERFIL = "perfil"
    }
}
