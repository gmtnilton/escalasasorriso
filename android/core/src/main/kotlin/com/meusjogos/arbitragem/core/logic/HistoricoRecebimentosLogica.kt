package com.meusjogos.arbitragem.core.logic

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
