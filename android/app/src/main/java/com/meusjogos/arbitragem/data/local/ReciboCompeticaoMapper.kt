package com.meusjogos.arbitragem.data.local

import com.meusjogos.arbitragem.core.model.ReciboCompeticao
import java.time.Instant
import java.time.LocalDate

fun ReciboCompeticaoEntity.toDomain(): ReciboCompeticao = ReciboCompeticao(
    id = id,
    competicaoId = competicaoId,
    pagadorNome = pagadorNome,
    pagadorDocumento = pagadorDocumento,
    recebedorNome = recebedorNome,
    recebedorDocumento = recebedorDocumento,
    valorCentavos = valorCentavos,
    quantidadeJogos = quantidadeJogos,
    dataPagamento = LocalDate.ofEpochDay(dataPagamento),
    descricao = descricao,
    criadoEm = Instant.ofEpochMilli(criadoEm),
)

fun ReciboCompeticao.toEntity(): ReciboCompeticaoEntity = ReciboCompeticaoEntity(
    id = id,
    competicaoId = competicaoId,
    pagadorNome = pagadorNome,
    pagadorDocumento = pagadorDocumento,
    recebedorNome = recebedorNome,
    recebedorDocumento = recebedorDocumento,
    valorCentavos = valorCentavos,
    quantidadeJogos = quantidadeJogos,
    dataPagamento = dataPagamento.toEpochDay(),
    descricao = descricao?.takeIf(String::isNotBlank),
    criadoEm = criadoEm.toEpochMilli(),
)
