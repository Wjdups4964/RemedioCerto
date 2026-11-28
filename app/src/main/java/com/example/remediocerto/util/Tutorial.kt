package com.example.remediocerto.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/**
 * Controla se o tutorial já foi mostrado. A marca é por conta e por celular: quem entra pela primeira vez
 * vê o tutorial sozinho, e depois só quando abrir "Ver tutorial" em Configurações.
 */
object Tutorial {
    private const val ARQUIVO = "tutorial"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    private fun chave(): String? =
        FirebaseAuth.getInstance().currentUser?.uid?.let { "visto_$it" }

    /** Sem conta logada, não mostra nada. */
    fun precisaMostrar(context: Context): Boolean {
        val chave = chave() ?: return false
        return !prefs(context).getBoolean(chave, false)
    }

    fun marcarVisto(context: Context) {
        val chave = chave() ?: return
        prefs(context).edit().putBoolean(chave, true).apply()
    }
}
