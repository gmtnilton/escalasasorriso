package com.meusjogos.arbitragem.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Utilitários de data/hora no formato brasileiro (dd/MM/aaaa). */
object DateUtils {
    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoDataCurta = DateTimeFormatter.ofPattern("dd/MM/yy")
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

    private val nomesMeses = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
    )

    fun formatarData(data: LocalDate): String = data.format(formatoData)

    fun formatarDataCurta(data: LocalDate): String = data.format(formatoDataCurta)

    fun formatarHora(hora: java.time.LocalTime): String = hora.format(formatoHora)

    /** "SETEMBRO/2026" — usado nos cabeçalhos de resumo mensal. */
    fun nomeMesAno(ano: Int, mes: Int): String = "${nomesMeses[mes - 1].uppercase()}/$ano"

    fun nomeMes(mes: Int): String = nomesMeses[mes - 1]

    /** Faz o parse de "dd/MM/aaaa" digitado pelo usuário; null se inválido. */
    fun parseData(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), formatoData)
    } catch (e: DateTimeParseException) {
        null
    }

    /** Aplica a máscara dd/MM/aaaa a uma sequência de dígitos digitados (ex.: "03092026" -> "03/09/2026"). */
    fun aplicarMascaraData(digitos: String): String {
        val d = digitos.filter { it.isDigit() }.take(8)
        val builder = StringBuilder()
        for (i in d.indices) {
            if (i == 2 || i == 4) builder.append('/')
            builder.append(d[i])
        }
        return builder.toString()
    }

    /** Aplica a máscara HH:mm a uma sequência de dígitos digitados (ex.: "1430" -> "14:30"). */
    fun aplicarMascaraHora(digitos: String): String {
        val h = digitos.filter { it.isDigit() }.take(4)
        val builder = StringBuilder()
        for (i in h.indices) {
            if (i == 2) builder.append(':')
            builder.append(h[i])
        }
        return builder.toString()
    }

    /** Faz o parse de "HH:mm" digitado pelo usuário; null se inválido. */
    fun parseHora(texto: String): java.time.LocalTime? = try {
        java.time.LocalTime.parse(texto.trim(), formatoHora)
    } catch (e: DateTimeParseException) {
        null
    }
}
