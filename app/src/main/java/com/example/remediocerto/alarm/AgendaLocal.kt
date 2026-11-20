package com.example.remediocerto.alarm

import android.content.Context
import com.example.remediocerto.data.model.Medicamento
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Cópia dos medicamentos guardada no próprio celular (SharedPreferences), só para os avisos.
 * Assim o aviso é reagendado depois de reiniciar o aparelho ou de atualizar o app, mesmo sem abrir
 * o aplicativo e sem internet. Também guarda quais alarmes estão agendados, para poder cancelá-los.
 */
internal object AgendaLocal {
    private const val ARQUIVO = "agenda_lembretes"
    private const val CHAVE_MEDICAMENTOS = "medicamentos"
    private const val CHAVE_CODIGOS = "codigos"
    private const val CHAVE_PERGUNTOU_EXATO = "perguntou_alarme_exato"

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun salvarMedicamentos(context: Context, medicamentos: List<Medicamento>) {
        prefs(context).edit().putString(CHAVE_MEDICAMENTOS, gson.toJson(medicamentos)).apply()
    }

    fun lerMedicamentos(context: Context): List<Medicamento> {
        val json = prefs(context).getString(CHAVE_MEDICAMENTOS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<Medicamento>>(json, object : TypeToken<List<Medicamento>>() {}.type)
        }.getOrNull().orEmpty()
    }

    fun salvarCodigos(context: Context, codigos: Collection<Int>) {
        prefs(context).edit().putStringSet(CHAVE_CODIGOS, codigos.map { it.toString() }.toSet()).apply()
    }

    fun lerCodigos(context: Context): List<Int> =
        prefs(context).getStringSet(CHAVE_CODIGOS, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }

    fun jaPerguntouAlarmeExato(context: Context) = prefs(context).getBoolean(CHAVE_PERGUNTOU_EXATO, false)

    fun marcarPerguntouAlarmeExato(context: Context) {
        prefs(context).edit().putBoolean(CHAVE_PERGUNTOU_EXATO, true).apply()
    }

    /** Apaga os medicamentos e os alarmes guardados (ao sair da conta). Mantém só a pergunta já feita. */
    fun limpar(context: Context) {
        prefs(context).edit().remove(CHAVE_MEDICAMENTOS).remove(CHAVE_CODIGOS).apply()
    }
}
