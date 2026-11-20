package com.example.remediocerto.data.remote.firestore

import android.util.Log
import com.example.remediocerto.data.model.CONVITE_DIGITOS
import com.example.remediocerto.data.model.CONVITE_VALIDADE_HORAS
import com.example.remediocerto.data.model.Convite
import com.example.remediocerto.data.model.MotivoFalhaVinculo
import com.example.remediocerto.data.model.ResultadoVinculo
import com.example.remediocerto.data.model.Vinculo
import com.example.remediocerto.data.model.codigoCompleto
import com.example.remediocerto.data.repository.VinculoRepository
import com.example.remediocerto.util.aguardar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import java.security.SecureRandom
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Convites em convites/{codigo} e vínculos em vinculos/{idosoUid}_{familiarUid}.
 * Quem garante a validade do código e o acesso aos dados do idoso são as regras do Firestore
 * (arquivo firestore.rules na raiz do projeto); o código do app só confere antes, para dar mensagens claras.
 */
class FirestoreVinculoRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : VinculoRepository {

    private val aleatorio = SecureRandom()

    override suspend fun gerarConvite(nomeIdoso: String): Convite? {
        val uid = auth.currentUser?.uid ?: return null
        repeat(5) { // código repetido é raríssimo, mas se acontecer sorteia outro
            val codigo = sortearCodigo()
            val agora = System.currentTimeMillis()
            val convite = Convite(
                codigo = codigo,
                idosoUid = uid,
                idosoNome = nomeIdoso,
                criadoEm = Date(agora),
                expiraEm = Date(agora + CONVITE_VALIDADE_HORAS * 60 * 60 * 1000L),
            )
            val criou = try {
                db.runTransaction(Transaction.Function<Boolean> { tx ->
                    val ref = db.collection("convites").document(codigo)
                    if (tx.get(ref).exists()) {
                        false
                    } else {
                        tx.set(ref, convite)
                        true
                    }
                }).aguardar()
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao gerar convite", e)
                return null
            }
            if (criou) return convite
        }
        return null
    }

    override fun observarDoIdoso(): Flow<List<Vinculo>> = observar("idosoUid")

    override fun observarDoFamiliar(): Flow<List<Vinculo>> = observar("familiarUid")

    private fun observar(campo: String): Flow<List<Vinculo>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registro = db.collection("vinculos").whereEqualTo(campo, uid)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    Log.e(TAG, "Erro ao ler vínculos", erro)
                    close(erro)
                    return@addSnapshotListener
                }
                val lista = snapshot?.toObjects(Vinculo::class.java).orEmpty()
                trySend(lista.sortedBy { it.criadoEm })
            }
        awaitClose { registro.remove() }
    }

    override suspend fun usarConvite(codigo: String, nomeFamiliar: String): ResultadoVinculo {
        val uid = auth.currentUser?.uid ?: return ResultadoVinculo.Falha(MotivoFalhaVinculo.OUTRO)
        if (!codigoCompleto(codigo)) return ResultadoVinculo.Falha(MotivoFalhaVinculo.CODIGO_INVALIDO)
        return try {
            db.runTransaction(Transaction.Function<ResultadoVinculo> { tx ->
                val conviteRef = db.collection("convites").document(codigo)
                val convite = tx.get(conviteRef).toObject(Convite::class.java)
                    ?: return@Function ResultadoVinculo.Falha(MotivoFalhaVinculo.CODIGO_INVALIDO)
                if (convite.idosoUid == uid) return@Function ResultadoVinculo.Falha(MotivoFalhaVinculo.PROPRIO_CODIGO)
                if (convite.usado) return@Function ResultadoVinculo.Falha(MotivoFalhaVinculo.JA_USADO)
                if (convite.vencidoEm(System.currentTimeMillis())) {
                    return@Function ResultadoVinculo.Falha(MotivoFalhaVinculo.EXPIRADO)
                }
                val vinculoRef = db.collection("vinculos").document("${convite.idosoUid}_$uid")
                if (tx.get(vinculoRef).exists()) {
                    return@Function ResultadoVinculo.Falha(MotivoFalhaVinculo.JA_VINCULADO)
                }
                tx.set(
                    vinculoRef,
                    mapOf(
                        "idosoUid" to convite.idosoUid,
                        "idosoNome" to convite.idosoNome,
                        "familiarUid" to uid,
                        "familiarNome" to nomeFamiliar,
                        "codigo" to codigo,
                        "criadoEm" to FieldValue.serverTimestamp(),
                    ),
                )
                tx.update(conviteRef, mapOf("usado" to true, "usadoPor" to uid))
                ResultadoVinculo.Sucesso(
                    Vinculo(
                        id = vinculoRef.id,
                        idosoUid = convite.idosoUid,
                        idosoNome = convite.idosoNome,
                        familiarUid = uid,
                        familiarNome = nomeFamiliar,
                        codigo = codigo,
                    )
                )
            }).aguardar()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Falha ao usar convite", e)
            ResultadoVinculo.Falha(
                when (e.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> MotivoFalhaVinculo.SEM_INTERNET
                    // As regras recusaram: o servidor achou o código vencido ou já usado.
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> MotivoFalhaVinculo.EXPIRADO
                    else -> MotivoFalhaVinculo.OUTRO
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao usar convite", e)
            ResultadoVinculo.Falha(MotivoFalhaVinculo.OUTRO)
        }
    }

    override suspend fun encerrar(vinculoId: String): Boolean {
        val tarefa = db.collection("vinculos").document(vinculoId).delete()
        // Offline, a exclusão fica na fila e só termina com internet: não esperamos para sempre.
        return withTimeoutOrNull(10_000) { tarefa.aguardar(); true } ?: false
    }

    private fun sortearCodigo(): String {
        val maximo = Math.pow(10.0, CONVITE_DIGITOS.toDouble()).toLong()
        return (aleatorio.nextLong().mod(maximo)).toString().padStart(CONVITE_DIGITOS, '0')
    }

    private companion object {
        const val TAG = "FirestoreVinculo"
    }
}
