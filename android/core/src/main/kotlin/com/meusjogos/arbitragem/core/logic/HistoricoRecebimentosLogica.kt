package com.meusjogos.arbitragem.core.logic

import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.FiltroHistoricoRecebimentos
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.OrdenacaoRecebimento
import java.time.LocalDate

/**
 * Filtra e ordena o histórico de valores recebidos (VERSÃO 1.4, seção 5). Cada jogo recebido
 * representa um pagamento (este app registra um recebimento por jogo) — "consultar nunca cria
 * outro lançamento" (REGRA 8): esta função só lê e reordena, nunca grava nada.
 *
 * Diferença importante em relação a [filtrarEPesquisar]: o período aqui é aplicado sobre
 * [Jogo.dataRecebimento] (data do PAGAMENTO), não sobre [Jogo.data] (data do jogo).
 */
fun List<Jogo>.filtrarHistoricoRecebimentos(
    filtro: FiltroHistoricoRecebimentos,
    hoje: LocalDate = LocalDate.now(),
): List<Jogo> {
    val intervalo = filtro.periodo.paraIntervalo(hoje)
    val filtrados = recebidos().filter { jogo ->
        val dataPagamento = jogo.dataRecebimento
        val periodoOk = intervalo == null ||
            (dataPagamento != null && dataPagamento >= intervalo.first && dataPagamento <= intervalo.second)
        val competicaoOk = filtro.competicao.isNullOrBlank() ||
            jogo.competicao.equals(filtro.competicao, ignoreCase = true)
        val cidadeOk = filtro.cidade.isNullOrBlank() ||
            jogo.cidade?.trim().equals(filtro.cidade.trim(), ignoreCase = true)
        val modalidadeOk = filtro.modalidade.isNullOrBlank() ||
            jogo.modalidade.equals(filtro.modalidade, ignoreCase = true)
        val pesquisaOk = jogo.correspondeA(filtro.pesquisa)
        periodoOk && competicaoOk && cidadeOk && modalidadeOk && pesquisaOk
    }
    return when (filtro.ordenacao) {
        OrdenacaoRecebimento.MAIS_RECENTE -> filtrados.sortedWith(compareByDescending<Jogo> { it.dataRecebimento }.thenByDescending { it.id })
        OrdenacaoRecebimento.MAIS_ANTIGO -> filtrados.sortedWith(compareBy<Jogo> { it.dataRecebimento }.thenBy { it.id })
        OrdenacaoRecebimento.MAIOR_VALOR -> filtrados.sortedByDescending { it.valorCentavos }
        OrdenacaoRecebimento.MENOR_VALOR -> filtrados.sortedBy { it.valorCentavos }
        OrdenacaoRecebimento.COMPETICAO_AZ -> filtrados.sortedBy { (it.competicao ?: "").lowercase() }
    }
}

/**
 * Um item do histórico de valores recebidos, já agrupado por competição — mesma organização dos
 * módulos "Competições em aberto"/"Competições encerradas a receber", em vez de uma linha por
 * jogo. [competicaoId] só fica null quando os jogos do grupo não têm competição vinculada (dado
 * legado/texto livre); nesse caso o pagamento continua aparecendo (nunca se perde um registro),
 * só não há para onde navegar com "VER COMPETIÇÃO".
 */
data class ItemHistoricoRecebimento(
    val competicaoId: Long?,
    val nome: String,
    val cidade: String?,
    val modalidade: String?,
    val encerrada: Boolean,
    val valorRecebidoCentavos: Long,
    val quantidadeJogos: Int,
    val ultimoPagamento: LocalDate?,
    /** Quantidade de pagamentos (dentro do filtro atual) por função — mesmo padrão exibido nos
     * demais módulos que mostram competições. */
    val porFuncao: List<Pair<String, Int>> = emptyList(),
)

/**
 * Agrupa o histórico de valores recebidos por competição (correção pós-entrega da VERSÃO 1.4,
 * seção 2: a lista deve ficar organizada por competição, igual aos outros dois módulos
 * financeiros). Reaproveita [filtrarHistoricoRecebimentos] como única fonte do conjunto já
 * filtrado e ordenado por jogo — só agrupa o resultado, nunca recalcula nem duplica um pagamento.
 */
fun List<Jogo>.agruparHistoricoPorCompeticao(
    filtro: FiltroHistoricoRecebimentos,
    competicoes: List<Competicao>,
    hoje: LocalDate = LocalDate.now(),
): List<ItemHistoricoRecebimento> {
    val filtrados = filtrarHistoricoRecebimentos(filtro, hoje)
    val competicaoPorId = competicoes.associateBy { it.id }

    val itens = filtrados
        .groupBy { jogo -> jogo.competicaoId?.let { "id:$it" } ?: "txt:${jogo.competicao.orEmpty()}" }
        .map { (_, jogosDoGrupo) ->
            val primeiro = jogosDoGrupo.first()
            val competicao = primeiro.competicaoId?.let { competicaoPorId[it] }
            ItemHistoricoRecebimento(
                competicaoId = competicao?.id,
                nome = competicao?.nome ?: primeiro.competicao?.takeIf(String::isNotBlank) ?: "Sem competição vinculada",
                cidade = competicao?.cidade ?: primeiro.cidade,
                modalidade = competicao?.modalidade ?: primeiro.modalidade,
                encerrada = competicao?.encerrada ?: false,
                valorRecebidoCentavos = jogosDoGrupo.sumOf { it.valorCentavos },
                quantidadeJogos = jogosDoGrupo.size,
                ultimoPagamento = jogosDoGrupo.mapNotNull { it.dataRecebimento }.maxOrNull(),
                porFuncao = jogosDoGrupo.contarPorFuncao(),
            )
        }

    return when (filtro.ordenacao) {
        OrdenacaoRecebimento.MAIS_RECENTE -> itens.sortedWith(compareByDescending<ItemHistoricoRecebimento> { it.ultimoPagamento })
        OrdenacaoRecebimento.MAIS_ANTIGO -> itens.sortedWith(compareBy<ItemHistoricoRecebimento> { it.ultimoPagamento })
        OrdenacaoRecebimento.MAIOR_VALOR -> itens.sortedByDescending { it.valorRecebidoCentavos }
        OrdenacaoRecebimento.MENOR_VALOR -> itens.sortedBy { it.valorRecebidoCentavos }
        OrdenacaoRecebimento.COMPETICAO_AZ -> itens.sortedBy { it.nome.lowercase() }
    }
}
