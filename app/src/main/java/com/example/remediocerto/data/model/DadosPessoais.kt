package com.example.remediocerto.data.model

enum class Sexo { FEMININO, MASCULINO, OUTRO, NAO_INFORMADO }

/**
 * Dados pessoais do usuário ("Meu perfil"). Ficam dentro do documento usuarios/{id}, no campo "dados".
 * Só o nome é obrigatório; o resto é opcional. Não confundir com [Perfil], que é o tipo de uso (idoso ou familiar).
 */
data class DadosPessoais(
    val nome: String = "",
    /** Data de nascimento: meia-noite UTC do dia, em milissegundos (0 = não informada). A idade é calculada na hora. */
    val nascimentoEm: Long = 0,
    val sexo: Sexo = Sexo.NAO_INFORMADO,
    val contatoEmergencia: String = "",
)
