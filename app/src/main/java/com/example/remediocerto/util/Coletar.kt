package com.example.remediocerto.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Recebe os valores de um Flow enquanto a tela está visível e para quando ela some
 * (evita gastar bateria e atualizar uma tela que já foi destruída).
 */
fun <T> Fragment.coletar(fluxo: Flow<T>, aoReceber: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            fluxo.collect { aoReceber(it) }
        }
    }
}
