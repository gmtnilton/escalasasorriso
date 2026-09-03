package com.meusjogos.arbitragem.data.repository

import com.meusjogos.arbitragem.core.logic.duplicarJogo
import com.meusjogos.arbitragem.core.logic.marcarComoRecebido as marcarComoRecebidoLogica
import com.meusjogos.arbitragem.core.logic.desfazerRecebimento as desfazerRecebimentoLogica
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.data.local.JogoDao
import com.meusjogos.arbitragem.data.local.toDomain
import com.meusjogos.arbitragem.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Fonte única de verdade dos jogos: expõe a lista completa como [Flow]
 * (a UI observa e recalcula os totais automaticamente a cada mudança) e
 * concentra todas as operações de escrita (cadastrar, editar, excluir,
 * duplicar, marcar/desmarcar recebido), sempre delegando as regras de
 * negócio (o que muda em cada ação) para o módulo :core.
 */
class JogoRepository(private val dao: JogoDao) {

    fun observarJogos(): Flow<List<Jogo>> = dao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    suspend fun buscarPorId(id: Long): Jogo? = dao.buscarPorId(id)?.toDomain()

    /** Cadastra (id == 0) ou atualiza (id != 0) um jogo; retorna o id final. */
    suspend fun salvar(jogo: Jogo): Long {
        val agora = java.time.Instant.now()
        return if (jogo.id == 0L) {
            dao.inserir(jogo.copy(dataCriacao = agora, dataAtualizacao = agora).toEntity())
        } else {
            dao.atualizar(jogo.copy(dataAtualizacao = agora).toEntity())
            jogo.id
        }
    }

    suspend fun excluir(jogo: Jogo) {
        dao.excluir(jogo.toEntity())
    }

    /**
     * Cria a cópia de [original] (REGRA 9/10 — "Duplicar jogo") e já
     * insere no banco, retornando o novo jogo (com o id gerado) para a UI
     * abrir imediatamente a tela de edição.
     */
    suspend fun duplicar(original: Jogo): Jogo {
        val copia = duplicarJogo(original)
        val novoId = dao.inserir(copia.toEntity())
        return copia.copy(id = novoId)
    }

    suspend fun marcarComoRecebido(jogo: Jogo, dataRecebimento: LocalDate = LocalDate.now()) {
        dao.atualizar(marcarComoRecebidoLogica(jogo, dataRecebimento).toEntity())
    }

    suspend fun desfazerRecebimento(jogo: Jogo) {
        dao.atualizar(desfazerRecebimentoLogica(jogo).toEntity())
    }

    /** Lista completa "de uma vez", usada por backup/exportação. */
    suspend fun listarTudoUmaVez(): List<Jogo> = dao.observarTodos().first().map { it.toDomain() }

    /** Substitui toda a base de dados pelos jogos restaurados de um backup. */
    suspend fun restaurarBackup(jogos: List<Jogo>) {
        dao.excluirTodos()
        dao.inserirTodos(jogos.map { it.toEntity().copy(id = 0L) })
    }
}
