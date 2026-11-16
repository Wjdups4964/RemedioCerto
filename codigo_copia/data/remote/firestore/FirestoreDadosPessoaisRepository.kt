package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.model.DadosPessoais
import com.example.remediocerto.data.repository.DadosPessoaisRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Dados pessoais do usuário logado, no campo "dados" do documento usuarios/{id}. */
class FirestoreDadosPessoaisRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : DadosPessoaisRepository {

    private fun documento(): DocumentReference? =
        auth.currentUser?.uid?.let { db.collection("usuarios").document(it) }

    override fun observar(): Flow<DadosPessoais?> = callbackFlow {
        val doc = documento()
        if (doc == null) {
            Log.w(TAG, "observar: sem usuário logado")
            trySend(null)
            close()
            return@callbackFlow
        }
        val registro = doc.addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                Log.e(TAG, "Erro ao ler dados pessoais", erro)
                close(erro)
                return@addSnapshotListener
            }
            trySend(snapshot?.get("dados", DadosPessoais::class.java))
        }
        awaitClose { registro.remove() }
    }

    override fun salvar(dados: DadosPessoais) {
        val doc = documento() ?: run {
            Log.w(TAG, "salvar: sem usuário logado")
            return
        }
        // merge: não apaga o tipo de uso nem a chave de acessibilidade que moram no mesmo documento.
        doc.set(mapOf("dados" to dados), SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "Falha ao salvar dados pessoais", it) }
    }

    private companion object {
        const val TAG = "FirestoreDados"
    }
}
