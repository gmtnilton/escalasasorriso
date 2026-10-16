package com.meusjogos.arbitragem.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Linha da tabela "competicoes" (VERSÃO 1.2) — cadastro central de
 * competições. Nunca guarda totais/contagens (ver [com.meusjogos.arbitragem.core.model.ResumoCompeticao]):
 * eles são sempre recalculados a partir dos jogos vinculados por [JogoEntity.competicaoId].
 */
@Entity(tableName = "competicoes")
data class CompeticaoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "cidade")
    val cidade: String?,

    @ColumnInfo(name = "modalidade")
    val modalidade: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "data_criacao")
    val dataCriacao: Long,

    @ColumnInfo(name = "data_encerramento")
    val dataEncerramento: Long?,
)
