package com.example.remediocerto.data.model

import java.util.Date

/** Quantas horas um código de convite vale (regra RN10). Mude aqui e em firestore.rules (limite máximo de 2 dias). */
const val CONVITE_VALIDADE_HORAS = 24

/** Quantos dígitos tem o código. 8 dígitos = 100 milhões de combinações, difícil de adivinhar. */
const val CONVITE_DIGITOS = 8

/**
 * Convite criado pelo idoso para um familiar. Fica em convites/{codigo}; o próprio código é o id do documento.
 * Os campos de data são [Date] porque o Firestore guarda como Timestamp, e as regras de segurança comparam
 * a validade com o relógio do servidor (não dá para burlar mudando a hora do celular).
 */
data class Convite(
    val codigo: String = "",
    val idosoUid: String = "",
    val idosoNome: String = "",
    val criadoEm: Date = Date(0),
    val expiraEm: Date = Date(0),
    val usado: Boolean = false,
    val usadoPor: String = "",
) {
    /** Confere só no celular, para avisar rápido. Quem decide de verdade é o servidor. */
    fun vencidoEm(agora: Long): Boolean = agora >= expiraEm.time
}

/** Deixa só os dígitos: a pessoa pode digitar "1234 5678" ou "1234-5678". */
fun normalizarCodigo(texto: String): String = texto.filter { it.isDigit() }

/** "12345678" -> "1234 5678", para ler em voz alta ou ditar por telefone. */
fun formatarCodigo(codigo: String): String =
    if (codigo.length == CONVITE_DIGITOS) "${codigo.substring(0, 4)} ${codigo.substring(4)}" else codigo

fun codigoCompleto(codigo: String): Boolean = codigo.length == CONVITE_DIGITOS && codigo.all { it.isDigit() }
