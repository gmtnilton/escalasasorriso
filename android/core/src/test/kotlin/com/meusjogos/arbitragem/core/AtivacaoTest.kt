package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.codigoAtivacaoValido
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AtivacaoTest {

    @Test
    fun `codigo correto e aceito em qualquer variacao de maiuscula`() {
        assertTrue(codigoAtivacaoValido("Apito Ativo"))
        assertTrue(codigoAtivacaoValido("APITO ATIVO"))
        assertTrue(codigoAtivacaoValido("apito ativo"))
        assertTrue(codigoAtivacaoValido("ApItO aTiVo"))
    }

    @Test
    fun `codigo correto e aceito mesmo com espacos extras nas pontas`() {
        assertTrue(codigoAtivacaoValido("  Apito Ativo  "))
        assertTrue(codigoAtivacaoValido("\tapito ativo\n"))
    }

    @Test
    fun `codigo incorreto e rejeitado`() {
        assertFalse(codigoAtivacaoValido("apito"))
        assertFalse(codigoAtivacaoValido(""))
        assertFalse(codigoAtivacaoValido("Apito Ativo!"))
        assertFalse(codigoAtivacaoValido("Apito  Ativo"))
        assertFalse(codigoAtivacaoValido("senha123"))
    }
}
