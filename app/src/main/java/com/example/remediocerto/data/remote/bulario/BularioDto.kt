package com.example.remediocerto.data.remote.bulario

import com.example.remediocerto.data.model.Bula

/*
 * Formato das respostas do servidor bulario-api. Todos os campos têm valor padrão: assim, se o servidor
 * deixar um campo de fora, o app não quebra (o Gson usa o construtor sem argumentos que o Kotlin gera).
 */

data class BulaResumoDto(
    val id: String = "",
    val nome: String = "",
    val principioAtivo: String = "",
    val classe: String = "",
)

data class PesquisaBulaDto(
    val total: Int = 0,
    val pagina: Int = 1,
    val tamanhoPagina: Int = 10,
    val itens: List<BulaResumoDto> = emptyList(),
)

data class BulaDto(
    val id: String = "",
    val nome: String = "",
    val principioAtivo: String = "",
    val classe: String = "",
    val indicacao: String = "",
    val observacao: String? = null,
    val urlBulaOficial: String = "",
    val aviso: String = "",
    val atualizadoEm: String = "",
) {
    fun paraModelo() = Bula(
        id = id,
        nome = nome,
        principioAtivo = principioAtivo,
        classe = classe,
        indicacao = indicacao,
        observacao = observacao?.takeIf { it.isNotBlank() },
        urlBulaOficial = urlBulaOficial,
        aviso = aviso,
        atualizadoEm = atualizadoEm,
    )
}
