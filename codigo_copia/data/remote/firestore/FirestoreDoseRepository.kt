package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.repository.DoseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Registros de dose do usuário logado, em usuarios/{id}/registros. Alimentam o Histórico e a adesão. */
class FirestoreDoseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DoseRepository {

    private fun colecao(): CollectionReference? =
        auth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).collection("registros")
        }

    override fun observar(): Flow<List<RegistroDose>> = callbackFlow {
        val col = colecao()
        if (col == null) {
            Log.w(TAG, "observar: sem usuário logado")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registro = col.addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                Log.e(TAG, "Erro ao ler registros", erro)
                close(erro)
                return@addSnapshotListener
            }
            val lista = snapshot?.toObjects(RegistroDose::class.java).orEmpty()
            trySend(lista.sortedByDescending { it.previstoEm }) // mais recentes primeiro
        }
        awaitClose { registro.remove() }
    }

    override fun registrar(registro: RegistroDose) {
        val col = colecao() ?: run {
            Log.w(TAG, "registrar: sem usuário logado")
            return
        }
        // Um documento por dose prevista: tocar "Tomei" duas vezes sobrescreve, não duplica.
        val idDaDose = when {
            registro.id.isNotBlank() -> registro.id
            registro.medicamentoId.isNotBlank() -> "${registro.medicamentoId}_${registro.previstoEm}"
            else -> col.document().id
        }
        col.document(idDaDose)
            .set(registro.copy(id = idDaDose))
            .addOnFailureListener { Log.e(TAG, "Falha ao registrar dose", it) }
    }

    private companion object {
        const val TAG = "FirestoreDose"
    }
}