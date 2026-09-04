package com.meusjogos.arbitragem.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Linha da tabela "jogos" no Room/SQLite. Baseada na estrutura sugerida na
 * seção 22 do briefing, com o antigo campo "local" desmembrado em
 * [cidade] (predefinida + editável) e [estadio] (nome do estádio/ginásio) —
 * ver [MIGRATION_1_2].
 *
 * Datas são gravadas como "epoch day" (Long) e horários como "HH:mm"
 * (String), sempre anuláveis exceto [data]. O valor é guardado em
 * centavos ([valorCentavos]) para garantir somas financeiras exatas.
 */
@Entity(tableName = "jogos")
data class JogoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "data")
    val data: Long,

    @ColumnInfo(name = "horario")
    val horario: String?,

    @ColumnInfo(name = "competicao")
    val competicao: String?,

    @ColumnInfo(name = "modalidade")
    val modalidade: String?,

    @ColumnInfo(name = "categoria")
    val categoria: String?,

    @ColumnInfo(name = "equipe_mandante")
    val equipeMandante: String?,

    @ColumnInfo(name = "equipe_visitante")
    val equipeVisitante: String?,

    @ColumnInfo(name = "cidade")
    val cidade: String?,

    @ColumnInfo(name = "estadio")
    val estadio: String?,

    @ColumnInfo(name = "funcao")
    val funcao: String?,

    @ColumnInfo(name = "valor_centavos")
    val valorCentavos: Long,

    @ColumnInfo(name = "status_pagamento")
    val statusPagamento: String,

    @ColumnInfo(name = "data_recebimento")
    val dataRecebimento: Long?,

    @ColumnInfo(name = "observacoes")
    val observacoes: String?,

    @ColumnInfo(name = "data_criacao")
    val dataCriacao: Long,

    @ColumnInfo(name = "data_atualizacao")
    val dataAtualizacao: Long,
)
