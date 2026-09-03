package com.meusjogos.arbitragem.core.model

import java.time.LocalDate

/** Filtro de status de pagamento aplicado à lista "Meus Jogos". */
enum class FiltroStatus {
    TODOS,
    A_RECEBER,
    RECEBIDOS,
}

/** Filtro de período aplicado à lista "Meus Jogos". */
sealed class FiltroPeriodo {
    data object Todos : FiltroPeriodo()
    data object Hoje : FiltroPeriodo()
    data object EstaSemana : FiltroPeriodo()
    data object EsteMes : FiltroPeriodo()
    data object MesAnterior : FiltroPeriodo()
    data object EsteAno : FiltroPeriodo()
    data class Personalizado(val inicio: LocalDate, val fim: LocalDate) : FiltroPeriodo()
}

/**
 * Conjunto completo de filtros + termo de pesquisa aplicáveis à lista de
 * jogos. A função que aplica este filtro (filtrarEPesquisar, em
 * FiltroLogica.kt) é regra de negócio pura, testável sem Android/Room.
 */
data class FiltroJogos(
    val status: FiltroStatus = FiltroStatus.TODOS,
    val periodo: FiltroPeriodo = FiltroPeriodo.Todos,
    val competicao: String? = null,
    val funcao: String? = null,
    val pesquisa: String = "",
) {
    val ativo: Boolean
        get() = status != FiltroStatus.TODOS ||
            periodo != FiltroPeriodo.Todos ||
            !competicao.isNullOrBlank() ||
            !funcao.isNullOrBlank()
}
