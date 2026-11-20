package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.repository.AcompanhamentoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Lê usuarios/{idosoUid}/medicamentos e /registros. Só funciona enquanto existir o vínculo (regra do Firestore). */
class FirestoreAcompanhamentoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AcompanhamentoRepository {

    override fun medicamentosDe(idosoUid: String): Flow<List<Medicamento>> =
        observar(idosoUid, "medicamentos", Medicamento::class.java)

    override fun registrosDe(idosoUid: String): Flow<List<RegistroDose>> =
        observar(idosoUid, "registros", RegistroDose::class.java)

    private fun <T : Any> observar(idosoUid: String, subcolecao: String, classe: Class<T>): Flow<List<T>> = callbackFlow {
        val registro = db.collection("usuarios").document(idosoUid).collection(subcolecao)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Acontece, por exemplo, quando o idoso revoga o vínculo com a tela aberta.
                    Log.w(TAG, "Sem acesso a $subcolecao de $idosoUid", erro)
                    close(erro)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(classe).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    private companion object {
        const val TAG = "FirestoreAcompanhamento"
    }
}
