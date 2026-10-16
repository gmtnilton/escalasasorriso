package com.meusjogos.arbitragem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReciboCompeticaoDao {

    @Query("SELECT * FROM recibos_competicao WHERE competicao_id = :competicaoId LIMIT 1")
    suspend fun buscarPorCompeticaoId(competicaoId: Long): ReciboCompeticaoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(recibo: ReciboCompeticaoEntity): Long

    @Update
    suspend fun atualizar(recibo: ReciboCompeticaoEntity)
}
