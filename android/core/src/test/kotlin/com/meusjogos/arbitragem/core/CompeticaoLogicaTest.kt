package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.agruparCompeticoesEquivalentes
import com.meusjogos.arbitragem.core.logic.chaveNormalizadaCompeticao
import com.meusjogos.arbitragem.core.logic.contarPorCompeticaoId
import com.meusjogos.arbitragem.core.logic.encerrarCompeticao
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
    fun `contarPorCompeticaoId agrupa por id e nunca por texto duplicado`() {
        val jogos = listOf(
            jogoDaCompeticao(competicaoId = 1L, nome = "Copa Sorriso", valor = 1_000),
            jogoDaCompeticao(competicaoId = 1L, nome = "Copa Sorriso", valor = 1_000),
            jogoDaCompeticao(competicaoId = 2L, nome = "Copa Sinop", valor = 1_000),
            jogoDaCompeticao(competicaoId = null, nome = null, valor = 1_000),
        )

        val contagem = jogos.contarPorCompeticaoId()

        assertEquals(2, contagem.size)
        assertEquals(Triple(1L, "Copa Sorriso", 2), contagem.first())
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

    private fun jogoDaCompeticao(competicaoId: Long?, valor: Long, recebido: Boolean = false, nome: String? = "Competição"): Jogo {
        val data = LocalDate.of(2026, 9, 1)
        val base = Jogo(data = data, valorCentavos = valor, competicao = nome, competicaoId = competicaoId)
        return if (recebido) marcarComoRecebido(base, data) else base
    }
}
