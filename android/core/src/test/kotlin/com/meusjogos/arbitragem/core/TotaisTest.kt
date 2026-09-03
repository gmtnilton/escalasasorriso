package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.estatisticas
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.resumoDoAno
import com.meusjogos.arbitragem.core.logic.resumoDoMes
import com.meusjogos.arbitragem.core.logic.totalAReceberCentavos
import com.meusjogos.arbitragem.core.logic.totalGeralCentavos
import com.meusjogos.arbitragem.core.logic.totalRecebidoCentavos
import com.meusjogos.arbitragem.core.model.Jogo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TotaisTest {

    private fun jogo(data: LocalDate, valor: Long, recebido: Boolean = false) =
        if (recebido) marcarComoRecebido(Jogo(data = data, valorCentavos = valor), data)
        else Jogo(data = data, valorCentavos = valor)

    @Test
    fun `total geral e sempre recebido mais a receber`() {
        val jogos = listOf(
            jogo(LocalDate.of(2026, 9, 1), 40_000, recebido = true),
            jogo(LocalDate.of(2026, 9, 5), 20_000),
            jogo(LocalDate.of(2026, 9, 10), 10_000),
        )

        assertEquals(40_000L, jogos.totalRecebidoCentavos())
        assertEquals(30_000L, jogos.totalAReceberCentavos())
        assertEquals(70_000L, jogos.totalGeralCentavos())
        assertEquals(jogos.totalRecebidoCentavos() + jogos.totalAReceberCentavos(), jogos.totalGeralCentavos())
    }

    @Test
    fun `lista vazia gera todos os totais zerados`() {
        val jogos = emptyList<Jogo>()
        assertEquals(0L, jogos.totalRecebidoCentavos())
        assertEquals(0L, jogos.totalAReceberCentavos())
        assertEquals(0L, jogos.totalGeralCentavos())
    }

    @Test
    fun `resumo mensal considera apenas jogos do mes e ano informados - exemplo secao 4`() {
        val jogos = listOf(
            jogo(LocalDate.of(2026, 9, 3), 150_000, recebido = true),
            jogo(LocalDate.of(2026, 9, 20), 80_000),
            jogo(LocalDate.of(2026, 8, 15), 999_00), // agosto não deve entrar
        )

        val resumo = jogos.resumoDoMes(2026, 9)

        assertEquals(2, resumo.totalJogos)
        assertEquals(150_000L, resumo.totalRecebidoCentavos)
        assertEquals(80_000L, resumo.totalAReceberCentavos)
        assertEquals(230_000L, resumo.totalGeralCentavos)
    }

    @Test
    fun `resumo anual calcula media por jogo - exemplo secao 17`() {
        // 85 jogos somando exatamente R$ 25.700,00, como no exemplo do prompt.
        val jogosExemplo = List(85) { i ->
            val valor = if (i < 84) 25_700_00L / 85 else 25_700_00L - (25_700_00L / 85) * 84
            jogo(LocalDate.of(2026, (i % 12) + 1, 10), valor, recebido = i < 74)
        }

        val resumo = jogosExemplo.resumoDoAno(2026)
        assertEquals(85, resumo.totalJogos)
        assertEquals(25_700_00L, resumo.totalGeralCentavos)
        assertEquals(resumo.totalGeralCentavos / 85, resumo.mediaPorJogoCentavos)
    }

    @Test
    fun `estatisticas retorna maior valor e quantidades corretas`() {
        val jogos = listOf(
            jogo(LocalDate.now(), 45_000, recebido = true),
            jogo(LocalDate.now(), 90_000),
            jogo(LocalDate.now(), 30_000, recebido = true),
        )

        val stats = jogos.estatisticas()

        assertEquals(3, stats.totalJogos)
        assertEquals(2, stats.jogosRecebidos)
        assertEquals(1, stats.jogosPendentes)
        assertEquals(90_000L, stats.maiorValorCentavos)
        assertEquals(165_000L, stats.totalGeralCentavos)
    }

    @Test
    fun `estatisticas de lista vazia nao lanca excecao`() {
        val stats = emptyList<Jogo>().estatisticas()
        assertEquals(0, stats.totalJogos)
        assertEquals(0L, stats.maiorValorCentavos)
        assertEquals(0L, stats.mediaPorJogoCentavos)
    }
}
