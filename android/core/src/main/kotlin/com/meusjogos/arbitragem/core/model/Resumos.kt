package com.meusjogos.arbitragem.core.model

/** Resumo de um período (mês ou intervalo) — usado no dashboard e na tela de resumo. */
data class ResumoPeriodo(
    val totalJogos: Int,
    val totalRecebidoCentavos: Long,
    val totalAReceberCentavos: Long,
) {
    val totalGeralCentavos: Long get() = totalRecebidoCentavos + totalAReceberCentavos
}

/** Resumo anual, com a média por jogo já calculada. */
data class ResumoAnual(
    val ano: Int,
    val totalJogos: Int,
    val totalRecebidoCentavos: Long,
    val totalAReceberCentavos: Long,
) {
    val totalGeralCentavos: Long get() = totalRecebidoCentavos + totalAReceberCentavos
    val mediaPorJogoCentavos: Long get() = if (totalJogos == 0) 0L else totalGeralCentavos / totalJogos
}

/** Estatísticas gerais, considerando toda a base de jogos (ou um subconjunto filtrado). */
data class Estatisticas(
    val totalJogos: Int,
    val totalRecebidoCentavos: Long,
    val totalAReceberCentavos: Long,
    val jogosRecebidos: Int,
    val jogosPendentes: Int,
    val maiorValorCentavos: Long,
) {
    val totalGeralCentavos: Long get() = totalRecebidoCentavos + totalAReceberCentavos
    val mediaPorJogoCentavos: Long get() = if (totalJogos == 0) 0L else totalGeralCentavos / totalJogos

    companion object {
        val VAZIO = Estatisticas(0, 0, 0, 0, 0, 0)
    }
}

/** Um ponto de dados mensal, usado nos gráficos simples de barras (jogos/valores por mês). */
data class PontoMensal(
    val ano: Int,
    val mes: Int,
    val totalJogos: Int,
    val totalRecebidoCentavos: Long,
    val totalAReceberCentavos: Long,
) {
    val totalGeralCentavos: Long get() = totalRecebidoCentavos + totalAReceberCentavos
}
