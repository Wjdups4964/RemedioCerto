package com.example.remediocerto.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Tema do app: automático (segue o celular), claro ou escuro.
 * A escolha fica só neste aparelho (não vai para a conta), porque cada celular pode ter o seu gosto.
 */
enum class ModoTema(val modoNoturno: Int) {
    AUTOMATICO(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    CLARO(AppCompatDelegate.MODE_NIGHT_NO),
    ESCURO(AppCompatDelegate.MODE_NIGHT_YES),
}

object Tema {
    private const val ARQUIVO = "preferencias"
    private const val CHAVE = "tema"

    fun salvo(context: Context): ModoTema {
        val nome = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).getString(CHAVE, null)
        return ModoTema.values().firstOrNull { it.name == nome } ?: ModoTema.AUTOMATICO
    }

    /** Chamado ao abrir o app, antes de qualquer tela aparecer. */
    fun aplicarSalvo(context: Context) {
        AppCompatDelegate.setDefaultNightMode(salvo(context).modoNoturno)
    }

    /** Grava e aplica na hora: o Android recria as telas abertas já com o tema novo. */
    fun escolher(context: Context, modo: ModoTema) {
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).edit().putString(CHAVE, modo.name).apply()
        AppCompatDelegate.setDefaultNightMode(modo.modoNoturno)
    }
}
