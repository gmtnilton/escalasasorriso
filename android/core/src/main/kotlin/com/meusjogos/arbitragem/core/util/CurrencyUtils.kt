package com.meusjogos.arbitragem.core.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utilitários de moeda. Valores são sempre manipulados como centavos
 * (Long) para evitar erros de arredondamento de ponto flutuante nas somas
 * financeiras — só viram texto/BigDecimal na hora de exibir ou exportar.
 */
object CurrencyUtils {

    /** 125000L -> "R$ 1.250,00" */
    fun formatar(centavos: Long): String {
        val negativo = centavos < 0
        val valorAbs = kotlin.math.abs(centavos)
        val reais = valorAbs / 100
        val centavosParte = valorAbs % 100
        val sinal = if (negativo) "-" else ""
        return "${sinal}R$ ${formatarMilhar(reais)},${centavosParte.toString().padStart(2, '0')}"
    }

    private fun formatarMilhar(valor: Long): String {
        val texto = valor.toString()
        val builder = StringBuilder()
        for ((indice, caractere) in texto.reversed().withIndex()) {
            if (indice != 0 && indice % 3 == 0) builder.append('.')
            builder.append(caractere)
        }
        return builder.reverse().toString()
    }

    /**
     * Converte o texto bruto de um campo de valor com máscara "R$ 0,00" para
     * centavos: cada dígito digitado entra pela direita, como em qualquer
     * teclado numérico de valor monetário (ex.: digitar "4","0","0" resulta
     * em R$ 4,00). Entradas vazias ou só com zeros retornam 0.
     */
    fun paraCentavosMascarado(textoDigitado: String): Long {
        val somenteDigitos = textoDigitado.filter { it.isDigit() }.trimStart('0')
        if (somenteDigitos.isEmpty()) return 0L
        return somenteDigitos.toLongOrNull()?.coerceIn(0, Long.MAX_VALUE / 2) ?: 0L
    }

    /**
     * Interpreta um valor monetário já formatado (ex.: "R$ 1.250,00",
     * "1250,50", "1250.50") — usado ao importar CSV/backup. Retorna null se
     * o texto não puder ser interpretado como um valor monetário válido.
     */
    fun paraCentavosDeTextoFormatado(texto: String): Long? {
        val limpo = texto.trim().removePrefix("R$").trim()
        if (limpo.isEmpty()) return null
        val normalizado = if (limpo.contains(',')) {
            limpo.replace(".", "").replace(",", ".")
        } else {
            limpo
        }
        val valor = normalizado.toBigDecimalOrNull() ?: return null
        return valor.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? = try {
        BigDecimal(this)
    } catch (e: NumberFormatException) {
        null
    }

    /** Centavos -> BigDecimal em reais (2 casas), útil para exportações (CSV/backup). */
    fun paraBigDecimal(centavos: Long): BigDecimal =
        BigDecimal(centavos).divide(BigDecimal(100)).setScale(2, RoundingMode.UNNECESSARY)
}
