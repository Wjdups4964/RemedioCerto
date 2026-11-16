package com.example.remediocerto.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Medicamento do usuário. Todos os campos têm valor padrão: o Firestore exige isso para montar o objeto.
 *
 * Agenda = três perguntas independentes:
 *  1. Quando, dentro do dia: [horarios] (sempre preenchido; no modo de intervalo é calculado a partir de
 *     [intervaloHoras] e [primeiraDose]).
 *  2. Em quais dias: a cada [intervaloDias] dias (1 = todo dia, 7 = uma vez por semana), contando de [inicioEm].
 *  3. Por quanto tempo: [duracaoDias] dias (0 = uso contínuo, sem data para terminar).
 */
data class Medicamento(
    @DocumentId val id: String = "",
    val nome: String = "",
    val dose: String = "",
    val forma: String = "",
    val observacoes: String = "",
    val adiamentoMin: Int = 10,                 // 5, 10 ou 15
    val tipoFrequencia: TipoFrequencia = TipoFrequencia.HORARIOS_FIXOS,
    val horarios: List<String> = emptyList(),   // "08:00", "14:00"
    val intervaloHoras: Int = 0,                // só no modo INTERVALO_HORAS: 2, 3, 4, 6, 8 ou 12
    val primeiraDose: String = "08:00",         // só no modo INTERVALO_HORAS
    val intervaloDias: Int = 1,
    val duracaoDias: Int = 0,
    val inicioEm: Long = 0,                     // início do 1º dia de tratamento, em milissegundos (0 = sem data)
)
