package com.example.remediocerto.data.remote.bulario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TermosDeBuscaTest {

    @Test
    fun `tira dose e unidade do nome`() {
        assertEquals(listOf("losartana"), termosDeBusca("Losartana 50 mg"))
        assertEquals(listOf("losartana"), termosDeBusca("LOSARTANA 50MG comprimidos"))
    }

    @Test
    fun `sem acento e com termo mais geral como segunda tentativa`() {
        assertEquals(listOf("dipirona sodica", "dipirona"), termosDeBusca("Dipirona sódica 500 mg"))
        assertEquals(listOf("acido acetilsalicilico aas", "acido"), termosDeBusca("Ácido acetilsalicílico (AAS)"))
    }

    @Test
    fun `nome vazio ou so numeros nao gera busca`() {
        assertEquals(emptyList<String>(), termosDeBusca(""))
        assertEquals(emptyList<String>(), termosDeBusca("500 mg"))
        assertEquals(emptyList<String>(), termosDeBusca("a"))
    }

    @Test
    fun `prefere o resultado de nome igual ao termo`() {
        val itens = listOf(
            BulaResumoDto(id = "losartana-hct", nome = "Losartana + Hidroclorotiazida"),
            BulaResumoDto(id = "losartana", nome = "Losartana"),
        )
        assertEquals("losartana", escolherMelhor(itens, "losartana")?.id)
    }

    @Test
    fun `sem igual, fica com o primeiro; lista vazia devolve nulo`() {
        val itens = listOf(BulaResumoDto(id = "omeprazol", nome = "Omeprazol"))
        assertEquals("omeprazol", escolherMelhor(itens, "omepra")?.id)
        assertNull(escolherMelhor(emptyList(), "x"))
    }

    @Test
    fun `sugestoes que comecam com o termo vem primeiro`() {
        val nomes = listOf("Clonazepam", "Losartana potássica", "Dipirona sódica", "Lorazepam")
        assertEquals(
            listOf("Lorazepam", "Losartana potássica", "Clonazepam"),
            ordenarSugestoes(nomes, "lo").filter { it != "Dipirona sódica" },
        )
        assertEquals("Lorazepam", ordenarSugestoes(nomes, "lo").first())
    }

    @Test
    fun `sugestoes ignoram acento ao ordenar`() {
        assertEquals("Ácido acetilsalicílico", ordenarSugestoes(listOf("Omeprazol", "Ácido acetilsalicílico"), "acido").first())
    }
}
