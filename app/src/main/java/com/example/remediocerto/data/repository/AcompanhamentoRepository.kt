package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.RegistroDose
import kotlinx.coroutines.flow.Flow

/** Leitura dos dados de um idoso vinculado (somente leitura: as regras do Firestore não deixam o familiar gravar). */
interface AcompanhamentoRepository {
    fun medicamentosDe(idosoUid: String): Flow<List<Medicamento>>
    fun registrosDe(idosoUid: String): Flow<List<RegistroDose>>
}
