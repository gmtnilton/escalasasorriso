package com.meusjogos.arbitragem.core.logic

import com.meusjogos.arbitragem.core.model.Estatisticas
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.PontoMensal
import com.meusjogos.arbitragem.core.model.ResumoAnual
import com.meusjogos.arbitragem.core.model.ResumoPeriodo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import java.time.LocalDate

/**
 * Regra financeira central do app, repetida em todo o prompt original:
 *
 *   TOTAL GERAL = TOTAL RECEBIDO + TOTAL A RECEBER
 *
 * Nunca é armazenada — é sempre recalculada a partir da lista de jogos,
 * o que garante que jamais fique dessincronizada (ver testes de integridade
 * financeira em IntegridadeFinanceiraTest).
 */

fun List<Jogo>.totalAReceberCentavos(): Long =
    filter { it.statusPagamento == StatusPagamento.A_RECEBER }.sumOf { it.valorCentavos }

fun List<Jogo>.totalRecebidoCentavos(): Long =
    filter { it.statusPagamento == StatusPagamento.RECEBIDO }.sumOf { it.valorCentavos }

fun List<Jogo>.totalGeralCentavos(): Long = sumOf { it.valorCentavos }

fun List<Jogo>.quantidadeRecebidos(): Int = count { it.statusPagamento == StatusPagamento.RECEBIDO }

fun List<Jogo>.quantidadeAReceber(): Int = count { it.statusPagamento == StatusPagamento.A_RECEBER }

fun List<Jogo>.paraResumoPeriodo(): ResumoPeriodo = ResumoPeriodo(
    totalJogos = size,
    totalRecebidoCentavos = totalRecebidoCentavos(),
    totalAReceberCentavos = totalAReceberCentavos(),
)

/** Resumo do mês/ano informado, considerando a DATA DO JOGO (não a de recebimento). */
fun List<Jogo>.resumoDoMes(ano: Int, mes: Int): ResumoPeriodo =
    filter { it.data.year == ano && it.data.monthValue == mes }.paraResumoPeriodo()

/** Resumo do ano informado, já com a média por jogo calculada. */
fun List<Jogo>.resumoDoAno(ano: Int): ResumoAnual {
    val doAno = filter { it.data.year == ano }
    return ResumoAnual(
        ano = ano,
        totalJogos = doAno.size,
        totalRecebidoCentavos = doAno.totalRecebidoCentavos(),
        totalAReceberCentavos = doAno.totalAReceberCentavos(),
    )
}

fun List<Jogo>.estatisticas(): Estatisticas {
    if (isEmpty()) return Estatisticas.VAZIO
    return Estatisticas(
        totalJogos = size,
        totalRecebidoCentavos = totalRecebidoCentavos(),
        totalAReceberCentavos = totalAReceberCentavos(),
        jogosRecebidos = quantidadeRecebidos(),
        jogosPendentes = quantidadeAReceber(),
        maiorValorCentavos = maxOf { it.valorCentavos },
    )
}

/** Série mensal (jan..dez) do ano informado, para os gráficos simples de "por mês". */
fun List<Jogo>.serieMensal(ano: Int): List<PontoMensal> = (1..12).map { mes ->
    val doMes = filter { it.data.year == ano && it.data.monthValue == mes }
    PontoMensal(
        ano = ano,
        mes = mes,
        totalJogos = doMes.size,
        totalRecebidoCentavos = doMes.totalRecebidoCentavos(),
        totalAReceberCentavos = doMes.totalAReceberCentavos(),
    )
}

/** Todos os anos com pelo menos um jogo cadastrado, do mais recente para o mais antigo. */
fun List<Jogo>.anosDisponiveis(): List<Int> = map { it.data.year }.distinct().sortedDescending()

/** Todas as competições distintas já cadastradas (não vazias), em ordem alfabética. */
fun List<Jogo>.competicoesDisponiveis(): List<String> =
    mapNotNull { it.competicao?.takeIf(String::isNotBlank) }.distinct().sorted()

/** Todas as funções distintas já cadastradas (não vazias), em ordem alfabética. */
fun List<Jogo>.funcoesDisponiveis(): List<String> =
    mapNotNull { it.funcao?.takeIf(String::isNotBlank) }.distinct().sorted()

/** Todas as cidades distintas já cadastradas (não vazias), em ordem alfabética. */
fun List<Jogo>.cidadesDisponiveis(): List<String> =
    mapNotNull { it.cidade?.takeIf(String::isNotBlank) }.distinct().sorted()

/** Todas as modalidades distintas já cadastradas (não vazias), em ordem alfabética. */
fun List<Jogo>.modalidadesDisponiveis(): List<String> =
    mapNotNull { it.modalidade?.takeIf(String::isNotBlank) }.distinct().sorted()

/** Quantidade de jogos por cidade (não vazias), da mais frequente para a menos. */
fun List<Jogo>.contarPorCidade(): List<Pair<String, Int>> =
    mapNotNull { it.cidade?.takeIf(String::isNotBlank) }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

/** Quantidade de jogos por modalidade (não vazias), da mais frequente para a menos. */
fun List<Jogo>.contarPorModalidade(): List<Pair<String, Int>> =
    mapNotNull { it.modalidade?.takeIf(String::isNotBlank) }
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

/**
 * Cria a cópia de um jogo para a função "Duplicar jogo".
 *
 * Mantém competição, categoria, cidade, estádio, função, observações e equipes (para
 * facilitar o cadastro de jogos semelhantes), mas NUNCA copia o status de
 * pagamento nem a data de recebimento — o novo jogo sempre nasce A RECEBER,
 * pronto para ser editado (data, equipes e valor ficam livres para ajuste).
 */
fun duplicarJogo(original: Jogo, agora: java.time.Instant = java.time.Instant.now()): Jogo = original.copy(
    id = 0L,
    statusPagamento = StatusPagamento.PADRAO,
    dataRecebimento = null,
    dataCriacao = agora,
    dataAtualizacao = agora,
)

/** Marca um jogo como recebido, registrando a data do recebimento (hoje, por padrão). */
fun marcarComoRecebido(
    jogo: Jogo,
    dataRecebimento: LocalDate = LocalDate.now(),
    agora: java.time.Instant = java.time.Instant.now(),
): Jogo = jogo.copy(
    statusPagamento = StatusPagamento.RECEBIDO,
    dataRecebimento = dataRecebimento,
    dataAtualizacao = agora,
)

/** Desfaz um recebimento, devolvendo o jogo para A RECEBER e limpando a data de recebimento. */
fun desfazerRecebimento(jogo: Jogo, agora: java.time.Instant = java.time.Instant.now()): Jogo = jogo.copy(
    statusPagamento = StatusPagamento.A_RECEBER,
    dataRecebimento = null,
    dataAtualizacao = agora,
)
