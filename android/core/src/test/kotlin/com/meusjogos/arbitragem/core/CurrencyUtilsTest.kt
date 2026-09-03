package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.util.CurrencyUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CurrencyUtilsTest {

    @Test
    fun `formata centavos como reais no padrao brasileiro`() {
        assertEquals("R$ 0,00", CurrencyUtils.formatar(0))
        assertEquals("R$ 4,00", CurrencyUtils.formatar(400))
        assertEquals("R$ 450,00", CurrencyUtils.formatar(45_000))
        assertEquals("R$ 1.250,00", CurrencyUtils.formatar(125_000))
        assertEquals("R$ 1.500,00", CurrencyUtils.formatar(150_000))
        assertEquals("R$ 22.500,00", CurrencyUtils.formatar(2_250_000))
        assertEquals("R$ 1.234.567,89", CurrencyUtils.formatar(123_456_789))
    }

    @Test
    fun `mascara de digitacao interpreta digitos como centavos entrando pela direita`() {
        assertEquals(0L, CurrencyUtils.paraCentavosMascarado(""))
        assertEquals(400L, CurrencyUtils.paraCentavosMascarado("400"))
        assertEquals(40_000L, CurrencyUtils.paraCentavosMascarado("40000"))
        assertEquals(50L, CurrencyUtils.paraCentavosMascarado("050"))
    }

    @Test
    fun `parse de texto formatado aceita virgula e milhar com ponto`() {
        assertEquals(150_000L, CurrencyUtils.paraCentavosDeTextoFormatado("R$ 1.500,00"))
        assertEquals(150_000L, CurrencyUtils.paraCentavosDeTextoFormatado("1500,00"))
        assertEquals(150_000L, CurrencyUtils.paraCentavosDeTextoFormatado("1500.00"))
        assertEquals(30_000L, CurrencyUtils.paraCentavosDeTextoFormatado("300,00"))
        assertNull(CurrencyUtils.paraCentavosDeTextoFormatado(""))
        assertNull(CurrencyUtils.paraCentavosDeTextoFormatado("abc"))
    }

    @Test
    fun `roundtrip formatar e reinterpretar mantem o mesmo valor`() {
        val valores = listOf(0L, 1L, 99L, 100L, 45_000L, 302_35L, 25_700_00L)
        for (v in valores) {
            val textoFormatado = CurrencyUtils.formatar(v)
            assertEquals(v, CurrencyUtils.paraCentavosDeTextoFormatado(textoFormatado))
        }
    }
}
