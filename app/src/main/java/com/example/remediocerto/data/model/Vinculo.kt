package com.example.remediocerto.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Ligação entre um idoso e um familiar. Fica em vinculos/{idosoUid}_{familiarUid}: o id já impede
 * o mesmo par de se vincular duas vezes. Os nomes são copiados na criação para a lista aparecer sem
 * precisar ler o perfil da outra pessoa.
 */
data class Vinculo(
    @DocumentId val id: String = "",
    val idosoUid: String = "",
    val idosoNome: String = "",
    val familiarUid: String = "",
    val familiarNome: String = "",
    val codigo: String = "",
    val criadoEm: Date = Date(0),
)

/** Por que não foi possível criar o vínculo. A tela traduz cada motivo para uma mensagem. */
enum class MotivoFalhaVinculo { CODIGO_INVALIDO, EXPIRADO, JA_USADO, JA_VINCULADO, PROPRIO_CODIGO, SEM_INTERNET, OUTRO }

sealed interface ResultadoVinculo {
    data class Sucesso(val vinculo: Vinculo) : ResultadoVinculo
    data class Falha(val motivo: MotivoFalhaVinculo) : ResultadoVinculo
}
