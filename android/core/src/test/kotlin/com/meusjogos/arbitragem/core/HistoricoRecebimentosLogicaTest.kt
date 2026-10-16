package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.filtrarHistoricoRecebimentos
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.recebidos
import com.meusjogos.arbitragem.core.model.FiltroHistoricoRecebimentos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.OrdenacaoRecebimento
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HistoricoRecebimentosLogicaTest {

    @Test
    fun `recebidos retorna apenas os jogos com status RECEBIDO`() {
        val recebido = marcarComoRecebido(jogo(valor = 10_000), LocalDate.of(2026, 9, 1))
        val pendente = jogo(valor = 5_000)
        assertEquals(listOf(recebido), listOf(recebido, pendente).recebidos())
    }

    @Test
    fun `filtrarHistoricoRecebimentos usa a data do PAGAMENTO, nao a data do jogo - secao 5 da V1_4`() {
        // Jogo apitado em julho, mas só pago em setembro — um filtro por "este mês" (setembro)
        // deve incluí-lo (porque foi PAGO em setembro), mesmo tendo sido jogado em julho.
        val jogoApitadoEmJulhoPagoEmSetembro = marcarComoRecebido(
            jogo(data = LocalDate.of(2026, 7, 10), valor = 10_000),
            LocalDate.of(2026, 9, 5),
        )
        val filtro = FiltroHistoricoRecebimentos(
            periodo = FiltroPeriodo.Personalizado(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
        )

        val resultado = listOf(jogoApitadoEmJulhoPagoEmSetembro).filtrarHistoricoRecebimentos(filtro)

        assertEquals(1, resultado.size)
    }

    @Test
    fun `filtrarHistoricoRecebimentos nunca inclui jogos ainda pendentes`() {
        val pendente = jogo(valor = 10_000)
        val resultado = listOf(pendente).filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos())
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `filtrarHistoricoRecebimentos filtra por competicao cidade e modalidade`() {
        val a = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", cidade = "Sorriso", modalidade = "Futebol"), LocalDate.of(2026, 9, 1))
        val b = marcarComoRecebido(jogo(valor = 2_000, competicao = "Copa B", cidade = "Sinop", modalidade = "Futsal"), LocalDate.of(2026, 9, 1))

        val porCompeticao = listOf(a, b).filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(competicao = "Copa A"))
        assertEquals(listOf(a), porCompeticao)

        val porCidade = listOf(a, b).filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(cidade = "Sinop"))
        assertEquals(listOf(b), porCidade)

        val porModalidade = listOf(a, b).filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(modalidade = "Futsal"))
        assertEquals(listOf(b), porModalidade)
    }

    @Test
    fun `filtrarHistoricoRecebimentos ordena por maior e menor valor`() {
        val menor = marcarComoRecebido(jogo(valor = 1_000), LocalDate.of(2026, 9, 1))
        val maior = marcarComoRecebido(jogo(valor = 9_000), LocalDate.of(2026, 9, 1))
        val jogos = listOf(menor, maior)

        val porMaiorValor = jogos.filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.MAIOR_VALOR))
        assertEquals(listOf(maior, menor), porMaiorValor)

        val porMenorValor = jogos.filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.MENOR_VALOR))
        assertEquals(listOf(menor, maior), porMenorValor)
    }

    @Test
    fun `filtrarHistoricoRecebimentos ordena por data de pagamento mais recente e mais antiga`() {
        val antigo = marcarComoRecebido(jogo(valor = 1_000), LocalDate.of(2026, 8, 1))
        val recente = marcarComoRecebido(jogo(valor = 1_000), LocalDate.of(2026, 9, 1))
        val jogos = listOf(antigo, recente)

        val maisRecentePrimeiro = jogos.filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.MAIS_RECENTE))
        assertEquals(listOf(recente, antigo), maisRecentePrimeiro)

        val maisAntigoPrimeiro = jogos.filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.MAIS_ANTIGO))
        assertEquals(listOf(antigo, recente), maisAntigoPrimeiro)
    }

    @Test
    fun `filtrarHistoricoRecebimentos ordena competicao em ordem alfabetica`() {
        val b = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa B"), LocalDate.of(2026, 9, 1))
        val a = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A"), LocalDate.of(2026, 9, 1))

        val ordenado = listOf(b, a).filtrarHistoricoRecebimentos(FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.COMPETICAO_AZ))

        assertEquals(listOf(a, b), ordenado)
    }

    private fun jogo(
        data: LocalDate = LocalDate.of(2026, 9, 1),
        valor: Long,
        competicao: String? = "Competição",
        cidade: String? = null,
        modalidade: String? = null,
    ): Jogo = Jogo(data = data, valorCentavos = valor, competicao = competicao, cidade = cidade, modalidade = modalidade)
}
