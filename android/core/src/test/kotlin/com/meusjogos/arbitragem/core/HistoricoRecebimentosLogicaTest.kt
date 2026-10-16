package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.agruparHistoricoPorCompeticao
import com.meusjogos.arbitragem.core.logic.filtrarHistoricoRecebimentos
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.recebidos
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.FiltroHistoricoRecebimentos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.OrdenacaoRecebimento
import com.meusjogos.arbitragem.core.model.StatusCompeticao
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

    @Test
    fun `agruparHistoricoPorCompeticao soma os pagamentos da mesma competicao em um unico item - correcao pos entrega da V1_4`() {
        val copaA = Competicao(id = 1L, nome = "Copa A", cidade = "Sorriso", modalidade = "Futebol")
        val jogo1 = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L), LocalDate.of(2026, 9, 1))
        val jogo2 = marcarComoRecebido(jogo(valor = 2_000, competicao = "Copa A", competicaoId = 1L), LocalDate.of(2026, 9, 10))

        val itens = listOf(jogo1, jogo2).agruparHistoricoPorCompeticao(FiltroHistoricoRecebimentos(), listOf(copaA))

        assertEquals(1, itens.size)
        assertEquals(1L, itens.single().competicaoId)
        assertEquals(2, itens.single().quantidadeJogos)
        assertEquals(3_000L, itens.single().valorRecebidoCentavos)
        assertEquals(LocalDate.of(2026, 9, 10), itens.single().ultimoPagamento)
    }

    @Test
    fun `agruparHistoricoPorCompeticao nunca conta o mesmo pagamento duas vezes mesmo com competicoes diferentes`() {
        val copaA = Competicao(id = 1L, nome = "Copa A")
        val copaB = Competicao(id = 2L, nome = "Copa B", status = StatusCompeticao.ENCERRADA)
        val deA = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L), LocalDate.of(2026, 9, 1))
        val deB = marcarComoRecebido(jogo(valor = 5_000, competicao = "Copa B", competicaoId = 2L), LocalDate.of(2026, 9, 2))

        val itens = listOf(deA, deB).agruparHistoricoPorCompeticao(FiltroHistoricoRecebimentos(), listOf(copaA, copaB))

        assertEquals(2, itens.size)
        assertEquals(1_000L + 5_000L, itens.sumOf { it.valorRecebidoCentavos })
        assertTrue(itens.single { it.competicaoId == 2L }.encerrada)
        assertTrue(!itens.single { it.competicaoId == 1L }.encerrada)
    }

    @Test
    fun `agruparHistoricoPorCompeticao ordena grupos por competicao A-Z`() {
        val copaA = Competicao(id = 1L, nome = "Copa A")
        val copaB = Competicao(id = 2L, nome = "Copa B")
        val deB = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa B", competicaoId = 2L), LocalDate.of(2026, 9, 1))
        val deA = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L), LocalDate.of(2026, 9, 1))

        val ordenado = listOf(deB, deA).agruparHistoricoPorCompeticao(
            FiltroHistoricoRecebimentos(ordenacao = OrdenacaoRecebimento.COMPETICAO_AZ),
            listOf(copaA, copaB),
        )

        assertEquals(listOf("Copa A", "Copa B"), ordenado.map { it.nome })
    }

    @Test
    fun `agruparHistoricoPorCompeticao preserva pagamentos de jogos sem competicao vinculada`() {
        val semVinculo = marcarComoRecebido(jogo(valor = 1_000, competicao = "Amistoso avulso", competicaoId = null), LocalDate.of(2026, 9, 1))

        val itens = listOf(semVinculo).agruparHistoricoPorCompeticao(FiltroHistoricoRecebimentos(), emptyList())

        assertEquals(1, itens.size)
        assertEquals(null, itens.single().competicaoId)
        assertEquals("Amistoso avulso", itens.single().nome)
        assertEquals(1_000L, itens.single().valorRecebidoCentavos)
    }

    @Test
    fun `agruparHistoricoPorCompeticao traz a quantidade de pagamentos por funcao - correcao pos entrega`() {
        val copaA = Competicao(id = 1L, nome = "Copa A")
        val arbitro1 = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L, funcao = "Árbitro"), LocalDate.of(2026, 9, 1))
        val arbitro2 = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L, funcao = "Árbitro"), LocalDate.of(2026, 9, 2))
        val assistente = marcarComoRecebido(jogo(valor = 1_000, competicao = "Copa A", competicaoId = 1L, funcao = "Assistente"), LocalDate.of(2026, 9, 3))

        val itens = listOf(arbitro1, arbitro2, assistente).agruparHistoricoPorCompeticao(FiltroHistoricoRecebimentos(), listOf(copaA))

        assertEquals(listOf("Árbitro" to 2, "Assistente" to 1), itens.single().porFuncao)
    }

    private fun jogo(
        data: LocalDate = LocalDate.of(2026, 9, 1),
        valor: Long,
        competicao: String? = "Competição",
        cidade: String? = null,
        modalidade: String? = null,
        competicaoId: Long? = null,
        funcao: String? = null,
    ): Jogo = Jogo(data = data, valorCentavos = valor, competicao = competicao, cidade = cidade, modalidade = modalidade, competicaoId = competicaoId, funcao = funcao)
}
