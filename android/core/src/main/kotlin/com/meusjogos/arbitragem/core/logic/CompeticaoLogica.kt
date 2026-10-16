package com.meusjogos.arbitragem.core.logic

import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.ResumoCompeticao
import com.meusjogos.arbitragem.core.model.StatusCompeticao

/**
 * Regras da VERSÃO 1.2: a competição vira um cadastro central, identificado
 * por id, mas DEDUPLICADO por uma chave normalizada de nome+cidade+modalidade
 * — duas competições só são "a mesma" quando essa chave bate, ignorando
 * maiúsculas/minúsculas e espaços nas pontas/duplicados (REGRA 1). Nunca
 * altera o texto original digitado pelo usuário — a normalização existe só
 * para a comparação.
 */

private fun normalizarTexto(texto: String?): String =
    (texto ?: "").trim().replace(Regex("\\s+"), " ").uppercase()

/** Chave de agrupamento: nome + cidade + modalidade, todos normalizados. Duas competições com a
 * mesma chave são consideradas a mesma competição (REGRA 1 da V1.2). */
fun chaveNormalizadaCompeticao(nome: String?, cidade: String?, modalidade: String?): String =
    "${normalizarTexto(nome)}|${normalizarTexto(cidade)}|${normalizarTexto(modalidade)}"

fun Competicao.chaveNormalizada(): String = chaveNormalizadaCompeticao(nome, cidade, modalidade)

/** Encontra, numa lista de competições já cadastradas, a que é equivalente (mesma chave normalizada)
 * ao nome/cidade/modalidade informados — usado para nunca criar uma segunda competição equivalente
 * (REGRA 16) e para o autocomplete (REGRA 6) reconhecer uma competição já existente. */
fun List<Competicao>.encontrarEquivalente(nome: String, cidade: String?, modalidade: String?): Competicao? {
    val chave = chaveNormalizadaCompeticao(nome, cidade, modalidade)
    return firstOrNull { it.chaveNormalizada() == chave }
}

/** Sugestões de autocomplete (REGRA 6): competições cujo nome contém o texto digitado, ordenadas
 * alfabeticamente — comparação sempre ignorando maiúsculas/minúsculas. */
fun List<Competicao>.sugestoesPara(textoDigitado: String): List<Competicao> {
    if (textoDigitado.isBlank()) return emptyList()
    val termo = textoDigitado.trim()
    return filter { it.nome.contains(termo, ignoreCase = true) }
        .sortedBy { it.nome.lowercase() }
}

/** Resumo de uma competição — sempre recalculado a partir dos jogos vinculados a ela por
 * [Jogo.competicaoId], nunca armazenado (mesmo princípio de Totais.kt). */
fun List<Jogo>.resumoDaCompeticao(competicaoId: Long): ResumoCompeticao {
    val doCompeticao = filter { it.competicaoId == competicaoId }
    if (doCompeticao.isEmpty()) return ResumoCompeticao.VAZIO
    val recebidoCentavos = doCompeticao.totalRecebidoCentavos()
    val aReceberCentavos = doCompeticao.totalAReceberCentavos()
    return ResumoCompeticao(
        totalJogos = doCompeticao.size,
        jogosRecebidos = doCompeticao.quantidadeRecebidos(),
        jogosPendentes = doCompeticao.quantidadeAReceber(),
        valorTotalCentavos = recebidoCentavos + aReceberCentavos,
        valorRecebidoCentavos = recebidoCentavos,
        valorPendenteCentavos = aReceberCentavos,
    )
}

/** Todos os jogos vinculados a uma competição (por id) — não cria, duplica nem altera nenhum jogo. */
fun List<Jogo>.jogosDaCompeticaoId(competicaoId: Long): List<Jogo> = filter { it.competicaoId == competicaoId }

/** Quantidade de jogos de uma competição, já com seu status — usado para colorir o chip "Por
 * competição" em Jogos (🟢 em andamento / 🔴 encerrada, REGRA 17). */
data class ContagemCompeticao(val id: Long, val nome: String, val quantidade: Int, val encerrada: Boolean)

/** Quantidade de jogos por competição (REGRA do chip "Por competição" em Jogos) — agrupa por
 * [Jogo.competicaoId] (não por texto), então nunca conta duas competições equivalentes como
 * separadas. Jogos sem competição vinculada não entram na contagem. */
fun List<Jogo>.contarPorCompeticaoComStatus(competicoes: List<Competicao>): List<ContagemCompeticao> {
    val encerradaPorId = competicoes.associateBy({ it.id }, { it.encerrada })
    return filter { it.competicaoId != null && !it.competicao.isNullOrBlank() }
        .groupBy { it.competicaoId!! }
        .map { (id, jogosDoGrupo) ->
            ContagemCompeticao(
                id = id,
                nome = jogosDoGrupo.first().competicao!!,
                quantidade = jogosDoGrupo.size,
                encerrada = encerradaPorId[id] ?: false,
            )
        }
        .sortedByDescending { it.quantidade }
}

/** Marca uma competição como encerrada, registrando a data/hora (REGRA 8 da V1.2). */
fun encerrarCompeticao(competicao: Competicao, agora: java.time.Instant = java.time.Instant.now()): Competicao =
    competicao.copy(status = StatusCompeticao.ENCERRADA, dataEncerramento = agora)

/** Reabre uma competição encerrada, limpando a data de encerramento. */
fun reabrirCompeticao(competicao: Competicao): Competicao =
    competicao.copy(status = StatusCompeticao.PADRAO, dataEncerramento = null)

/** Uma competição ENCERRADA junto com seu resumo financeiro — usada no painel "Competições
 * encerradas a receber" e no card do Início (VERSÃO 1.3). */
data class CompeticaoComResumo(val competicao: Competicao, val resumo: ResumoCompeticao)

/**
 * Competições ENCERRADAS que ainda têm saldo pendente (VERSÃO 1.3, seção 3/4/6) — nunca inclui
 * competições em andamento (REGRA 6), nem encerradas já totalmente recebidas (REGRA 5). Reaproveita
 * [resumoDaCompeticao] (mesma fonte de verdade usada em toda a tela da competição), então o total
 * aqui nunca pode divergir ou contar um pagamento duas vezes (REGRA 7/10).
 */
fun List<Competicao>.encerradasComPendencia(jogos: List<Jogo>): List<CompeticaoComResumo> =
    filter { it.encerrada }
        .map { competicao -> CompeticaoComResumo(competicao, jogos.resumoDaCompeticao(competicao.id)) }
        .filter { it.resumo.valorPendenteCentavos > 0 }
        .sortedByDescending { it.resumo.valorPendenteCentavos }

/**
 * Competições EM ANDAMENTO que já têm saldo pendente (VERSÃO 1.4, seção 1) — espelha
 * [encerradasComPendencia] para o lado "ainda abertas": some desta lista tão logo a competição
 * seja encerrada (passa a aparecer em [encerradasComPendencia], se ainda houver pendência) ou
 * tão logo o saldo chegue a zero.
 */
fun List<Competicao>.emAndamentoComPendencia(jogos: List<Jogo>): List<CompeticaoComResumo> =
    filterNot { it.encerrada }
        .map { competicao -> CompeticaoComResumo(competicao, jogos.resumoDaCompeticao(competicao.id)) }
        .filter { it.resumo.valorPendenteCentavos > 0 }
        .sortedByDescending { it.resumo.valorPendenteCentavos }

/**
 * Competições com QUALQUER recebimento já registrado (VERSÃO 1.4, seção 2) — independente do
 * status (em andamento ou encerrada) e mesmo que ainda reste saldo pendente: uma competição pode
 * aparecer aqui E em um dos módulos de pendência ao mesmo tempo, o que é esperado (REGRA 7 da
 * seção 7). Nunca duplica um pagamento — reaproveita [resumoDaCompeticao] como fonte única.
 */
fun List<Competicao>.comRecebimentos(jogos: List<Jogo>): List<CompeticaoComResumo> =
    map { competicao -> CompeticaoComResumo(competicao, jogos.resumoDaCompeticao(competicao.id)) }
        .filter { it.resumo.jogosRecebidos > 0 }
        .sortedByDescending { it.resumo.valorRecebidoCentavos }

// ---------------------------------------------------------------------
// Agrupamento por texto bruto (usado na migração de dados existentes e na
// unificação ao editar) — generaliza o mesmo padrão de
// agruparCidadesIgnorandoCaixa (Totais.kt) para nome+cidade+modalidade.
// ---------------------------------------------------------------------

/** Um grupo de registros de competição (lidos como texto bruto) considerados equivalentes —
 * [nome]/[cidade]/[modalidade] são a grafia mais usada entre os registros do grupo (empate: a
 * primeira encontrada), preservando a forma já usada pelo usuário em vez de forçar uma nova. */
data class GrupoCompeticaoNormalizado(
    val chave: String,
    val nome: String,
    val cidade: String?,
    val modalidade: String?,
    val quantidade: Int,
)

private fun List<String>.grafiaMaisUsada(): String =
    groupingBy { it }.eachCount().entries.maxByOrNull { it.value }!!.key

/**
 * Agrupa registros brutos (nome, cidade, modalidade) — tipicamente lidos diretamente dos jogos já
 * cadastrados — por chave normalizada (REGRA 15, migração/limpeza da V1.2). Linhas com nome em
 * branco são ignoradas (jogo sem competição informada não gera competição nenhuma).
 */
fun List<Triple<String?, String?, String?>>.agruparCompeticoesEquivalentes(): List<GrupoCompeticaoNormalizado> =
    filter { (nome, _, _) -> !nome.isNullOrBlank() }
        .groupBy { (nome, cidade, modalidade) -> chaveNormalizadaCompeticao(nome, cidade, modalidade) }
        .map { (chave, linhas) ->
            GrupoCompeticaoNormalizado(
                chave = chave,
                nome = linhas.map { it.first!!.trim() }.grafiaMaisUsada(),
                cidade = linhas.mapNotNull { it.second?.trim()?.takeIf(String::isNotBlank) }.takeIf { it.isNotEmpty() }?.grafiaMaisUsada(),
                modalidade = linhas.mapNotNull { it.third?.trim()?.takeIf(String::isNotBlank) }.takeIf { it.isNotEmpty() }?.grafiaMaisUsada(),
                quantidade = linhas.size,
            )
        }
