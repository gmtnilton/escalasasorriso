package com.meusjogos.arbitragem.data.local

import com.meusjogos.arbitragem.core.model.Recibo
import java.time.Instant
import java.time.LocalDate

fun ReciboEntity.toDomain(): Recibo = Recibo(
    id = id,
    jogoId = jogoId,
    pagadorNome = pagadorNome,
    pagadorDocumento = pagadorDocumento,
    recebedorNome = recebedorNome,
    recebedorDocumento = recebedorDocumento,
    valorCentavos = valorCentavos,
    dataPagamento = LocalDate.ofEpochDay(dataPagamento),
    descricao = descricao,
    criadoEm = Instant.ofEpochMilli(criadoEm),
)

fun Recibo.toEntity(): ReciboEntity = ReciboEntity(
    id = id,
    jogoId = jogoId,
    pagadorNome = pagadorNome,
    pagadorDocumento = pagadorDocumento,
    recebedorNome = recebedorNome,
    recebedorDocumento = recebedorDocumento,
    valorCentavos = valorCentavos,
    dataPagamento = dataPagamento.toEpochDay(),
    descricao = descricao?.takeIf(String::isNotBlank),
    criadoEm = criadoEm.toEpochMilli(),
)
