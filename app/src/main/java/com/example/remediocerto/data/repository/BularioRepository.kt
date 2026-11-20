package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.ResultadoBula

interface BularioRepository {
    /**
     * Procura o medicamento pelo nome que a pessoa cadastrou (ex.: "Losartana 50 mg") e devolve a bula.
     * Sem internet, devolve a última consulta salva, se houver.
     */
    suspend fun consultar(nomeMedicamento: String): ResultadoBula

    /**
     * Sugestões de nomes para quem está digitando ("los" -> "Losartana potássica"). Quem começa com o
     * trecho vem primeiro. Devolve lista vazia se o trecho for curto, não houver internet ou der erro:
     * o campo continua aceitando qualquer texto digitado.
     */
    suspend fun sugerir(trecho: String): List<String>
}
