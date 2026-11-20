package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.Convite
import com.example.remediocerto.data.model.ResultadoVinculo
import com.example.remediocerto.data.model.Vinculo
import kotlinx.coroutines.flow.Flow

/** Convites e vínculos entre idoso e familiar. */
interface VinculoRepository {
    /** Idoso: cria um código novo, válido por [com.example.remediocerto.data.model.CONVITE_VALIDADE_HORAS] horas. Null se falhar. */
    suspend fun gerarConvite(nomeIdoso: String): Convite?

    /** Idoso: quem acompanha você. */
    fun observarDoIdoso(): Flow<List<Vinculo>>

    /** Familiar: quem você acompanha. */
    fun observarDoFamiliar(): Flow<List<Vinculo>>

    /** Familiar: usa o código recebido. Precisa de internet (a validade é conferida no servidor). */
    suspend fun usarConvite(codigo: String, nomeFamiliar: String): ResultadoVinculo

    /** Idoso revoga o acesso, ou familiar deixa de acompanhar. Devolve false se falhar. */
    suspend fun encerrar(vinculoId: String): Boolean
}
