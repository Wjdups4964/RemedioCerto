package com.example.remediocerto.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.databinding.ActivityMainBinding

/**
 * Hospeda o NavHostFragment e a navegação inferior. As abas mudam conforme o
 * perfil: idoso (Hoje, Medicamentos, Histórico, Configurações) ou familiar
 * (Acompanhamento, Configurações).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Altura da barra de botões do sistema (voltar, início, recentes), informada pelo aparelho.
    private var espacoInferiorDoSistema = 0

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

        // A barra inferior só aparece nas telas principais de cada perfil.
        navController.addOnDestinationChangedListener { _, destino, _ ->
            binding.bottomNav.isVisible = destino.id in abas
            ajustarEspacoInferior()
        }
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
