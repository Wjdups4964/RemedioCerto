package com.example.remediocerto.data.model

/** Como os horários do dia são definidos. */
enum class TipoFrequencia {
    /** O usuário escolhe cada horário (ex.: 08:00 e 20:00). */
    HORARIOS_FIXOS,

    /** O usuário escolhe de quantas em quantas horas (ex.: 8 em 8 horas) e a hora da primeira dose. */
    INTERVALO_HORAS,
}
