package com.meusjogos.arbitragem.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Linha da tabela "jogos" no Room/SQLite. Segue a estrutura sugerida na
 * seção 22 do briefing: id, data, horario, competicao, categoria,
 * equipeMandante, equipeVisitante, local, funcao, valor, statusPagamento,
 * dataRecebimento, observacoes, dataCriacao, dataAtualizacao.
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

    @ColumnInfo(name = "categoria")
    val categoria: String?,

    @ColumnInfo(name = "equipe_mandante")
    val equipeMandante: String?,

    @ColumnInfo(name = "equipe_visitante")
    val equipeVisitante: String?,

    @ColumnInfo(name = "local")
    val local: String?,

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
