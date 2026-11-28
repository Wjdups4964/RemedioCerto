package com.example.remediocerto.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.example.remediocerto.alarm.idNotificacaoDaDose
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import com.example.remediocerto.databinding.ActivityMainBinding
import com.example.remediocerto.notification.NotificacaoHelper
import com.example.remediocerto.ui.tutorial.TutorialActivity
import com.example.remediocerto.util.PermissoesHelper
import com.example.remediocerto.util.TamanhoFonte
import com.example.remediocerto.util.Tutorial
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Hospeda o NavHostFragment e a navegação inferior. As abas mudam conforme o
 * modo: Uso próprio (Hoje, Medicamentos, Histórico, Configurações) ou Acompanhante
 * (Acompanhamento, Configurações). A troca fica em Configurações.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Altura da barra de botões do sistema (voltar, início, recentes), informada pelo aparelho.
    private var espacoInferiorDoSistema = 0

    // Android 13 ou mais novo: a pessoa precisa permitir as notificações. Depois da resposta, vem a
    // pergunta sobre "Alarmes e lembretes" (que faz o aviso chegar no minuto exato).
    private val pedirNotificacoes =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { perguntarAlarmeExato() }

    // Na primeira vez da conta neste celular, o tutorial vem antes dos pedidos de permissão (senão eles
    // apareceriam por cima dele). Quando o tutorial fecha, seguem os pedidos.
    private val abrirTutorial =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { pedirPermissoesDosAvisos() }

    // Só o modo "Uso próprio" recebe avisos de dose.
    private var avisosAtivos = false

    // Aplica o tamanho de letra escolhido (padrão ou modo de acessibilidade).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TamanhoFonte.aplicar(newBase))
    }

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

        avisosAtivos = perfil == Perfil.IDOSO
        if (avisosAtivos) configurarAvisosDeDose()
        if (savedInstanceState == null) {
            if (Tutorial.precisaMostrar(this)) {
                abrirTutorial.launch(Intent(this, TutorialActivity::class.java))
            } else {
                pedirPermissoesDosAvisos()
            }
        }

        // Botão de voltar: igual ao botão "voltar" do celular, em todas as telas internas.
        binding.btnVoltarGlobal.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // A barra inferior só aparece nas telas principais de cada perfil.
        // Nas demais telas (cadastro, detalhes, vínculo, farmácias...) aparece o botão de voltar.
        navController.addOnDestinationChangedListener { _, destino, _ ->
            binding.bottomNav.isVisible = destino.id in abas
            binding.btnVoltarGlobal.isVisible = destino.id !in abas
            ajustarEspacoInferior()
        }
    }

    /**
     * Avisos de dose (10 min antes, 5 min antes e na hora). Enquanto o app está aberto, qualquer mudança nos
     * medicamentos (novo, editado, apagado) reagenda os avisos; eles continuam valendo com o app fechado.
     */
    private fun configurarAvisosDeDose() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    Repositorios.medicamentos.observar()
                        .catch { /* sem lista nova: os avisos já agendados continuam valendo */ }
                        .collect { AlarmeScheduler.atualizar(applicationContext, it) }
                }
                // Dose tomada (aqui no app, ou em outro aparelho): o alarme que ainda toca ou está
                // esperando por um "Adiar" dessa dose não faz mais sentido.
                launch {
                    Repositorios.doses.observar()
                        .catch { /* sem registros novos: nada a cancelar */ }
                        .collect { registros -> silenciarDosesTomadas(registros) }
                }
            }
        }
    }

    // Primeira abertura: pede a permissão de notificações (Android 13 ou mais novo) e, depois, a de alarmes exatos.
    private fun pedirPermissoesDosAvisos() {
        if (!avisosAtivos) return
        if (PermissoesHelper.faltaPermissaoDeNotificacao(this)) {
            pedirNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            perguntarAlarmeExato()
        }
    }

    private fun silenciarDosesTomadas(registros: List<RegistroDose>) {
        val limite = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
        registros.filter { it.status == StatusDose.TOMADA && it.previstoEm >= limite }.forEach {
            val id = idNotificacaoDaDose(it.medicamentoId, it.previstoEm)
            AlarmeScheduler.cancelarAdiamento(applicationContext, id)
            NotificacaoHelper.cancelar(applicationContext, id)
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
