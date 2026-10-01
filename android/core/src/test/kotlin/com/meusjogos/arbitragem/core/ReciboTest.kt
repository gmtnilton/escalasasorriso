package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.model.Recibo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ReciboTest {

    private fun recibo(id: Long) = Recibo(
        id = id,
        jogoId = 1L,
        pagadorNome = "Liga Municipal",
        pagadorDocumento = "00.000.000/0001-00",
        recebedorNome = "NILTON RODRIGO RIBEIRO",
        recebedorDocumento = "000.000.000-00",
        valorCentavos = 15_000,
        dataPagamento = LocalDate.of(2026, 9, 25),
    )

    @Test
    fun `numero formatado usa o id com seis digitos`() {
        assertEquals("Nº 000001", recibo(1L).numeroFormatado)
        assertEquals("Nº 000123", recibo(123L).numeroFormatado)
        assertEquals("Nº 123456", recibo(123_456L).numeroFormatado)
    }
}
