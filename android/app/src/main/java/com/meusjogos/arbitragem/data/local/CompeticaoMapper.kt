package com.meusjogos.arbitragem.data.local

import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.StatusCompeticao
import java.time.Instant

fun CompeticaoEntity.toDomain(): Competicao = Competicao(
    id = id,
    nome = nome,
    cidade = cidade,
    modalidade = modalidade,
    status = StatusCompeticao.valueOf(status),
    dataCriacao = Instant.ofEpochMilli(dataCriacao),
    dataEncerramento = dataEncerramento?.let { Instant.ofEpochMilli(it) },
)

fun Competicao.toEntity(): CompeticaoEntity = CompeticaoEntity(
    id = id,
    nome = nome,
    cidade = cidade?.takeIf(String::isNotBlank),
    modalidade = modalidade?.takeIf(String::isNotBlank),
    status = status.name,
    dataCriacao = dataCriacao.toEpochMilli(),
    dataEncerramento = dataEncerramento?.toEpochMilli(),
)
