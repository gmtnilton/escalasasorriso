package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.agruparCompeticoesEquivalentes
import com.meusjogos.arbitragem.core.logic.chaveNormalizadaCompeticao
import com.meusjogos.arbitragem.core.logic.comRecebimentos
import com.meusjogos.arbitragem.core.logic.contarPorCompeticaoComStatus
import com.meusjogos.arbitragem.core.logic.emAndamentoComPendencia
import com.meusjogos.arbitragem.core.logic.encerrarCompeticao
import com.meusjogos.arbitragem.core.logic.encerradasComPendencia
import com.meusjogos.arbitragem.core.logic.encontrarEquivalente
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.reabrirCompeticao
import com.meusjogos.arbitragem.core.logic.resumoDaCompeticao
import com.meusjogos.arbitragem.core.logic.sugestoesPara
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusCompeticao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CompeticaoLogicaTest {

    @Test
    fun `chave normalizada ignora maiusculas minusculas e espacos - exemplo da secao 1`() {
        val a = chaveNormalizadaCompeticao("Campeonato Municipal", "Sorriso", "Futebol")
        val b = chaveNormalizadaCompeticao("campeonato municipal", "SORRISO", "futebol")
        val c = chaveNormalizadaCompeticao("  Campeonato   Municipal  ", " Sorriso ", "Futebol")

        assertEquals(a, b)
        assertEquals(a, c)
    }

    @Test
    fun `chave normalizada distingue competicoes realmente diferentes`() {
        val a = chaveNormalizadaCompeticao("Copa Sorriso", "Sorriso", "Futebol")
        val b = chaveNormalizadaCompeticao("Copa Sorriso", "Sinop", "Futebol") // cidade diferente
        val c = chaveNormalizadaCompeticao("Copa Sinop", "Sorriso", "Futebol") // nome diferente

        assertTrue(a != b)
        assertTrue(a != c)
    }

    @Test
    fun `encontrarEquivalente acha competicao existente ignorando formatacao - exemplo secao 2`() {
        val existentes = listOf(
            Competicao(id = 1L, nome = "Copa Sorriso", cidade = "Sorriso", modalidade = "Futebol"),
            Competicao(id = 2L, nome = "Copa Sinop", cidade = "Sinop", modalidade = "Futebol"),
        )

        val equivalente = existentes.encontrarEquivalente("COPA SORRISO", " sorriso ", "futebol")

        assertNotNull(equivalente)
        assertEquals(1L, equivalente!!.id)
    }

    @Test
    fun `encontrarEquivalente retorna null quando nao ha correspondencia`() {
        val existentes = listOf(Competicao(id = 1L, nome = "Copa Sorriso", cidade = "Sorriso", modalidade = "Futebol"))
        assertNull(existentes.encontrarEquivalente("Copa Municipal 2026", "Sorriso", "Futebol"))
    }

    @Test
    fun `sugestoesPara filtra por nome ignorando caixa e ordena alfabeticamente`() {
        val competicoes = listOf(
            Competicao(id = 1L, nome = "Copa Municipal 2026", cidade = "Sorriso", modalidade = "Futebol"),
            Competicao(id = 2L, nome = "Campeonato Estadual", cidade = "Sorriso", modalidade = "Futsal"),
            Competicao(id = 3L, nome = "Copa Município Antiga", cidade = "Sinop", modalidade = "Futebol"),
        )

        val sugestoes = competicoes.sugestoesPara("copa mun")

        // Ordenação lexicográfica simples (mesmo critério de competicoesDisponiveis() em
        // Totais.kt): "i" (U+0069) vem antes de "í" (U+00ED), então "Municipal" < "Município".
        assertEquals(listOf("Copa Municipal 2026", "Copa Município Antiga"), sugestoes.map { it.nome })
    }

    @Test
    fun `sugestoesPara retorna vazio para texto em branco`() {
        val competicoes = listOf(Competicao(id = 1L, nome = "Copa Sorriso"))
        assertTrue(competicoes.sugestoesPara("").isEmpty())
        assertTrue(competicoes.sugestoesPara("   ").isEmpty())
    }

    @Test
    fun `resumoDaCompeticao soma apenas os jogos vinculados aquela competicao`() {
        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, valor = 10_000, recebido = true),
            jogoDaCompeticao(competicaoId = 1L, valor = 20_000, recebido = false),
            jogoDaCompeticao(competicaoId = 2L, valor = 99_000, recebido = true), // outra competição
            jogoDaCompeticao(competicaoId = null, valor = 5_000, recebido = false), // sem competição
        )

        val resumo = jogos.resumoDaCompeticao(1L)

        assertEquals(2, resumo.totalJogos)
        assertEquals(1, resumo.jogosRecebidos)
        assertEquals(1, resumo.jogosPendentes)
        assertEquals(10_000L, resumo.valorRecebidoCentavos)
        assertEquals(20_000L, resumo.valorPendenteCentavos)
        assertEquals(30_000L, resumo.valorTotalCentavos)
    }

    @Test
    fun `resumoDaCompeticao de competicao sem jogos e vazio`() {
        val resumo = emptyList<Jogo>().resumoDaCompeticao(99L)
        assertEquals(0, resumo.totalJogos)
        assertEquals(0L, resumo.valorTotalCentavos)
    }

    @Test
    fun `contarPorCompeticaoComStatus agrupa por id e nunca por texto duplicado`() {
        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, nome = "Copa Sorriso", valor = 1_000),
            jogoDaCompeticao(competicaoId = 1L, nome = "Copa Sorriso", valor = 1_000),
            jogoDaCompeticao(competicaoId = 2L, nome = "Copa Sinop", valor = 1_000),
            jogoDaCompeticao(competicaoId = null, nome = null, valor = 1_000),
        )
        val competicoes = listOf(
            Competicao(id = 1L, nome = "Copa Sorriso", status = StatusCompeticao.EM_ANDAMENTO),
            Competicao(id = 2L, nome = "Copa Sinop", status = StatusCompeticao.ENCERRADA),
        )

        val contagem = jogos.contarPorCompeticaoComStatus(competicoes)

        assertEquals(2, contagem.size)
        assertEquals(1L, contagem[0].id)
        assertEquals("Copa Sorriso", contagem[0].nome)
        assertEquals(2, contagem[0].quantidade)
        assertFalse(contagem[0].encerrada)
        assertEquals(2L, contagem[1].id)
        assertTrue(contagem[1].encerrada)
    }

    @Test
    fun `contarPorCompeticaoComStatus trata competicao sem registro central como em andamento`() {
        val jogos = listOf(jogoDaCompeticao(competicaoId = 1L, nome = "Copa Sorriso", valor = 1_000))

        val contagem = jogos.contarPorCompeticaoComStatus(competicoes = emptyList())

        assertEquals(1, contagem.size)
        assertFalse(contagem.first().encerrada)
    }

    @Test
    fun `encerrarCompeticao e reabrirCompeticao alternam status e data de encerramento`() {
        val competicao = Competicao(id = 1L, nome = "Copa Sorriso", status = StatusCompeticao.EM_ANDAMENTO)

        val encerrada = encerrarCompeticao(competicao)
        assertEquals(StatusCompeticao.ENCERRADA, encerrada.status)
        assertNotNull(encerrada.dataEncerramento)
        assertTrue(encerrada.encerrada)

        val reaberta = reabrirCompeticao(encerrada)
        assertEquals(StatusCompeticao.EM_ANDAMENTO, reaberta.status)
        assertNull(reaberta.dataEncerramento)
        assertFalse(reaberta.encerrada)
    }

    @Test
    fun `agruparCompeticoesEquivalentes unifica registros equivalentes preservando a grafia mais usada - exemplo secao 15`() {
        val linhasBrutas = listOf(
            Triple("Campeonato Municipal", "Sorriso", "Futebol"),
            Triple("CAMPEONATO MUNICIPAL", "SORRISO", "futebol"),
            Triple("Campeonato Municipal", "Sorriso", "Futebol"),
            Triple("Copa Sinop", "Sinop", "Futebol"),
        )

        val grupos = linhasBrutas.agruparCompeticoesEquivalentes()

        assertEquals(2, grupos.size)
        val grupoMunicipal = grupos.first { it.quantidade == 3 }
        assertEquals("Campeonato Municipal", grupoMunicipal.nome) // grafia mais usada (2 de 3)
        assertEquals("Sorriso", grupoMunicipal.cidade)
        assertEquals("Futebol", grupoMunicipal.modalidade)
    }

    @Test
    fun `agruparCompeticoesEquivalentes ignora linhas sem nome`() {
        val linhas = listOf(Triple<String?, String?, String?>(null, "Sorriso", "Futebol"), Triple("", "Sorriso", "Futebol"))
        assertTrue(linhas.agruparCompeticoesEquivalentes().isEmpty())
    }

    @Test
    fun `encerradasComPendencia inclui so encerradas com saldo pendente - cenarios A B C E da V1_3`() {
        val encerradaComPendenciaTotal = Competicao(id = 1L, nome = "Copa A", status = StatusCompeticao.ENCERRADA) // cenário A
        val encerradaParcial = Competicao(id = 2L, nome = "Copa B", status = StatusCompeticao.ENCERRADA) // cenário B
        val encerradaQuitada = Competicao(id = 3L, nome = "Copa C", status = StatusCompeticao.ENCERRADA) // cenário C
        val emAndamentoComPendencia = Competicao(id = 4L, nome = "Copa D", status = StatusCompeticao.EM_ANDAMENTO) // cenário E

        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, valor = 20_000, recebido = false),
            jogoDaCompeticao(competicaoId = 2L, valor = 10_000, recebido = true),
            jogoDaCompeticao(competicaoId = 2L, valor = 10_000, recebido = false),
            jogoDaCompeticao(competicaoId = 3L, valor = 15_000, recebido = true),
            jogoDaCompeticao(competicaoId = 4L, valor = 50_000, recebido = false),
        )
        val competicoes = listOf(encerradaComPendenciaTotal, encerradaParcial, encerradaQuitada, emAndamentoComPendencia)

        val pendentes = competicoes.encerradasComPendencia(jogos)

        assertEquals(2, pendentes.size) // só as encerradas 1 e 2 — nunca a 3 (quitada) nem a 4 (em andamento)
        assertEquals(1L, pendentes[0].competicao.id) // maior saldo pendente primeiro
        assertEquals(20_000L, pendentes[0].resumo.valorPendenteCentavos)
        assertEquals(2L, pendentes[1].competicao.id)
        assertEquals(10_000L, pendentes[1].resumo.valorPendenteCentavos) // só o saldo restante, não o total
    }

    @Test
    fun `encerradasComPendencia some da lista quando o saldo chega a zero - cenario D`() {
        val competicao = Competicao(id = 1L, nome = "Copa A", status = StatusCompeticao.ENCERRADA)
        val jogoPendente = jogoDaCompeticao(competicaoId = 1L, valor = 20_000, recebido = false)

        val antesDoRecebimento = listOf(competicao).encerradasComPendencia(listOf(jogoPendente))
        assertEquals(1, antesDoRecebimento.size)

        val jogoRecebido = marcarComoRecebido(jogoPendente, LocalDate.of(2026, 9, 2))
        val depoisDoRecebimento = listOf(competicao).encerradasComPendencia(listOf(jogoRecebido))
        assertTrue(depoisDoRecebimento.isEmpty())
    }

    @Test
    fun `emAndamentoComPendencia inclui so em andamento com saldo pendente - secao 1 da V1_4`() {
        val emAndamentoPendente = Competicao(id = 1L, nome = "Copa A", status = StatusCompeticao.EM_ANDAMENTO)
        val emAndamentoQuitada = Competicao(id = 2L, nome = "Copa B", status = StatusCompeticao.EM_ANDAMENTO)
        val encerradaComPendencia = Competicao(id = 3L, nome = "Copa C", status = StatusCompeticao.ENCERRADA)

        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, valor = 20_000, recebido = false),
            jogoDaCompeticao(competicaoId = 2L, valor = 15_000, recebido = true),
            jogoDaCompeticao(competicaoId = 3L, valor = 30_000, recebido = false),
        )
        val competicoes = listOf(emAndamentoPendente, emAndamentoQuitada, encerradaComPendencia)

        val pendentes = competicoes.emAndamentoComPendencia(jogos)

        assertEquals(1, pendentes.size) // nunca a quitada (2), nunca a encerrada (3)
        assertEquals(1L, pendentes.first().competicao.id)
        assertEquals(20_000L, pendentes.first().resumo.valorPendenteCentavos)
    }

    @Test
    fun `comRecebimentos inclui qualquer competicao com recebimento, mesmo com saldo pendente - secao 2 7 da V1_4`() {
        val emAndamentoParcial = Competicao(id = 1L, nome = "Copa A", status = StatusCompeticao.EM_ANDAMENTO)
        val encerradaQuitada = Competicao(id = 2L, nome = "Copa B", status = StatusCompeticao.ENCERRADA)
        val semRecebimento = Competicao(id = 3L, nome = "Copa C", status = StatusCompeticao.EM_ANDAMENTO)

        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, valor = 10_000, recebido = true),
            jogoDaCompeticao(competicaoId = 1L, valor = 10_000, recebido = false), // ainda tem pendência
            jogoDaCompeticao(competicaoId = 2L, valor = 15_000, recebido = true),
            jogoDaCompeticao(competicaoId = 3L, valor = 9_000, recebido = false),
        )
        val competicoes = listOf(emAndamentoParcial, encerradaQuitada, semRecebimento)

        val comRecebimento = competicoes.comRecebimentos(jogos)

        assertEquals(2, comRecebimento.size) // 1 e 2 — nunca a 3 (nenhum recebimento)
        val parcial = comRecebimento.first { it.competicao.id == 1L }
        assertEquals(10_000L, parcial.resumo.valorRecebidoCentavos)
        assertEquals(10_000L, parcial.resumo.valorPendenteCentavos) // aparece aqui E teria pendência em outro módulo
        val quitada = comRecebimento.first { it.competicao.id == 2L }
        assertEquals(0L, quitada.resumo.valorPendenteCentavos)
    }

    private fun jogoDaCompeticao(competicaoId: Long?, valor: Long, recebido: Boolean = false, nome: String? = "Competição"): Jogo {
        val data = LocalDate.of(2026, 9, 1)
        val base = Jogo(data = data, valorCentavos = valor, competicao = nome, competicaoId = competicaoId)
        return if (recebido) marcarComoRecebido(base, data) else base
    }
}
