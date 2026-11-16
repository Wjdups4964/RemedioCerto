package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.Perfil
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

/** Motivos de falha que a tela sabe explicar em português. */
enum class ErroAuth { CREDENCIAIS, EMAIL_EM_USO, SENHA_FRACA, REDE, MUITAS_TENTATIVAS, OUTRO }

/**
 * Conta do usuário: cadastro e login com e-mail e senha (Firebase Authentication).
 * O e-mail só é considerado válido depois que a pessoa toca no link enviado para a caixa de entrada.
 * Os callbacks rodam na thread principal; "null" no erro significa sucesso.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    init {
        auth.useAppLanguage() // e-mails de confirmação e de nova senha no idioma do celular
    }

    /** ID do usuário logado, ou null. É o mesmo ID usado em usuarios/{id}. */
    val usuarioId: String? get() = auth.currentUser?.uid
    val emailAtual: String? get() = auth.currentUser?.email
    val logado: Boolean get() = auth.currentUser != null
    val emailVerificado: Boolean get() = auth.currentUser?.isEmailVerified == true

    /** Sessões anônimas das versões de teste não servem mais: encerra para pedir login de verdade. */
    fun descartarSessaoAnonima() {
        if (auth.currentUser?.isAnonymous == true) auth.signOut()
    }

    /** Cria a conta, guarda o tipo de uso e o nome, e envia o e-mail de confirmação. */
    fun criarConta(email: String, senha: String, nome: String, perfil: Perfil, aoTerminar: (ErroAuth?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user
                if (usuario == null) {
                    aoTerminar(ErroAuth.OUTRO)
                    return@addOnSuccessListener
                }
                salvarCadastro(perfil, nome)
                // Se o envio falhar, a conta já existe: a tela de confirmação tem o botão "Reenviar".
                usuario.sendEmailVerification().addOnCompleteListener { aoTerminar(null) }
            }
            .addOnFailureListener { aoTerminar(traduzir(it)) }
    }

    fun entrar(email: String, senha: String, aoTerminar: (ErroAuth?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { aoTerminar(null) }
            .addOnFailureListener { aoTerminar(traduzir(it)) }
    }

    fun reenviarVerificacao(aoTerminar: (ErroAuth?) -> Unit) {
        val usuario = auth.currentUser ?: return aoTerminar(ErroAuth.OUTRO)
        usuario.sendEmailVerification()
            .addOnSuccessListener { aoTerminar(null) }
            .addOnFailureListener { aoTerminar(traduzir(it)) }
    }

    /** Pergunta ao servidor se o e-mail já foi confirmado (o celular não fica sabendo sozinho). */
    fun verificarEmail(aoTerminar: (verificado: Boolean, erro: ErroAuth?) -> Unit) {
        val usuario = auth.currentUser ?: return aoTerminar(false, ErroAuth.OUTRO)
        usuario.reload()
            .addOnSuccessListener { aoTerminar(auth.currentUser?.isEmailVerified == true, null) }
            .addOnFailureListener { aoTerminar(false, traduzir(it)) }
    }

    fun recuperarSenha(email: String, aoTerminar: (ErroAuth?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { aoTerminar(null) }
            .addOnFailureListener { aoTerminar(traduzir(it)) }
    }

    /** Guarda o tipo de uso e o nome informados no cadastro, sem apagar outros campos. */
    private fun salvarCadastro(perfil: Perfil, nome: String) {
        val uid = usuarioId ?: return
        db.collection("usuarios").document(uid)
            .set(
                mapOf("perfil" to perfil.name, "dados" to mapOf("nome" to nome)),
                SetOptions.merge(),
            )
    }

    /** Guarda o perfil escolhido no documento do usuário, sem apagar outros campos. */
    fun salvarPerfil(perfil: Perfil) {
        val uid = usuarioId ?: return
        db.collection("usuarios").document(uid)
            .set(mapOf("perfil" to perfil.name), SetOptions.merge())
    }

    /** Lê o perfil guardado (do servidor ou, sem internet, do cache do celular). Perfil null = nunca foi escolhido. */
    fun lerPerfil(aoTerminar: (perfil: Perfil?, erro: ErroAuth?) -> Unit) {
        val uid = usuarioId ?: return aoTerminar(null, ErroAuth.OUTRO)
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nome = doc.getString("perfil")
                aoTerminar(Perfil.values().firstOrNull { it.name == nome }, null)
            }
            .addOnFailureListener { aoTerminar(null, traduzir(it)) }
    }

    fun sair() = auth.signOut()

    private fun traduzir(e: Exception): ErroAuth = when {
        e is FirebaseNetworkException -> ErroAuth.REDE
        e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErroAuth.REDE
        e is FirebaseTooManyRequestsException -> ErroAuth.MUITAS_TENTATIVAS
        e is FirebaseAuthUserCollisionException -> ErroAuth.EMAIL_EM_USO
        e is FirebaseAuthWeakPasswordException -> ErroAuth.SENHA_FRACA // antes de InvalidCredentials: é filha dela
        e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException -> ErroAuth.CREDENCIAIS
        else -> ErroAuth.OUTRO
    }
}
