package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.util.DateUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun `formata data no padrao brasileiro`() {
        assertEquals("03/09/2026", DateUtils.formatarData(LocalDate.of(2026, 9, 3)))
    }

    @Test
    fun `nome do mes e ano em maiusculo - exemplo secao 4 e 17`() {
        assertEquals("SETEMBRO/2026", DateUtils.nomeMesAno(2026, 9))
    }

    @Test
    fun `parse aceita dd-MM-aaaa e rejeita texto invalido`() {
        assertEquals(LocalDate.of(2026, 9, 3), DateUtils.parseData("03/09/2026"))
        assertNull(DateUtils.parseData("32/13/2026"))
        assertNull(DateUtils.parseData("data invalida"))
        assertNull(DateUtils.parseData(""))
    }

    @Test
    fun `mascara de data formata digitos progressivamente`() {
        assertEquals("03", DateUtils.aplicarMascaraData("03"))
        assertEquals("03/09", DateUtils.aplicarMascaraData("0309"))
        assertEquals("03/09/2026", DateUtils.aplicarMascaraData("03092026"))
        assertEquals("03/09/2026", DateUtils.aplicarMascaraData("030920269999"))
    }
}
