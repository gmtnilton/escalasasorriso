package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.duplicarJogo
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.desfazerRecebimento
import com.meusjogos.arbitragem.core.logic.totalAReceberCentavos
import com.meusjogos.arbitragem.core.logic.totalGeralCentavos
import com.meusjogos.arbitragem.core.logic.totalRecebidoCentavos
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import java.time.LocalDate

/**
 * Reproduz, passo a passo, o roteiro de testes de integridade descrito na
 * seção 27 do briefing do app ("REGRAS DE INTEGRIDADE" — TESTE 1 a TESTE 7;
 * o TESTE 8, persistência entre reinícios do app, é garantido pelo uso do
 * Room/SQLite no módulo :app e não é reproduzível aqui, num módulo Kotlin
 * puro).
 *
 * Simula um "repositório" em memória (uma simples MutableList) para validar
 * que a regra TOTAL GERAL = TOTAL RECEBIDO + TOTAL A RECEBER nunca é
 * violada, mesmo depois de cadastrar, receber, excluir, duplicar e alterar
 * valores — exatamente a sequência de operações que o app real executa
 * sobre o Room.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class IntegridadeFinanceiraTest {

    /** Simula a tabela "jogos" do banco de dados. */
    private val banco = mutableListOf<Jogo>()

    private fun inserir(jogo: Jogo): Jogo {
        val comId = jogo.copy(id = (banco.maxOfOrNull { it.id } ?: 0L) + 1)
        banco.add(comId)
        return comId
    }

    private fun atualizar(jogo: Jogo) {
        val indice = banco.indexOfFirst { it.id == jogo.id }
        banco[indice] = jogo
    }

    private fun excluir(jogo: Jogo) {
        banco.removeIf { it.id == jogo.id }
    }

    private fun aReceber() = banco.totalAReceberCentavos()
    private fun recebido() = banco.totalRecebidoCentavos()
    private fun geral() = banco.totalGeralCentavos()

    private fun assertInvarianteTotalGeral() {
        assertEquals(recebido() + aReceber(), geral(), "TOTAL GERAL deve ser sempre RECEBIDO + A RECEBER")
    }

    @Test
    @Order(1)
    @DisplayName("TESTE 1 — cadastrar jogo de R$ 500 como A RECEBER")
    fun teste1_cadastrarJogoAReceber() {
        inserir(Jogo(data = LocalDate.of(2026, 9, 3), valorCentavos = 50_000))

        assertEquals(50_000L, aReceber())
        assertEquals(0L, recebido())
        assertEquals(50_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(2)
    @DisplayName("TESTE 2 — marcar como recebido")
    fun teste2_marcarComoRecebido() {
        teste1_cadastrarJogoAReceber()
        val jogo = banco.single()

        atualizar(marcarComoRecebido(jogo, dataRecebimento = LocalDate.of(2026, 9, 3)))

        assertEquals(0L, aReceber())
        assertEquals(50_000L, recebido())
        assertEquals(50_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(3)
    @DisplayName("TESTE 3 — cadastrar outro jogo de R$ 300 a receber")
    fun teste3_cadastrarSegundoJogo() {
        teste2_marcarComoRecebido()
        inserir(Jogo(data = LocalDate.of(2026, 9, 10), valorCentavos = 30_000))

        assertEquals(30_000L, aReceber())
        assertEquals(50_000L, recebido())
        assertEquals(80_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(4)
    @DisplayName("TESTE 4 — excluir o jogo de R$ 300")
    fun teste4_excluirJogoDe300() {
        teste3_cadastrarSegundoJogo()
        val jogoDe300 = banco.single { it.valorCentavos == 30_000L }

        excluir(jogoDe300)

        assertEquals(0L, aReceber())
        assertEquals(50_000L, recebido())
        assertEquals(50_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(5)
    @DisplayName("TESTE 5 — duplicar jogo recebido: a cópia deve nascer A RECEBER")
    fun teste5_duplicarJogoRecebidoNasceAReceber() {
        teste4_excluirJogoDe300()
        val original = banco.single()
        check(original.statusPagamento == StatusPagamento.RECEBIDO)

        val copia = inserir(duplicarJogo(original))

        assertEquals(StatusPagamento.A_RECEBER, copia.statusPagamento)
        assertEquals(null, copia.dataRecebimento)
        // original permanece intocado
        assertEquals(StatusPagamento.RECEBIDO, banco.first { it.id == original.id }.statusPagamento)
        assertEquals(50_000L, aReceber())
        assertEquals(50_000L, recebido())
        assertEquals(100_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(6)
    @DisplayName("TESTE 6 — alterar o valor de um jogo recalcula todos os totais")
    fun teste6_alterarValorRecalculaTotais() {
        teste5_duplicarJogoRecebidoNasceAReceber()
        val copiaAReceber = banco.single { it.statusPagamento == StatusPagamento.A_RECEBER }

        atualizar(copiaAReceber.copy(valorCentavos = 55_000))

        assertEquals(55_000L, aReceber())
        assertEquals(50_000L, recebido())
        assertEquals(105_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @Order(7)
    @DisplayName("TESTE 7 — mudar RECEBIDO para A RECEBER move o valor entre os totais")
    fun teste7_alterarRecebidoParaAReceber() {
        teste6_alterarValorRecalculaTotais()
        val jogoRecebido = banco.single { it.statusPagamento == StatusPagamento.RECEBIDO }

        atualizar(desfazerRecebimento(jogoRecebido))

        assertEquals(105_000L, aReceber())
        assertEquals(0L, recebido())
        assertEquals(105_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @DisplayName("Exemplo da seção 11: R$500 a receber + R$2000 recebido -> recebe o de R$500")
    fun exemploSecao11_marcarComoRecebidoAtualizaDashboard() {
        banco.clear()
        inserir(Jogo(data = LocalDate.now(), valorCentavos = 50_000))
        inserir(marcarComoRecebido(Jogo(data = LocalDate.now(), valorCentavos = 200_000)))

        assertEquals(50_000L, aReceber())
        assertEquals(200_000L, recebido())
        assertEquals(250_000L, geral())

        val pendente = banco.single { it.statusPagamento == StatusPagamento.A_RECEBER }
        atualizar(marcarComoRecebido(pendente))

        assertEquals(0L, aReceber())
        assertEquals(250_000L, recebido())
        assertEquals(250_000L, geral())
        assertInvarianteTotalGeral()
    }

    @Test
    @DisplayName("Nunca duplica nem perde valor ao alternar status repetidamente")
    fun alternarStatusRepetidamenteNuncaAlteraOTotalGeral() {
        banco.clear()
        var jogo = inserir(Jogo(data = LocalDate.now(), valorCentavos = 123_45))

        repeat(10) {
            jogo = marcarComoRecebido(jogo)
            atualizar(jogo)
            assertEquals(123_45L, geral())
            jogo = desfazerRecebimento(jogo)
            atualizar(jogo)
            assertEquals(123_45L, geral())
        }
        assertInvarianteTotalGeral()
    }
}
