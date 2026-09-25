package com.meusjogos.arbitragem.data.recibo

import com.meusjogos.arbitragem.core.model.Recibo
import com.meusjogos.arbitragem.data.local.ReciboDao
import com.meusjogos.arbitragem.data.local.toDomain
import com.meusjogos.arbitragem.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Fonte única de verdade dos recibos de pagamento — cada jogo tem no máximo
 * um recibo (índice único em jogo_id no banco). [salvar] faz upsert por
 * jogoId: se já existe um recibo para o jogo, atualiza o MESMO registro
 * (mantendo id e data de criação originais); nunca cria um segundo recibo
 * para o mesmo jogo.
 */
class ReciboRepository(private val dao: ReciboDao) {

    suspend fun buscarPorJogoId(jogoId: Long): Recibo? = dao.buscarPorJogoId(jogoId)?.toDomain()

    fun observarPorJogoId(jogoId: Long): Flow<Recibo?> = dao.observarPorJogoId(jogoId).map { it?.toDomain() }

    suspend fun salvar(recibo: Recibo): Recibo {
        val existente = dao.buscarPorJogoId(recibo.jogoId)
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
