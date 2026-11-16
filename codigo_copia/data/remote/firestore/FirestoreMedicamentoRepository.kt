package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.repository.MedicamentoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Medicamentos do usuário logado, em usuarios/{id}/medicamentos. */
class FirestoreMedicamentoRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MedicamentoRepository {

    private fun colecao(): CollectionReference? =
        auth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).collection("medicamentos")
        }

    override fun observar(): Flow<List<Medicamento>> = callbackFlow {
        val col = colecao()
        if (col == null) {
            Log.w(TAG, "observar: sem usuário logado")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registro = col.addSnapshotListener { snapshot, erro ->
            if (erro != null) {
                Log.e(TAG, "Erro ao ler medicamentos", erro)
                close(erro)
                return@addSnapshotListener
            }
            val lista = snapshot?.toObjects(Medicamento::class.java).orEmpty()
            trySend(lista.sortedBy { it.nome.lowercase() })
        }
        awaitClose { registro.remove() }
    }

    override fun salvar(medicamento: Medicamento) {
        val col = colecao() ?: run {
            Log.w(TAG, "salvar: sem usuário logado")
            return
        }
        // Sem id: cria um documento novo. Com id: atualiza o existente.
        val doc = if (medicamento.id.isBlank()) col.document() else col.document(medicamento.id)
        doc.set(medicamento.copy(id = doc.id))
            .addOnFailureListener { Log.e(TAG, "Falha ao salvar medicamento", it) }
    }

    override fun apagar(id: String) {
        val col = colecao() ?: return
        col.document(id).delete()
            .addOnFailureListener { Log.e(TAG, "Falha ao apagar medicamento", it) }
    }

    private companion object {
        const val TAG = "FirestoreMedicamento"
    }
}