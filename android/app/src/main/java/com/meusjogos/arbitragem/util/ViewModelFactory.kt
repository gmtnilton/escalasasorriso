package com.meusjogos.arbitragem.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Fábrica genérica de ViewModel para injeção manual (sem Hilt/Dagger). */
class ViewModelFactory<T : ViewModel>(private val criar: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = criar() as VM
}
