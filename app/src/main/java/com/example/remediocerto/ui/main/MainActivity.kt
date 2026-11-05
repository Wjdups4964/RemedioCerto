package com.example.remediocerto.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.databinding.ActivityMainBinding
import com.example.remediocerto.util.aplicarInsetsDoSistema

/**
 * Hospeda o NavHostFragment e a navegação inferior. As abas mudam conforme o
 * perfil: idoso (Hoje, Medicamentos, Histórico, Configurações) ou familiar
 * (Acompanhamento, Configurações).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

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
        }
    }

    companion object {
        const val EXTRA_PERFIL = "perfil"
    }
}
