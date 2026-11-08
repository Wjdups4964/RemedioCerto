package com.example.remediocerto.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.aplicarInsetsDoSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}
