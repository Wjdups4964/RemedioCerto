package com.example.remediocerto.data.model

import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConviteTest {

    @Test
    fun normalizarCodigo_ficaSoComDigitos() {
        assertEquals("12345678", normalizarCodigo("1234 5678"))
        assertEquals("12345678", normalizarCodigo(" 1234-5678 "))
        assertEquals("", normalizarCodigo("abc"))
    }

    @Test
    fun formatarCodigo_separaEmDoisBlocos() {
        assertEquals("1234 5678", formatarCodigo("12345678"))
        assertEquals("123", formatarCodigo("123")) // incompleto: não mexe
    }

    @Test
    fun codigoCompleto_exigeOitoDigitos() {
        assertTrue(codigoCompleto("00123456"))
        assertFalse(codigoCompleto("1234567"))
        assertFalse(codigoCompleto("123456789"))
        assertFalse(codigoCompleto("1234567a"))
    }

    @Test
    fun vencidoEm_comparaComAValidade() {
        val convite = Convite(expiraEm = Date(1_000))
        assertFalse(convite.vencidoEm(999))
        assertTrue(convite.vencidoEm(1_000)) // no instante exato já venceu
        assertTrue(convite.vencidoEm(5_000))
    }
}
