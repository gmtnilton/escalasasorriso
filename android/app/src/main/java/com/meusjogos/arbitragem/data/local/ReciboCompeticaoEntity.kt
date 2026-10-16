package com.meusjogos.arbitragem.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Linha da tabela "recibos_competicao" (VERSÃO 1.2) — recibo do recebimento
 * total/parcial de uma competição (índice único em [competicaoId], mesmo
 * padrão de [ReciboEntity]/[MIGRATION_3_4]). Regenerar o recibo de uma
 * competição reaproveita esta mesma linha.
 */
@Entity(tableName = "recibos_competicao", indices = [Index(value = ["competicao_id"], unique = true)])
data class ReciboCompeticaoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "competicao_id")
    val competicaoId: Long,

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

    @ColumnInfo(name = "quantidade_jogos")
    val quantidadeJogos: Int,

    @ColumnInfo(name = "data_pagamento")
    val dataPagamento: Long,

    @ColumnInfo(name = "descricao")
    val descricao: String?,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long,
)
