package com.example.remediocerto.alarm

import com.example.remediocerto.data.model.Medicamento
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LembreteTest {

    private fun instante(ano: Int, mes: Int, dia: Int, hora: Int = 0, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(ano, mes - 1, dia, hora, minuto, 0)
        }.timeInMillis

    private fun losartana(
        horarios: List<String> = listOf("08:00"),
        inicioEm: Long = 0,
        intervaloDias: Int = 1,
        duracaoDias: Int = 0,
    ) = Medicamento(
        id = "m1", nome = "Losartana", dose = "50 mg", horarios = horarios,
        inicioEm = inicioEm, intervaloDias = intervaloDias, duracaoDias = duracaoDias,
    )

    private fun horas(lembretes: List<Lembrete>): List<String> = lembretes.map {
        val c = Calendar.getInstance().apply { timeInMillis = it.disparaEm }
        "%02d/%02d %02d:%02d".format(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    @Test
    fun `uma dose gera avisos 10 min antes, 5 min antes e na hora`() {
        val agora = instante(2026, 11, 20, 7, 0)
        val lembretes = proximosLembretes(listOf(losartana()), agora, diasAFrente = 1)
        assertEquals(listOf("20/11 07:50", "20/11 07:55", "20/11 08:00"), horas(lembretes))
        assertEquals(listOf(10, 5, 0), lembretes.map { it.minutosAntes })
    }

    @Test
    fun `avisos que ja passaram nao sao agendados`() {
        // 07:58: o de 10 e o de 5 minutos já passaram; sobra só o da hora.
        val agora = instante(2026, 11, 20, 7, 58)
        assertEquals(listOf("20/11 08:00"), horas(proximosLembretes(listOf(losartana()), agora, diasAFrente = 1)))
    }

    @Test
    fun `depois da hora da dose, os avisos de hoje acabam e entram os de amanha`() {
        val agora = instante(2026, 11, 20, 8, 1)
        val lembretes = proximosLembretes(listOf(losartana()), agora, diasAFrente = 2)
        assertEquals(listOf("21/11 07:50", "21/11 07:55", "21/11 08:00"), horas(lembretes))
    }

    @Test
    fun `varios horarios saem em ordem de tempo`() {
        val agora = instante(2026, 11, 20, 6, 0)
        val lembretes = proximosLembretes(listOf(losartana(horarios = listOf("20:00", "08:00"))), agora, diasAFrente = 1)
        assertEquals(
            listOf("20/11 07:50", "20/11 07:55", "20/11 08:00", "20/11 19:50", "20/11 19:55", "20/11 20:00"),
            horas(lembretes),
        )
    }

    @Test
    fun `respeita remedio de dois em dois dias`() {
        val inicio = instante(2026, 11, 20)
        val agora = instante(2026, 11, 20, 9, 0) // hoje já passou; amanhã não é dia de tomar
        val lembretes = proximosLembretes(listOf(losartana(inicioEm = inicio, intervaloDias = 2)), agora, diasAFrente = 2)
        assertTrue(lembretes.isEmpty())
    }

    @Test
    fun `respeita o fim do tratamento`() {
        val inicio = instante(2026, 11, 19)
        val agora = instante(2026, 11, 20, 9, 0) // tratamento de 2 dias: 19 e 20. O dia 21 já não vale.
        assertTrue(proximosLembretes(listOf(losartana(inicioEm = inicio, duracaoDias = 2)), agora, diasAFrente = 2).isEmpty())
    }

    @Test
    fun `tratamento que ainda nao comecou nao avisa`() {
        val agora = instante(2026, 11, 20, 6, 0)
        val comecaDepois = instante(2026, 12, 1)
        assertTrue(proximosLembretes(listOf(losartana(inicioEm = comecaDepois)), agora, diasAFrente = 2).isEmpty())
    }

    @Test
    fun `codigos dos avisos sao todos diferentes`() {
        val agora = instante(2026, 11, 20, 6, 0)
        val lembretes = proximosLembretes(listOf(losartana(horarios = listOf("08:00", "20:00"))), agora)
        assertEquals(lembretes.size, lembretes.map { it.codigo }.toSet().size)
    }

    @Test
    fun `os tres avisos da mesma dose usam a mesma notificacao`() {
        val agora = instante(2026, 11, 20, 6, 0)
        val doMesmoHorario = proximosLembretes(listOf(losartana()), agora, diasAFrente = 1)
        assertEquals(1, doMesmoHorario.map { it.idNotificacao }.toSet().size)
    }

    @Test
    fun `alarme adiado toca no instante do adiamento, nao no horario da dose`() {
        val previsto = instante(2026, 11, 20, 8, 0)
        val original = Lembrete("m1", "Losartana", "50 mg", "08:00", previsto, minutosAntes = 0)
        val adiado = original.copy(adiadoPara = previsto + 10 * 60_000L)
        assertEquals(previsto, original.disparaEm)
        assertEquals(previsto + 10 * 60_000L, adiado.disparaEm)
    }

    @Test
    fun `alarme adiado tem codigo proprio mas a mesma notificacao da dose`() {
        val previsto = instante(2026, 11, 20, 8, 0)
        val original = Lembrete("m1", "Losartana", "50 mg", "08:00", previsto, minutosAntes = 0)
        val adiado = original.copy(adiadoPara = previsto + 10 * 60_000L)
        assertTrue(original.codigo != adiado.codigo)
        assertEquals(original.idNotificacao, adiado.idNotificacao)
        assertEquals(original.idNotificacao, idNotificacaoDaDose("m1", previsto))
    }
}
