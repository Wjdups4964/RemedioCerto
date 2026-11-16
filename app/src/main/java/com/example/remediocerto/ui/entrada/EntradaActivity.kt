package com.example.remediocerto.ui.entrada

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.databinding.ActivityEntradaBinding
import com.example.remediocerto.ui.main.MainActivity
import com.example.remediocerto.util.aplicarInsetsDoSistema
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Tela inicial do app: entrar, criar conta e confirmar o e-mail. Quem já entrou e confirmou
 * o e-mail passa direto para o app. As regras ficam na [EntradaViewModel].
 */
class EntradaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntradaBinding
    private val viewModel: EntradaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntradaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        configurarCliques()
        limparErrosAoDigitar()
        observarEstado()

        viewModel.iniciar()
    }

    private fun configurarCliques() {
        binding.btnEntrar.setOnClickListener {
            viewModel.entrar(
                email = binding.etLoginEmail.text?.toString().orEmpty(),
                senha = binding.etLoginSenha.text?.toString().orEmpty(),
            )
        }
        binding.btnEsqueciSenha.setOnClickListener {
            viewModel.recuperarSenha(binding.etLoginEmail.text?.toString().orEmpty())
        }
        binding.btnIrCadastro.setOnClickListener { viewModel.mostrarCadastro() }

        binding.btnCriarConta.setOnClickListener {
            val perfil = when (binding.togglePerfil.checkedButtonId) {
                R.id.btnIdoso -> Perfil.IDOSO
                R.id.btnFamiliar -> Perfil.FAMILIAR
                else -> null
            }
            viewModel.criarConta(
                nome = binding.etCadNome.text?.toString().orEmpty(),
                email = binding.etCadEmail.text?.toString().orEmpty(),
                senha = binding.etCadSenha.text?.toString().orEmpty(),
                confirmacao = binding.etCadConfirma.text?.toString().orEmpty(),
                perfil = perfil,
                aceitouAviso = binding.checkAviso.isChecked,
            )
        }
        binding.btnVoltarLogin.setOnClickListener { viewModel.mostrarLogin() }

        binding.btnJaConfirmei.setOnClickListener { viewModel.jaConfirmei() }
        binding.btnReenviar.setOnClickListener { viewModel.reenviar() }
        binding.btnTrocarConta.setOnClickListener { viewModel.trocarConta() }
    }

    private fun limparErrosAoDigitar() {
        binding.etLoginEmail.doAfterTextChanged { binding.tilLoginEmail.error = null }
        binding.etLoginSenha.doAfterTextChanged { binding.tilLoginSenha.error = null }
        binding.etCadNome.doAfterTextChanged { binding.tilCadNome.error = null }
        binding.etCadEmail.doAfterTextChanged { binding.tilCadEmail.error = null }
        binding.etCadSenha.doAfterTextChanged { binding.tilCadSenha.error = null }
        binding.etCadConfirma.doAfterTextChanged { binding.tilCadConfirma.error = null }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.painel.collect { mostrarPainel(it) } }
                launch { viewModel.ocupado.collect { mostrarOcupado(it) } }
                launch {
                    viewModel.emailVerificacao.collect {
                        binding.tvVerificacaoTexto.text = getString(R.string.entrada_verif_texto, it)
                    }
                }
                launch { viewModel.eventos.collect { tratar(it) } }
            }
        }
    }

    private fun mostrarPainel(painel: Painel) {
        binding.painelLogin.isVisible = painel == Painel.LOGIN
        binding.painelCadastro.isVisible = painel == Painel.CADASTRO
        binding.painelVerificacao.isVisible = painel == Painel.VERIFICACAO
    }

    private fun mostrarOcupado(ocupado: Boolean) {
        binding.progresso.isVisible = ocupado
        listOf(
            binding.btnEntrar, binding.btnEsqueciSenha, binding.btnIrCadastro,
            binding.btnCriarConta, binding.btnVoltarLogin,
            binding.btnJaConfirmei, binding.btnReenviar, binding.btnTrocarConta,
        ).forEach { it.isEnabled = !ocupado }
    }

    private fun tratar(evento: EventoEntrada) {
        when (evento) {
            is EventoEntrada.Mensagem -> mostrarMensagem(evento)
            is EventoEntrada.Entrar -> abrirApp(evento.perfil)
            EventoEntrada.EscolherPerfil -> perguntarPerfil()
        }
    }

    private fun mostrarMensagem(evento: EventoEntrada.Mensagem) {
        val texto = getString(evento.texto)
        when (evento.campo) {
            Campo.LOGIN_EMAIL -> binding.tilLoginEmail.error = texto
            Campo.LOGIN_SENHA -> binding.tilLoginSenha.error = texto
            Campo.CAD_NOME -> binding.tilCadNome.error = texto
            Campo.CAD_EMAIL -> binding.tilCadEmail.error = texto
            Campo.CAD_SENHA -> binding.tilCadSenha.error = texto
            Campo.CAD_CONFIRMA -> binding.tilCadConfirma.error = texto
            null -> Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
        }
    }

    private fun perguntarPerfil() {
        val opcoes = arrayOf(getString(R.string.entrada_btn_idoso), getString(R.string.entrada_btn_familiar))
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.entrada_escolher_perfil)
            .setCancelable(false)
            .setItems(opcoes) { _, indice ->
                viewModel.definirPerfil(if (indice == 0) Perfil.IDOSO else Perfil.FAMILIAR)
            }
            .show()
    }

    private fun abrirApp(perfil: Perfil) {
        startActivity(
            Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_PERFIL, perfil.name)
        )
        finish()
    }
}
