package com.example.remediocerto.util

import android.content.Context
import android.content.res.Configuration

/**
 * Tamanho das letras do app.
 *
 * - Modo padrão (chave desligada): letras de tamanho médio.
 * - Modo de acessibilidade (chave ligada): as letras grandes de antes.
 *
 * Funciona aumentando ou diminuindo a escala de fonte do app inteiro, então todo texto em "sp" acompanha.
 * A escolha fica só neste aparelho, como o tema. A tela do alarme não usa isto: ela sempre fica grande.
 */
object TamanhoFonte {
    private const val ARQUIVO = "preferencias"
    private const val CHAVE = "letras_grandes"

    // Quanto as letras diminuem no modo padrão, em relação ao tamanho grande.
    private const val ESCALA_PADRAO = 0.80f

    fun acessibilidadeAtiva(context: Context): Boolean =
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).getBoolean(CHAVE, false)

    fun escolher(context: Context, ativa: Boolean) {
        context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE).edit().putBoolean(CHAVE, ativa).apply()
    }

    /** Chamado no attachBaseContext das telas: devolve o contexto já com a escala de fonte certa. */
    fun aplicar(base: Context): Context {
        if (acessibilidadeAtiva(base)) return base
        val configuracao = Configuration(base.resources.configuration)
        configuracao.fontScale = configuracao.fontScale * ESCALA_PADRAO
        return base.createConfigurationContext(configuracao)
    }
}
