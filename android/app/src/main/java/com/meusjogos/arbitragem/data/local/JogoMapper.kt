package com.meusjogos.arbitragem.data.local

import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun JogoEntity.toDomain(): Jogo = Jogo(
    id = id,
    data = LocalDate.ofEpochDay(data),
    horario = horario?.let { LocalTime.parse(it) },
    competicao = competicao,
    modalidade = modalidade,
    categoria = categoria,
    equipeMandante = equipeMandante,
    equipeVisitante = equipeVisitante,
    cidade = cidade,
    estadio = estadio,
    funcao = funcao,
    valorCentavos = valorCentavos,
    statusPagamento = StatusPagamento.valueOf(statusPagamento),
    dataRecebimento = dataRecebimento?.let { LocalDate.ofEpochDay(it) },
    observacoes = observacoes,
    dataCriacao = Instant.ofEpochMilli(dataCriacao),
    dataAtualizacao = Instant.ofEpochMilli(dataAtualizacao),
)

fun Jogo.toEntity(): JogoEntity = JogoEntity(
    id = id,
    data = data.toEpochDay(),
    horario = horario?.toString(),
    competicao = competicao?.takeIf(String::isNotBlank),
    modalidade = modalidade?.takeIf(String::isNotBlank),
    categoria = categoria?.takeIf(String::isNotBlank),
    equipeMandante = equipeMandante?.takeIf(String::isNotBlank),
    equipeVisitante = equipeVisitante?.takeIf(String::isNotBlank),
    cidade = cidade?.takeIf(String::isNotBlank),
    estadio = estadio?.takeIf(String::isNotBlank),
    funcao = funcao?.takeIf(String::isNotBlank),
    valorCentavos = valorCentavos,
    statusPagamento = statusPagamento.name,
    dataRecebimento = dataRecebimento?.toEpochDay(),
    observacoes = observacoes?.takeIf(String::isNotBlank),
    dataCriacao = dataCriacao.toEpochMilli(),
    dataAtualizacao = dataAtualizacao.toEpochMilli(),
)
