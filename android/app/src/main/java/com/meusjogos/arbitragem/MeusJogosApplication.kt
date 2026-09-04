package com.meusjogos.arbitragem

import android.app.Application
import com.meusjogos.arbitragem.data.local.AppDatabase
import com.meusjogos.arbitragem.data.preferences.AtivacaoPreferences
import com.meusjogos.arbitragem.data.preferences.TemaPreferences
import com.meusjogos.arbitragem.data.repository.JogoRepository

/**
 * Ponto único de montagem das dependências do app (injeção manual — o app
 * é pequeno o suficiente para não precisar de um framework de DI).
 */
class MeusJogosApplication : Application() {

    val repository: JogoRepository by lazy {
        JogoRepository(AppDatabase.getInstance(this).jogoDao())
    }

    val temaPreferences: TemaPreferences by lazy { TemaPreferences(this) }

    val ativacaoPreferences: AtivacaoPreferences by lazy { AtivacaoPreferences(this) }
}
