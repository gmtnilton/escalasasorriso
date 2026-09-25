package com.meusjogos.arbitragem.core.model

/**
 * Status de pagamento de um jogo. Todo jogo novo nasce em [A_RECEBER];
 * a mudança para [RECEBIDO] é sempre uma ação explícita do usuário.
 */
enum class StatusPagamento {
    A_RECEBER,
    RECEBIDO;

    companion object {
        val PADRAO = A_RECEBER
    }
}
