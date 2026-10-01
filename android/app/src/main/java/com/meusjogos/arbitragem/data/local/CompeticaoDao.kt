package com.meusjogos.arbitragem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompeticaoDao {

    @Query("SELECT * FROM competicoes ORDER BY nome ASC")
    fun observarTodas(): Flow<List<CompeticaoEntity>>

    @Query("SELECT * FROM competicoes WHERE id = :id")
    suspend fun buscarPorId(id: Long): CompeticaoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(competicao: CompeticaoEntity): Long

    @Update
    suspend fun atualizar(competicao: CompeticaoEntity)

    @Query("DELETE FROM competicoes WHERE id = :id")
    suspend fun excluirPorId(id: Long)

    /** Reaponta todos os jogos da competição [deId] para [paraId] — usado ao unificar duas
     * competições equivalentes (edição que gera duplicidade, ou limpeza da migração da V1.2). */
    @Query("UPDATE jogos SET competicao_id = :paraId WHERE competicao_id = :deId")
    suspend fun repontarJogos(deId: Long, paraId: Long)

    /** Sincroniza o nome (texto livre, campo "competicao") dos jogos vinculados com o nome
     * canônico atual da competição — mantém os dois em sincronia após editar/unificar (ver
     * Jogo.competicaoId no :core). Não mexe na cidade/estádio/modalidade já preenchidos em cada
     * jogo individualmente: esses continuam livres para o usuário ajustar por jogo. */
    @Query("UPDATE jogos SET competicao = :nome WHERE competicao_id = :competicaoId")
    suspend fun sincronizarNomeDosJogos(competicaoId: Long, nome: String)
}
