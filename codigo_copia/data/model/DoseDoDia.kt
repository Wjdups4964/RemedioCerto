package com.example.remediocerto.data.model

/** Onde a dose está em relação ao relógio: ainda vai chegar, é a hora, ou já passou. */
enum class Momento { FUTURA, AGORA, ATRASADA }

/**
 * Uma dose prevista para hoje, montada a partir do medicamento + horário + registro (se houver).
 * [status] é null enquanto o usuário ainda não registrou nada para esta dose.
 */
data class DoseDoDia(
    val medicamentoId: String,
    val nome: String,
    val dose: String,
    val hora: String,
    val previstoEm: Long,
    val adiamentoMin: Int,
    val status: StatusDose?,
    val momento: Momento,
) {
    val chave: String get() = "${medicamentoId}_$previstoEm"
}
