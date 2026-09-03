package com.meusjogos.arbitragem.core.logic

import com.meusjogos.arbitragem.core.model.FiltroJogos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.FiltroStatus
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Intervalo [inicio, fim] (ambos inclusive) representado pelo período selecionado. */
fun FiltroPeriodo.paraIntervalo(hoje: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate>? = when (this) {
    is FiltroPeriodo.Todos -> null
    is FiltroPeriodo.Hoje -> hoje to hoje
    is FiltroPeriodo.EstaSemana -> {
        val inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val fim = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        inicio to fim
    }
    is FiltroPeriodo.EsteMes -> hoje.withDayOfMonth(1) to hoje.with(TemporalAdjusters.lastDayOfMonth())
    is FiltroPeriodo.MesAnterior -> {
        val mesAnterior = hoje.minusMonths(1)
        mesAnterior.withDayOfMonth(1) to mesAnterior.with(TemporalAdjusters.lastDayOfMonth())
    }
    is FiltroPeriodo.EsteAno -> hoje.withDayOfYear(1) to hoje.with(TemporalAdjusters.lastDayOfYear())
    is FiltroPeriodo.Personalizado -> inicio to fim
}

private fun Jogo.correspondeA(query: String): Boolean {
    if (query.isBlank()) return true
    val termo = query.trim()
    val campos = listOf(equipeMandante, equipeVisitante, competicao, categoria, cidade, estadio, funcao, observacoes)
    return campos.any { campo -> campo != null && campo.contains(termo, ignoreCase = true) }
}

/**
 * Aplica status, período, competição, função e termo de pesquisa a uma lista
 * de jogos. Todos os campos usados na pesquisa podem estar vazios/nulos sem
 * quebrar a busca (ver REGRA 14 do prompt: "a pesquisa deve funcionar mesmo
 * que alguns campos estejam vazios").
 */
fun List<Jogo>.filtrarEPesquisar(filtro: FiltroJogos, hoje: LocalDate = LocalDate.now()): List<Jogo> {
    val intervalo = filtro.periodo.paraIntervalo(hoje)
    return filter { jogo ->
        val statusOk = when (filtro.status) {
            FiltroStatus.TODOS -> true
            FiltroStatus.A_RECEBER -> jogo.statusPagamento == StatusPagamento.A_RECEBER
            FiltroStatus.RECEBIDOS -> jogo.statusPagamento == StatusPagamento.RECEBIDO
        }
        val periodoOk = intervalo == null || (jogo.data >= intervalo.first && jogo.data <= intervalo.second)
        val competicaoOk = filtro.competicao.isNullOrBlank() ||
            jogo.competicao.equals(filtro.competicao, ignoreCase = true)
        val funcaoOk = filtro.funcao.isNullOrBlank() || jogo.funcao.equals(filtro.funcao, ignoreCase = true)
        val pesquisaOk = jogo.correspondeA(filtro.pesquisa)
        statusOk && periodoOk && competicaoOk && funcaoOk && pesquisaOk
    }
}

/** Ordenação padrão da lista "Meus Jogos": mais recente para mais antigo. */
fun List<Jogo>.ordenarMaisRecentePrimeiro(): List<Jogo> =
    sortedWith(compareByDescending<Jogo> { it.data }.thenByDescending { it.id })
