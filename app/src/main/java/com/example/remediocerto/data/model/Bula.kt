package com.example.remediocerto.data.model

/** Informações de bula de um medicamento, vindas do serviço bulario-api. */
data class Bula(
    val id: String = "",
    val nome: String = "",
    val principioAtivo: String = "",
    val classe: String = "",
    val indicacao: String = "",
    val observacao: String? = null,
    /** Endereço da bula oficial no Bulário Eletrônico da Anvisa. */
    val urlBulaOficial: String = "",
    val aviso: String = "",
    /** Data de revisão do conteúdo, no formato aaaa-mm-dd. */
    val atualizadoEm: String = "",
)

/** Por que a consulta de bula não deu certo. */
enum class MotivoFalhaBula { SEM_INTERNET, NAO_ENCONTRADO, SERVIDOR, NAO_CONFIGURADO }

sealed interface ResultadoBula {
    /**
     * [doCache] é true quando a internet falhou e mostramos a última consulta salva no celular
     * ([salvoEm] diz quando ela foi feita, em milissegundos).
     */
    data class Sucesso(val bula: Bula, val doCache: Boolean = false, val salvoEm: Long = 0L) : ResultadoBula

    data class Falha(val motivo: MotivoFalhaBula) : ResultadoBula
}
