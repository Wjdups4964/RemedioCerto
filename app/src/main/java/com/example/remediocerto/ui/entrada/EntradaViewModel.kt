package com.example.remediocerto.ui.entrada

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.data.repository.AuthRepository
import com.example.remediocerto.data.repository.ErroAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/** Qual parte da tela de entrada aparece. */
enum class Painel { CARREGANDO, LOGIN, CADASTRO, VERIFICACAO }

/** Campo onde a mensagem de erro deve aparecer. Sem campo, a mensagem vira um aviso rápido (Toast). */
enum class Campo { LOGIN_EMAIL, LOGIN_SENHA, CAD_NOME, CAD_EMAIL, CAD_SENHA, CAD_CONFIRMA }

/** Coisas que a ViewModel pede para a tela fazer uma vez só. */
sealed interface EventoEntrada {
    data class Mensagem(@StringRes val texto: Int, val campo: Campo? = null) : EventoEntrada
    data class Entrar(val perfil: Perfil) : EventoEntrada
    /** Conta sem perfil guardado: a tela pergunta como a pessoa vai usar o app. */
    object EscolherPerfil : EventoEntrada
}

private const val TAMANHO_MINIMO_SENHA = 8

/**
 * Regras do cadastro e do login com e-mail e senha. Fluxo:
 * criar conta -> e-mail de confirmação -> "Já confirmei" -> app.
 * Quem já confirmou entra direto nas próximas vezes.
 */
class EntradaViewModel : ViewModel() {

    private val auth = AuthRepository()

    private val _painel = MutableStateFlow(Painel.CARREGANDO)
    val painel: StateFlow<Painel> = _painel.asStateFlow()

    private val _ocupado = MutableStateFlow(false)
    val ocupado: StateFlow<Boolean> = _ocupado.asStateFlow()

    private val _emailVerificacao = MutableStateFlow("")
    val emailVerificacao: StateFlow<String> = _emailVerificacao.asStateFlow()

    private val _eventos = Channel<EventoEntrada>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    private var iniciado = false

    /** Decide a primeira tela: login, confirmação de e-mail pendente, ou direto para o app. */
    fun iniciar() {
        if (iniciado) return
        iniciado = true
        auth.descartarSessaoAnonima()
        when {
            !auth.logado -> _painel.value = Painel.LOGIN
            !auth.emailVerificado -> irParaVerificacao()
            else -> entrarNoApp()
        }
    }

    fun mostrarLogin() { _painel.value = Painel.LOGIN }

    fun mostrarCadastro() { _painel.value = Painel.CADASTRO }

    fun entrar(email: String, senha: String) {
        val e = email.trim()
        if (!emailValido(e)) return avisar(R.string.erro_email_invalido, Campo.LOGIN_EMAIL)
        if (senha.isEmpty()) return avisar(R.string.erro_senha_vazia, Campo.LOGIN_SENHA)

        _ocupado.value = true
        auth.entrar(e, senha) { erro ->
            if (erro != null) falhar(erro)
            else if (!auth.emailVerificado) irParaVerificacao()
            else entrarNoApp()
        }
    }

    fun criarConta(
        nome: String,
        email: String,
        senha: String,
        confirmacao: String,
        perfil: Perfil?,
        aceitouAviso: Boolean,
    ) {
        val e = email.trim()
        if (nome.isBlank()) return avisar(R.string.erro_nome, Campo.CAD_NOME)
        if (!emailValido(e)) return avisar(R.string.erro_email_invalido, Campo.CAD_EMAIL)
        if (senha.length < TAMANHO_MINIMO_SENHA) return avisar(R.string.erro_senha_curta, Campo.CAD_SENHA)
        if (senha != confirmacao) return avisar(R.string.erro_senha_diferente, Campo.CAD_CONFIRMA)
        if (perfil == null) return avisar(R.string.erro_perfil)
        if (!aceitouAviso) return avisar(R.string.entrada_aceite_obrigatorio)

        _ocupado.value = true
        auth.criarConta(e, senha, nome.trim(), perfil) { erro ->
            if (erro != null) {
                falhar(erro, Campo.CAD_EMAIL)
            } else {
                avisar(R.string.verif_enviada)
                irParaVerificacao()
            }
        }
    }

    /** "Já confirmei": busca no servidor se o link do e-mail já foi aberto. */
    fun jaConfirmei() {
        _ocupado.value = true
        auth.verificarEmail { verificado, erro ->
            when {
                erro != null -> falhar(erro)
                verificado -> entrarNoApp()
                else -> {
                    _ocupado.value = false
                    avisar(R.string.verif_pendente)
                }
            }
        }
    }

    fun reenviar() {
        _ocupado.value = true
        auth.reenviarVerificacao { erro ->
            if (erro != null) falhar(erro)
            else {
                _ocupado.value = false
                avisar(R.string.verif_reenviada)
            }
        }
    }

    /** Sempre responde igual, exista ou não a conta, para não revelar quem tem cadastro. */
    fun recuperarSenha(email: String) {
        val e = email.trim()
        if (!emailValido(e)) return avisar(R.string.erro_email_invalido, Campo.LOGIN_EMAIL)

        _ocupado.value = true
        auth.recuperarSenha(e) { erro ->
            if (erro == ErroAuth.REDE || erro == ErroAuth.MUITAS_TENTATIVAS) falhar(erro)
            else {
                _ocupado.value = false
                avisar(R.string.recuperar_enviada)
            }
        }
    }

    /** Volta ao login para entrar com outro e-mail. */
    fun trocarConta() {
        auth.sair()
        _painel.value = Painel.LOGIN
    }

    /** Resposta do diálogo "como você vai usar o app?" (conta sem perfil guardado). */
    fun definirPerfil(perfil: Perfil) {
        auth.salvarPerfil(perfil)
        _eventos.trySend(EventoEntrada.Entrar(perfil))
    }

    private fun irParaVerificacao() {
        _emailVerificacao.value = auth.emailAtual.orEmpty()
        _ocupado.value = false
        _painel.value = Painel.VERIFICACAO
    }

    private fun entrarNoApp() {
        _ocupado.value = true
        _painel.value = Painel.CARREGANDO
        auth.lerPerfil { perfil, erro ->
            _ocupado.value = false
            when {
                erro != null -> {
                    _painel.value = Painel.LOGIN
                    avisar(mensagemDe(erro))
                }
                perfil == null -> _eventos.trySend(EventoEntrada.EscolherPerfil)
                else -> _eventos.trySend(EventoEntrada.Entrar(perfil))
            }
        }
    }

    private fun falhar(erro: ErroAuth, campoDoEmail: Campo? = null) {
        _ocupado.value = false
        when (erro) {
            ErroAuth.EMAIL_EM_USO -> avisar(R.string.erro_email_em_uso, campoDoEmail)
            ErroAuth.SENHA_FRACA -> avisar(R.string.erro_senha_fraca, Campo.CAD_SENHA)
            else -> avisar(mensagemDe(erro))
        }
    }

    private fun mensagemDe(erro: ErroAuth): Int = when (erro) {
        ErroAuth.CREDENCIAIS -> R.string.erro_credenciais
        ErroAuth.EMAIL_EM_USO -> R.string.erro_email_em_uso
        ErroAuth.SENHA_FRACA -> R.string.erro_senha_fraca
        ErroAuth.REDE -> R.string.erro_rede
        ErroAuth.MUITAS_TENTATIVAS -> R.string.erro_muitas_tentativas
        ErroAuth.OUTRO -> R.string.erro_generico
    }

    private fun avisar(@StringRes texto: Int, campo: Campo? = null) {
        _eventos.trySend(EventoEntrada.Mensagem(texto, campo))
    }

    private fun emailValido(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
