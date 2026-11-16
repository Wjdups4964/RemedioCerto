package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.repository.ConfiguracaoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Configurações do usuário, no próprio documento usuarios/{id} (campo "acessibilidade"). */
class FirestoreConfiguracaoRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ConfiguracaoRepository {

    private fun documento(): DocumentReference? =
        auth.currentUser?.uid?.let { db.collection("usuarios").document(it) }

    override fun observarAcessibilidade(): Flow<Boolean> = callbackFlow {
        val doc = documento()
        if (doc == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val registro = doc.addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                Log.e(TAG, "Erro ao ler configurações", erro)
                close(erro)
                return@addSnapshotListener
            }
            trySend(snapshot?.getBoolean("acessibilidade") ?: false)
        }
        awaitClose { registro.remove() }
    }

    override fun salvarAcessibilidade(ativo: Boolean) {
        val doc = documento() ?: run {
            Log.w(TAG, "salvarAcessibilidade: sem usuário logado")
            return
        }
        doc.set(mapOf("acessibilidade" to ativo), SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "Falha ao salvar acessibilidade", it) }
    }

    private companion object {
        const val TAG = "FirestoreConfiguracao"
    }
}