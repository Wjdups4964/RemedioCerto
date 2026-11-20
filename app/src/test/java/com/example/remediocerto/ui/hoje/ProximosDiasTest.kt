package com.example.remediocerto.ui.hoje

import com.example.remediocerto.data.model.Medicamento
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximosDiasTest {

    private fun instante(ano: Int, mes: Int, dia: Int, hora: Int = 0, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(ano, mes - 1, dia, hora, minuto, 0)
        }.timeInMillis

    private val agora = instante(2026, 11, 20, 14, 52) // sexta-feira

    private fun remedio(
        id: String,
        nome: String,
        horarios: List<String> = listOf("08:00"),
        inicio: Long = instante(2026, 11, 20),
        duracaoDias: Int = 0,
        intervaloDias: Int = 1,
    ) = Medicamento(
        id = id, nome = nome, dose = "1 comprimido", horarios = horarios,
        inicioEm = inicio, duracaoDias = duracaoDias, intervaloDias = intervaloDias,
    )

    private fun dia(d: Int) = instante(2026, 11, d)

    private fun dosesDoDia(itens: List<ItemProximos>, d: Int): List<ItemProximos.Dose> =
        itens.filterIsInstance<ItemProximos.Dose>().filter { it.previstoEm in dia(d) until dia(d + 1) }

    @Test
    fun `sem medicamentos a lista fica vazia`() {
        assertTrue(montarProximosDias(emptyList(), agora).isEmpty())
    }

    @Test
    fun `tratamentos que acabam em 23 e em 27 aparecem em ordem com os dias que faltam`() {
        val ate27 = remedio("b", "Vitamina", duracaoDias = 8)   // 20/11 + 7 = 27/11
        val ate23 = remedio("a", "Amoxicilina", duracaoDias = 4) // 20/11 + 3 = 23/11
        val terminos = montarProximosDias(listOf(ate27, ate23), agora).filterIsInstance<ItemProximos.Termino>()
        assertEquals(listOf("Amoxicilina", "Vitamina"), terminos.map { it.nome })
        assertEquals(listOf(dia(23), dia(27)), terminos.map { it.fim })
        assertEquals(listOf(3, 7), terminos.map { it.faltam })
    }

    @Test
    fun `uso continuo nao tem data para acabar`() {
        val itens = montarProximosDias(listOf(remedio("a", "Losartana")), agora)
        assertTrue(itens.filterIsInstance<ItemProximos.Termino>().isEmpty())
        assertFalse(itens.contains(ItemProximos.Secao(SecaoProximos.TERMINAM)))
    }

    @Test
    fun `tratamento que ja acabou nao aparece`() {
        val acabou = remedio("a", "Amoxicilina", inicio = dia(10), duracaoDias = 5) // terminou em 14/11
        val itens = montarProximosDias(listOf(acabou), agora)
        assertTrue(itens.filterIsInstance<ItemProximos.Termino>().isEmpty())
        assertTrue(itens.filterIsInstance<ItemProximos.Dose>().isEmpty())
    }

    @Test
    fun `tratamento que acaba hoje aparece com zero dias para acabar`() {
        val acabaHoje = remedio("a", "Amoxicilina", inicio = dia(18), duracaoDias = 3) // 18, 19, 20
        val termino = montarProximosDias(listOf(acabaHoje), agora).filterIsInstance<ItemProximos.Termino>().single()
        assertEquals(0, termino.faltam)
    }

    @Test
    fun `cada dia mostra so os remedios que ainda valem naquele dia`() {
        val ate23 = remedio("a", "Amoxicilina", duracaoDias = 4)
        val continuo = remedio("b", "Losartana")
        val itens = montarProximosDias(listOf(ate23, continuo), agora)
        assertEquals(listOf("Amoxicilina", "Losartana"), dosesDoDia(itens, 23).map { it.nome })
        assertEquals(listOf("Losartana"), dosesDoDia(itens, 24).map { it.nome })
    }

    @Test
    fun `a lista comeca amanha e vai ate 7 dias`() {
        val dias = montarProximosDias(listOf(remedio("a", "Losartana")), agora).filterIsInstance<ItemProximos.Dia>()
        assertEquals((21..27).map { dia(it) }, dias.map { it.dia })
    }

    @Test
    fun `o ultimo dia do tratamento e marcado`() {
        val itens = montarProximosDias(listOf(remedio("a", "Amoxicilina", duracaoDias = 4)), agora)
        assertFalse(dosesDoDia(itens, 22).single().ultimoDia)
        assertTrue(dosesDoDia(itens, 23).single().ultimoDia)
    }

    @Test
    fun `doses do mesmo dia saem em ordem de horario`() {
        val itens = montarProximosDias(
            listOf(remedio("a", "Losartana", horarios = listOf("20:00", "08:00")), remedio("b", "Vitamina", horarios = listOf("12:00"))),
            agora,
        )
        assertEquals(listOf("08:00", "12:00", "20:00"), dosesDoDia(itens, 21).map { it.hora })
    }

    @Test
    fun `dia sem remedio mostra o aviso`() {
        // De dois em dois dias, começando hoje: 20, 22, 24... Amanhã (21) não tem dose.
        val itens = montarProximosDias(listOf(remedio("a", "Losartana", intervaloDias = 2)), agora)
        assertTrue(itens.contains(ItemProximos.SemDoses(dia(21))))
        assertFalse(itens.contains(ItemProximos.SemDoses(dia(22))))
    }

    @Test
    fun `a lista vai ate o fim do ultimo tratamento mesmo passando de 7 dias`() {
        // Começa em 26/11 e termina em 09/12 (14 dias): 19 dias depois de hoje.
        val futuro = remedio("a", "Amoxicilina", inicio = dia(26), duracaoDias = 14)
        val itens = montarProximosDias(listOf(futuro), agora)
        val dias = itens.filterIsInstance<ItemProximos.Dia>()
        assertEquals(19, dias.size)
        assertEquals(instante(2026, 12, 9), dias.last().dia)
        // Só tem dose a partir do dia 26 e a última cai em 09/12, marcada como último dia.
        val doses = itens.filterIsInstance<ItemProximos.Dose>()
        assertEquals(14, doses.size)
        assertEquals(instante(2026, 11, 26, 8), doses.first().previstoEm)
        assertTrue(doses.last().ultimoDia)
    }

    @Test
    fun `tratamento que ainda nao comecou mostra quando comeca`() {
        val futuro = remedio("a", "Amoxicilina", inicio = dia(26), duracaoDias = 5)
        val termino = montarProximosDias(listOf(futuro), agora).filterIsInstance<ItemProximos.Termino>().single()
        assertEquals(dia(26), termino.comecaEm)
        assertEquals(dia(30), termino.fim)
    }

    @Test
    fun `tratamento que ja comecou nao tem data de comeco`() {
        val termino = montarProximosDias(listOf(remedio("a", "Amoxicilina", duracaoDias = 4)), agora)
            .filterIsInstance<ItemProximos.Termino>().single()
        assertEquals(null, termino.comecaEm)
    }

    @Test
    fun `tratamento muito longo respeita o limite de dias`() {
        val longo = remedio("a", "Vitamina", duracaoDias = 400)
        val dias = montarProximosDias(listOf(longo), agora).filterIsInstance<ItemProximos.Dia>()
        assertEquals(DIAS_PROXIMOS_MAXIMO, dias.size)
    }
}
