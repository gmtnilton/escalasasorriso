package com.meusjogos.arbitragem.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Linha da tabela "recibos" — um recibo de pagamento por jogo (índice único
 * em [jogoId], ver [MIGRATION_3_4]), gerado só depois que o jogo é marcado
 * como recebido. Regenerar o recibo de um jogo reaproveita esta mesma linha.
 */
@Entity(tableName = "recibos", indices = [Index(value = ["jogo_id"], unique = true)])
data class ReciboEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "jogo_id")
    val jogoId: Long,

    @ColumnInfo(name = "pagador_nome")
    val pagadorNome: String,

    @ColumnInfo(name = "pagador_documento")
    val pagadorDocumento: String,

    @ColumnInfo(name = "recebedor_nome")
    val recebedorNome: String,

    @ColumnInfo(name = "recebedor_documento")
    val recebedorDocumento: String,

    @ColumnInfo(name = "valor_centavos")
    val valorCentavos: Long,

    @ColumnInfo(name = "data_pagamento")
    val dataPagamento: Long,

    @ColumnInfo(name = "descricao")
    val descricao: String?,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long,
)
