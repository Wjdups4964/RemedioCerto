package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.Perfil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** ID do usuário logado, ou null se ainda não entrou. É o mesmo ID usado em usuarios/{id}. */
    val usuarioId: String? get() = auth.currentUser?.uid

    /** Entra de forma anônima. Se já há usuário (dos acessos anteriores), só avisa que deu certo. */
    fun entrarAnonimo(onSucesso: () -> Unit, onErro: (Exception) -> Unit) {
        if (auth.currentUser != null) {
            onSucesso()
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { onSucesso() }
            .addOnFailureListener { onErro(it) }
    }

    /** Guarda o perfil escolhido no documento do usuário, sem apagar outros campos. */
    fun salvarPerfil(perfil: Perfil) {
        val uid = usuarioId ?: return
        db.collection("usuarios").document(uid)
            .set(mapOf("perfil" to perfil.name), SetOptions.merge())
    }
}