package com.example.remediocerto.ui.medicamentos

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SugestoesTest {

    @Test
    fun `nome completo e igual ignorando maiuscula acento e espaco nas pontas`() {
        assertTrue(mesmoNome("Losartana", "losartana"))
        assertTrue(mesmoNome("Cápsula", " capsula "))
        assertTrue(mesmoNome("Ácido acetilsalicílico (AAS)", "acido acetilsalicilico (aas)"))
    }

    @Test
    fun `nome incompleto nao e igual`() {
        assertFalse(mesmoNome("Losartana", "losar"))
        assertFalse(mesmoNome("Losartana", ""))
    }
}
