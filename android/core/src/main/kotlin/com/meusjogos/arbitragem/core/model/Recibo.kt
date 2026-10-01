package com.meusjogos.arbitragem.core.model

import java.time.Instant
import java.time.LocalDate

/**
 * Recibo de pagamento de um jogo já recebido — sempre vinculado ao jogo que
 * o originou por [jogoId] (nunca cria outro jogo). Regenerar o recibo de um
 * mesmo jogo reaproveita este mesmo registro (upsert por jogoId no banco,
 * ver ReciboRepository.salvar), nunca duplica um pagamento.
 */
data class Recibo(
    val id: Long = 0L,
    val jogoId: Long,
    val pagadorNome: String,
    val pagadorDocumento: String,
    val recebedorNome: String,
    val recebedorDocumento: String,
    val valorCentavos: Long,
    val dataPagamento: LocalDate,
    val descricao: String? = null,
    val criadoEm: Instant = Instant.now(),
) {
    /** Número único do recibo, derivado do id gerado pelo banco — ex.: "Nº 000123". */
    val numeroFormatado: String get() = "Nº ${id.toString().padStart(6, '0')}"
}
