package com.meusjogos.arbitragem.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JogoDao {

    /**
     * Observa todos os jogos, do mais recente para o mais antigo (ordenação
     * padrão da tela "Meus Jogos" — REGRA 7). Filtros, pesquisa e resumos
     * são calculados em memória a partir desta lista (ver módulo :core),
     * o que é suficiente para o volume de jogos de um árbitro.
     */
    @Query("SELECT * FROM jogos ORDER BY data DESC, id DESC")
    fun observarTodos(): Flow<List<JogoEntity>>

    @Query("SELECT * FROM jogos WHERE id = :id")
    suspend fun buscarPorId(id: Long): JogoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(jogo: JogoEntity): Long

    @Update
    suspend fun atualizar(jogo: JogoEntity)

    @Delete
    suspend fun excluir(jogo: JogoEntity)

    /** Usado só na restauração de backup: substitui a base inteira. */
    @Query("DELETE FROM jogos")
    suspend fun excluirTodos()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserirTodos(jogos: List<JogoEntity>): List<Long>
}
