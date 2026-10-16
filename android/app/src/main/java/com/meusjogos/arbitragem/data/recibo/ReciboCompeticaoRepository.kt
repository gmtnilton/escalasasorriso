package com.meusjogos.arbitragem.data.recibo

import com.meusjogos.arbitragem.core.model.ReciboCompeticao
import com.meusjogos.arbitragem.data.local.ReciboCompeticaoDao
import com.meusjogos.arbitragem.data.local.toDomain
import com.meusjogos.arbitragem.data.local.toEntity
import java.time.Instant

/**
 * Fonte única de verdade dos recibos de RECEBIMENTO TOTAL/PARCIAL de
 * competição (VERSÃO 1.2, REGRA 12) — cada competição tem no máximo um
 * recibo (índice único em competicao_id). [salvar] faz upsert por
 * competicaoId: se já existe um recibo para a competição, atualiza o MESMO
 * registro (mantendo id e data de criação originais); nunca cria um segundo
 * recibo para a mesma competição.
 */
class ReciboCompeticaoRepository(private val dao: ReciboCompeticaoDao) {

    suspend fun buscarPorCompeticaoId(competicaoId: Long): ReciboCompeticao? = dao.buscarPorCompeticaoId(competicaoId)?.toDomain()

    suspend fun salvar(recibo: ReciboCompeticao): ReciboCompeticao {
        val existente = dao.buscarPorCompeticaoId(recibo.competicaoId)
        return if (existente != null) {
            val atualizado = recibo.copy(id = existente.id, criadoEm = Instant.ofEpochMilli(existente.criadoEm))
            dao.atualizar(atualizado.toEntity())
            atualizado
        } else {
            val novoId = dao.inserir(recibo.copy(id = 0L).toEntity())
            recibo.copy(id = novoId)
        }
    }
}
