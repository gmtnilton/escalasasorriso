package com.meusjogos.arbitragem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReciboDao {

    @Query("SELECT * FROM recibos WHERE jogo_id = :jogoId LIMIT 1")
    suspend fun buscarPorJogoId(jogoId: Long): ReciboEntity?

    /** Observado pelo detalhe do jogo, só para saber se já existe recibo gerado. */
    @Query("SELECT * FROM recibos WHERE jogo_id = :jogoId LIMIT 1")
    fun observarPorJogoId(jogoId: Long): Flow<ReciboEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(recibo: ReciboEntity): Long

    @Update
    suspend fun atualizar(recibo: ReciboEntity)
}
