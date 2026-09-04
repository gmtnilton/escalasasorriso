package com.meusjogos.arbitragem.data.preferences

import android.content.Context
import com.meusjogos.arbitragem.core.logic.codigoAtivacaoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Ativação local do app (código "Apito Ativo") — 100% offline, sem
 * servidor: libera o cadastro de jogos depois que o código correto é
 * informado uma vez no aparelho. Persistida em SharedPreferences, como
 * TemaPreferences — some com a desinstalação do app (nova instalação =
 * nova ativação) e sobrevive normalmente a uma atualização instalada por
 * cima da versão anterior.
 */
class AtivacaoPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE)

    private val _ativado = MutableStateFlow(prefs.getBoolean(CHAVE_ATIVADO, false))
    val ativado: StateFlow<Boolean> = _ativado

    /** Confere o código digitado e, se correto, ativa o app (retorna se ativou). */
    fun tentarAtivar(codigoDigitado: String): Boolean {
        val correto = codigoAtivacaoValido(codigoDigitado)
        if (correto) {
            prefs.edit().putBoolean(CHAVE_ATIVADO, true).apply()
            _ativado.value = true
        }
        return correto
    }

    companion object {
        private const val ARQUIVO_PREFERENCIAS = "preferencias_ativacao"
        private const val CHAVE_ATIVADO = "app_ativado"
    }
}
