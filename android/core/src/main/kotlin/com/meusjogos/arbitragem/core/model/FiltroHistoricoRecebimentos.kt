package com.meusjogos.arbitragem.core.model

/** Ordenação do histórico de valores recebidos (VERSÃO 1.4, seção 5). */
enum class OrdenacaoRecebimento {
    MAIS_RECENTE,
    MAIS_ANTIGO,
    MAIOR_VALOR,
    MENOR_VALOR,
    COMPETICAO_AZ,
}

/**
 * Filtros da tela "Histórico de valores recebidos" (VERSÃO 1.4, seção 5) — ao contrário do
 * [FiltroJogos] usado em "Meus Jogos"/"Recebimento" (cujo período olha a data do JOGO), aqui o
 * [periodo] é aplicado sobre a data do PAGAMENTO ([com.meusjogos.arbitragem.core.model.Jogo.dataRecebimento]),
 * conforme pedido explicitamente na seção 5.
 */
data class FiltroHistoricoRecebimentos(
    val periodo: FiltroPeriodo = FiltroPeriodo.Todos,
    val competicao: String? = null,
    val cidade: String? = null,
    val modalidade: String? = null,
    val pesquisa: String = "",
    val ordenacao: OrdenacaoRecebimento = OrdenacaoRecebimento.MAIS_RECENTE,
) {
    val ativo: Boolean
        get() = periodo != FiltroPeriodo.Todos ||
            !competicao.isNullOrBlank() ||
            !cidade.isNullOrBlank() ||
            !modalidade.isNullOrBlank() ||
            pesquisa.isNotBlank()
}
