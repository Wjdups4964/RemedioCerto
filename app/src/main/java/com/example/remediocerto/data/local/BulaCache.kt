package com.example.remediocerto.data.local

import android.content.Context
import com.example.remediocerto.data.model.Bula
import com.google.gson.Gson

/**
 * Guarda no celular a última bula consultada de cada medicamento, para abrir sem internet
 * (o app deve funcionar principalmente offline). Usa SharedPreferences: são poucos textos curtos.
 */
class BulaCache(context: Context, private val gson: Gson = Gson()) {

    private val prefs = context.applicationContext.getSharedPreferences("bula_cache", Context.MODE_PRIVATE)

    data class Entrada(val bula: Bula = Bula(), val salvoEm: Long = 0L)

    fun salvar(chave: String, bula: Bula, agora: Long = System.currentTimeMillis()) {
        prefs.edit().putString(prefixo + chave, gson.toJson(Entrada(bula, agora))).apply()
    }

    /** Devolve a bula salva, ou null se não há nenhuma (ou se o texto salvo estiver estragado). */
    fun ler(chave: String): Entrada? {
        val texto = prefs.getString(prefixo + chave, null) ?: return null
        val entrada = runCatching { gson.fromJson(texto, Entrada::class.java) }.getOrNull()
        return entrada?.takeIf { it.bula.id.isNotEmpty() }
    }

    private companion object {
        const val prefixo = "bula_"
    }
}
