package com.meusjogos.arbitragem.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Representa um jogo apitado (ou a apitar), o registro central do app.
 *
 * Único obrigatório de fato para o usuário no cadastro: [data] e
 * [valorCentavos]. Todos os demais campos são opcionais e podem ficar nulos
 * ou em branco — completá-los depois, via edição, é o fluxo esperado.
 *
 * Valores monetários são guardados em centavos ([valorCentavos]) para que
 * somas e comparações sejam sempre exatas, sem os erros de arredondamento
 * de ponto flutuante que um Double introduziria nos totais financeiros.
 */
data class Jogo(
    val id: Long = 0L,
    val data: LocalDate,
    val horario: LocalTime? = null,
    val competicao: String? = null,
    /** Vínculo com o cadastro central de competições (VERSÃO 1.2) — null para jogos sem competição
     * informada. Quando presente, [competicao] é mantido como uma cópia sincronizada do nome
     * canônico da competição vinculada, para que todo o código que já lê [competicao] como texto
     * livre (filtros, CSV, backup, recibo) continue funcionando sem nenhuma alteração. */
    val competicaoId: Long? = null,
    val modalidade: String? = null,
    val categoria: String? = null,
    val equipeMandante: String? = null,
    val equipeVisitante: String? = null,
    val cidade: String? = null,
    val estadio: String? = null,
    val funcao: String? = null,
    val valorCentavos: Long,
    val statusPagamento: StatusPagamento = StatusPagamento.PADRAO,
    val dataRecebimento: LocalDate? = null,
    val observacoes: String? = null,
    val dataCriacao: Instant = Instant.now(),
    val dataAtualizacao: Instant = Instant.now(),
) {
    val recebido: Boolean get() = statusPagamento == StatusPagamento.RECEBIDO

    val temEquipes: Boolean
        get() = !equipeMandante.isNullOrBlank() && !equipeVisitante.isNullOrBlank()

    /** "Sorriso FC x Sinop FC", ou null se as equipes não foram preenchidas. */
    val confronto: String?
        get() = if (temEquipes) "$equipeMandante x $equipeVisitante" else null
}
