package com.example.remediocerto.ui.historico

import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricoItensTest {

    // montarHistorico usa o fuso do aparelho (como a tela Hoje); os testes seguem o mesmo fuso.
    private val zona = ZoneId.systemDefault()

    private fun ms(ano: Int, mes: Int, dia: Int, hora: Int = 8, minuto: Int = 0) =
        LocalDateTime.of(ano, mes, dia, hora, minuto).atZone(zona).toInstant().toEpochMilli()

    private fun remedio(inicio: Long = ms(2026, 11, 10, 0), horarios: List<String> = listOf("08:00")) =
        Medicamento(id = "m1", nome = "Losartana", horarios = horarios, inicioEm = inicio)

    private fun registro(previsto: Long, status: StatusDose, registradoEm: Long = previsto) =
        RegistroDose(id = "r$previsto", medicamentoId = "m1", medicamentoNome = "Losartana",
            previstoEm = previsto, registradoEm = registradoEm, status = status)

    private fun dose(previsto: Long, situacao: SituacaoDose) =
        DoseHistorico("m1", "Losartana", previsto, null, situacao)

    private val agora = ms(2026, 11, 20, 12)

    private fun situacoes(doses: List<DoseHistorico>) =
        doses.sortedBy { it.previstoEm }.map { it.situacao }

    @Test
    fun `dose de dia anterior sem registro vira nao tomada`() {
        val doses = montarHistorico(listOf(remedio(inicio = ms(2026, 11, 19, 0))), emptyList(), agora)
        assertEquals(listOf(SituacaoDose.NAO_TOMADA, SituacaoDose.ATRASADA), situacoes(doses))
    }

    @Test
    fun `dose de hoje sem registro depois da tolerancia fica atrasada`() {
        val doses = montarHistorico(listOf(remedio(inicio = ms(2026, 11, 20, 0))), emptyList(), agora)
        assertEquals(listOf(SituacaoDose.ATRASADA), situacoes(doses))
    }

    @Test
    fun `dose futura ou na hora de tomar nao aparece`() {
        val remedios = listOf(remedio(inicio = ms(2026, 11, 20, 0), horarios = listOf("12:10", "11:50", "18:00")))
        // 11:50 ainda está na tolerância de 30 min; 12:10 e 18:00 são futuras.
        assertTrue(montarHistorico(remedios, emptyList(), agora).isEmpty())
    }

    @Test
    fun `dose tomada no horario e dose tomada com atraso`() {
        val dia19 = ms(2026, 11, 19)
        val dia20 = ms(2026, 11, 20)
        val doses = montarHistorico(
            listOf(remedio(inicio = ms(2026, 11, 19, 0))),
            listOf(
                registro(dia19, StatusDose.TOMADA, registradoEm = dia19 + 5 * 60_000),
                registro(dia20, StatusDose.TOMADA, registradoEm = dia20 + 90 * 60_000),
            ),
            agora,
        )
        assertEquals(listOf(SituacaoDose.TOMADA, SituacaoDose.TOMADA_COM_ATRASO), situacoes(doses))
    }

    @Test
    fun `adiada de ontem sem tomar conta como nao tomada`() {
        val ontem = ms(2026, 11, 19)
        val doses = montarHistorico(listOf(remedio(inicio = ms(2026, 11, 19, 0))), listOf(registro(ontem, StatusDose.ADIADA)), agora)
        assertEquals(SituacaoDose.NAO_TOMADA, doses.first { it.previstoEm == ontem }.situacao)
    }

    @Test
    fun `adiada hoje dentro da tolerancia continua adiada`() {
        val hoje8 = ms(2026, 11, 20, 8)
        val doses = montarHistorico(
            listOf(remedio(inicio = ms(2026, 11, 20, 0))),
            listOf(registro(hoje8, StatusDose.ADIADA)),
            agora = ms(2026, 11, 20, 8, 20),
        )
        assertEquals(listOf(SituacaoDose.ADIADA), situacoes(doses))
    }

    @Test
    fun `perdida registrada conta como nao tomada`() {
        val previsto = ms(2026, 11, 19)
        val doses = montarHistorico(listOf(remedio(inicio = ms(2026, 11, 19, 0))), listOf(registro(previsto, StatusDose.PERDIDA)), agora)
        assertEquals(SituacaoDose.NAO_TOMADA, doses.first { it.previstoEm == previsto }.situacao)
    }

    @Test
    fun `nao inventa doses antes do inicio do tratamento nem sem data de inicio`() {
        val comInicio = montarHistorico(listOf(remedio(inicio = ms(2026, 11, 20, 0))), emptyList(), agora)
        assertEquals(1, comInicio.size)
        assertTrue(montarHistorico(listOf(remedio(inicio = 0L)), emptyList(), agora).isEmpty())
    }

    @Test
    fun `respeita a duracao do tratamento`() {
        val curto = remedio(inicio = ms(2026, 11, 10, 0)).copy(duracaoDias = 2) // dias 10 e 11 apenas
        val doses = montarHistorico(listOf(curto), emptyList(), agora)
        assertEquals(listOf(ms(2026, 11, 10), ms(2026, 11, 11)), doses.map { it.previstoEm }.sorted())
    }

    @Test
    fun `registro de remedio apagado continua aparecendo`() {
        val previsto = ms(2026, 11, 18)
        val doses = montarHistorico(emptyList(), listOf(registro(previsto, StatusDose.TOMADA)), agora)
        assertEquals(listOf(SituacaoDose.TOMADA), situacoes(doses))
        assertEquals("Losartana", doses.single().nome)
    }

    @Test
    fun `agrupa por dia, do mais recente para o mais antigo`() {
        val itens = agruparPorDia(
            listOf(
                dose(ms(2026, 11, 19, 20), SituacaoDose.TOMADA),
                dose(ms(2026, 11, 20, 8), SituacaoDose.ATRASADA),
                dose(ms(2026, 11, 19, 8), SituacaoDose.NAO_TOMADA),
            ),
            zona,
        )
        assertEquals(
            listOf("dia 2026-11-20", "ATRASADA", "dia 2026-11-19", "TOMADA", "NAO_TOMADA"),
            itens.map { if (it is ItemHistorico.Dia) "dia ${it.dia}" else (it as ItemHistorico.Dose).dose.situacao.name },
        )
    }

    @Test
    fun `lista vazia nao gera titulos`() {
        assertEquals(emptyList<ItemHistorico>(), agruparPorDia(emptyList(), zona))
    }

    @Test
    fun `resumo conta so os ultimos 7 dias e separa nao tomadas e atrasadas`() {
        val resumo = calcularResumoSemana(
            listOf(
                dose(ms(2026, 11, 20, 8), SituacaoDose.TOMADA),             // dentro
                dose(ms(2026, 11, 20, 9), SituacaoDose.ATRASADA),           // dentro, atrasada
                dose(ms(2026, 11, 19, 20), SituacaoDose.NAO_TOMADA),        // dentro, não tomada
                dose(ms(2026, 11, 14, 8), SituacaoDose.TOMADA_COM_ATRASO),  // 6 dias atrás: ainda dentro, conta como tomada
                dose(ms(2026, 11, 13, 8), SituacaoDose.TOMADA),             // 7 dias atrás: fora
            ),
            agora,
            zona,
        )
        assertEquals(ResumoSemana(tomadas = 2, total = 4, naoTomadas = 1, atrasadas = 1), resumo)
    }

    @Test
    fun `resumo e nulo sem doses na semana`() {
        assertNull(calcularResumoSemana(listOf(dose(ms(2026, 10, 1), SituacaoDose.TOMADA)), agora, zona))
        assertNull(calcularResumoSemana(emptyList(), agora, zona))
    }

    @Test
    fun `dias diferentes nao se misturam perto da meia-noite`() {
        val itens = agruparPorDia(
            listOf(dose(ms(2026, 11, 20, 0), SituacaoDose.TOMADA), dose(ms(2026, 11, 19, 23), SituacaoDose.TOMADA)),
            zona,
        )
        assertEquals(
            listOf(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 19)),
            itens.filterIsInstance<ItemHistorico.Dia>().map { it.dia },
        )
    }
}
