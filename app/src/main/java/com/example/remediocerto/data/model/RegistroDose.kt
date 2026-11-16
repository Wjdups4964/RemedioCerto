package com.example.remediocerto.data.model

import com.google.firebase.firestore.DocumentId

/** Registro do que aconteceu com uma dose: alimenta o Histórico e a adesão. */
data class RegistroDose(
    @DocumentId val id: String = "",
    val medicamentoId: String = "",
    val medicamentoNome: String = "",
    val previstoEm: Long = 0,    // data e hora prevista, em milissegundos
    val registradoEm: Long = 0,  // quando o usuário agiu, em milissegundos
    val status: StatusDose = StatusDose.TOMADA
)