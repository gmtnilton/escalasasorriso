package com.meusjogos.arbitragem.core.logic

/**
 * Código de ativação local do app — proteção simples e 100% offline (sem
 * servidor): o cadastro de jogos só é liberado depois que esse código é
 * informado uma vez no aparelho. A comparação ignora maiúsculas/minúsculas
 * e espaços extras no início/fim do que foi digitado.
 */
private const val CODIGO_ATIVACAO = "Apito Ativo"

fun codigoAtivacaoValido(codigoDigitado: String): Boolean =
    codigoDigitado.trim().equals(CODIGO_ATIVACAO, ignoreCase = true)
