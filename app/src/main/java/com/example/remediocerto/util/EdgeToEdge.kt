package com.example.remediocerto.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * No Android 15+ o app desenha sob as barras do sistema. Esta função aplica o
 * espaço das barras como padding da view raiz da tela.
 */
fun View.aplicarInsetsDoSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}
