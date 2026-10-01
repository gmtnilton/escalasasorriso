package com.meusjogos.arbitragem.core.model

import java.time.Instant

/**
 * Status de uma competição (VERSÃO 1.2). Toda competição nasce
 * [EM_ANDAMENTO]; o encerramento é sempre uma ação explícita do usuário, e
 * pode ser desfeita (reabrir).
 */
enum class StatusCompeticao {
    EM_ANDAMENTO,
    ENCERRADA,
    ;

    companion object {
        val PADRAO = EM_ANDAMENTO
    }
}

/**
 * Competição como cadastro central (REGRA 5 da V1.2): identificada de forma
 * única por [id], e — para fins de deduplicação — pela combinação
 * normalizada de [nome] + [cidade] + [modalidade] (ver
 * [chaveNormalizadaCompeticao]). Nunca guarda totais/contagens: eles são
 * sempre recalculados a partir dos jogos vinculados (mesmo princípio de
 * Totais.kt), para nunca ficarem dessincronizados.
 */
data class Competicao(
    val id: Long = 0L,
    val nome: String,
    val cidade: String? = null,
    val modalidade: String? = null,
    val status: StatusCompeticao = StatusCompeticao.PADRAO,
    val dataCriacao: Instant = Instant.now(),
    val dataEncerramento: Instant? = null,
) {
    val encerrada: Boolean get() = status == StatusCompeticao.ENCERRADA

    /** "Copa Municipal 2026 — Sorriso • Futebol", omitindo cidade/modalidade quando vazios. */
    val subtitulo: String?
        get() = listOfNotNull(cidade?.takeIf(String::isNotBlank), modalidade?.takeIf(String::isNotBlank))
            .joinToString(" • ")
            .takeIf(String::isNotBlank)
}

/** Resumo financeiro/numérico de uma competição — sempre recalculado a partir dos jogos vinculados a ela. */
data class ResumoCompeticao(
    val totalJogos: Int,
    val jogosRecebidos: Int,
    val jogosPendentes: Int,
    val valorTotalCentavos: Long,
    val valorRecebidoCentavos: Long,
    val valorPendenteCentavos: Long,
) {
    companion object {
        val VAZIO = ResumoCompeticao(0, 0, 0, 0, 0, 0)
    }
}
