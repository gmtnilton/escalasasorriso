package com.meusjogos.arbitragem.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Preferência de aparência do app — o usuário pode fixar claro/escuro ou seguir o sistema. */
enum class ModoTema { CLARO, ESCURO, SISTEMA }

/**
 * Persiste a preferência de aparência em SharedPreferences — a única
 * configuração do app que não faz parte da base de jogos, por isso fica
 * fora do Room. Lida uma vez na criação e mantida em memória via
 * [StateFlow], já que é alterada raramente (só na tela de Configurações).
 */
class TemaPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(ARQUIVO_PREFERENCIAS, Context.MODE_PRIVATE)

    private val _modo = MutableStateFlow(lerModoSalvo())
    val modo: StateFlow<ModoTema> = _modo

    private fun lerModoSalvo(): ModoTema {
        val nomeSalvo = prefs.getString(CHAVE_MODO, null) ?: return ModoTema.SISTEMA
        return runCatching { ModoTema.valueOf(nomeSalvo) }.getOrDefault(ModoTema.SISTEMA)
    }

    fun definirModo(novoModo: ModoTema) {
        prefs.edit().putString(CHAVE_MODO, novoModo.name).apply()
        _modo.value = novoModo
    }

    companion object {
        private const val ARQUIVO_PREFERENCIAS = "preferencias_app"
        private const val CHAVE_MODO = "modo_tema"
    }
}
