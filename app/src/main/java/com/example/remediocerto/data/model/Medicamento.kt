package com.example.remediocerto.data.model

import com.google.firebase.firestore.DocumentId

/** Medicamento do usuário. Todos os campos têm valor padrão: o Firestore exige isso para montar o objeto. */
data class Medicamento(
    @DocumentId val id: String = "",
    val nome: String = "",
    val dose: String = "",
    val forma: String = "",
    val observacoes: String = "",
    val tratamentoContinuo: Boolean = false,
    val todosOsDias: Boolean = true,
    val adiamentoMin: Int = 10,        // 5, 10 ou 15
    val horarios: List<String> = emptyList() // "08:00", "14:00"
)