package com.meusjogos.arbitragem.core

import com.meusjogos.arbitragem.core.logic.filtrarEPesquisar
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido
import com.meusjogos.arbitragem.core.logic.ordenarMaisRecentePrimeiro
import com.meusjogos.arbitragem.core.model.FiltroJogos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.FiltroStatus
import com.meusjogos.arbitragem.core.model.Jogo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FiltroLogicaTest {

    @Test
    fun `pesquisa funciona mesmo com jogo totalmente sem campos opcionais`() {
        val jogos = listOf(
            Jogo(data = LocalDate.of(2026, 9, 3), valorCentavos = 40_000), // cadastro ultrarrápido, tudo nulo
            Jogo(data = LocalDate.of(2026, 9, 4), valorCentavos = 50_000, equipeMandante = "Sorriso FC", equipeVisitante = "Sinop FC"),
        )

        val resultado = jogos.filtrarEPesquisar(FiltroJogos(pesquisa = "Sorriso"))

        assertEquals(1, resultado.size)
        assertEquals("Sorriso FC", resultado.first().equipeMandante)
    }

    @Test
    fun `pesquisa e insensivel a maiusculas e busca em varios campos`() {
        val jogos = listOf(
            Jogo(data = LocalDate.now(), valorCentavos = 1000, competicao = "Campeonato Estadual"),
            Jogo(data = LocalDate.now(), valorCentavos = 1000, funcao = "Árbitro"),
            Jogo(data = LocalDate.now(), valorCentavos = 1000, observacoes = "jogo tranquilo"),
        )

        assertEquals(1, jogos.filtrarEPesquisar(FiltroJogos(pesquisa = "estadual")).size)
        assertEquals(1, jogos.filtrarEPesquisar(FiltroJogos(pesquisa = "árbitro")).size)
        assertEquals(1, jogos.filtrarEPesquisar(FiltroJogos(pesquisa = "TRANQUILO")).size)
        assertEquals(0, jogos.filtrarEPesquisar(FiltroJogos(pesquisa = "não existe")).size)
    }

    @Test
    fun `filtro de status separa a receber e recebidos`() {
        val recebido = marcarComoRecebido(Jogo(data = LocalDate.now(), valorCentavos = 1000))
        val aReceber = Jogo(data = LocalDate.now(), valorCentavos = 2000)
        val jogos = listOf(recebido, aReceber)

        assertEquals(1, jogos.filtrarEPesquisar(FiltroJogos(status = FiltroStatus.RECEBIDOS)).size)
        assertEquals(1, jogos.filtrarEPesquisar(FiltroJogos(status = FiltroStatus.A_RECEBER)).size)
        assertEquals(2, jogos.filtrarEPesquisar(FiltroJogos(status = FiltroStatus.TODOS)).size)
    }

    @Test
    fun `filtro de periodo personalizado inclui limites`() {
        val jogos = listOf(
            Jogo(data = LocalDate.of(2026, 9, 1), valorCentavos = 1000),
            Jogo(data = LocalDate.of(2026, 9, 15), valorCentavos = 1000),
            Jogo(data = LocalDate.of(2026, 9, 30), valorCentavos = 1000),
            Jogo(data = LocalDate.of(2026, 10, 1), valorCentavos = 1000),
        )

        val filtro = FiltroJogos(
            periodo = FiltroPeriodo.Personalizado(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
        )

        val resultado = jogos.filtrarEPesquisar(filtro)
        assertEquals(3, resultado.size)
    }

    @Test
    fun `filtro por competicao e funcao combinados`() {
        val jogos = listOf(
            Jogo(data = LocalDate.now(), valorCentavos = 1000, competicao = "Copa", funcao = "Árbitro"),
            Jogo(data = LocalDate.now(), valorCentavos = 1000, competicao = "Copa", funcao = "VAR"),
            Jogo(data = LocalDate.now(), valorCentavos = 1000, competicao = "Amistoso", funcao = "Árbitro"),
        )

        val resultado = jogos.filtrarEPesquisar(FiltroJogos(competicao = "Copa", funcao = "Árbitro"))
        assertEquals(1, resultado.size)
    }

    @Test
    fun `ordenacao padrao e do mais recente para o mais antigo`() {
        val antigo = Jogo(id = 1, data = LocalDate.of(2026, 1, 1), valorCentavos = 1000)
        val recente = Jogo(id = 2, data = LocalDate.of(2026, 9, 1), valorCentavos = 1000)

        val ordenado = listOf(antigo, recente).ordenarMaisRecentePrimeiro()

        assertEquals(recente, ordenado.first())
        assertTrue(ordenado.first().data.isAfter(ordenado.last().data))
    }
}
