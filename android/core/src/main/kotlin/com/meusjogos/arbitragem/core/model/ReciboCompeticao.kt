package com.meusjogos.arbitragem.core.model

import java.time.Instant
import java.time.LocalDate

/**
 * Recibo de pagamento do RECEBIMENTO TOTAL (ou parcial) de uma competição
 * (VERSÃO 1.2, REGRA 12) — sempre vinculado à competição que o originou por
 * [competicaoId] (upsert por competicaoId no banco, mesmo padrão de
 * [Recibo]/ReciboRepository.salvar): regenerar o recibo de uma mesma
 * competição reaproveita este mesmo registro, nunca duplica um pagamento.
 */
data class ReciboCompeticao(
    val id: Long = 0L,
    val competicaoId: Long,
    val pagadorNome: String,
    val pagadorDocumento: String,
    val recebedorNome: String,
    val recebedorDocumento: String,
    val valorCentavos: Long,
    val quantidadeJogos: Int,
    val dataPagamento: LocalDate,
    val descricao: String? = null,
    val criadoEm: Instant = Instant.now(),
) {
    /** Número único do recibo, derivado do id gerado pelo banco — ex.: "Nº C-000123". */
    val numeroFormatado: String get() = "Nº C-${id.toString().padStart(6, '0')}"
}
