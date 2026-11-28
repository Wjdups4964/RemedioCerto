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
    private const val CHAVE_ADIADOS = "adiados"
    private const val CHAVE_RESPONDIDOS = "respondidos"
    private const val UM_DIA_MS = 24 * 60 * 60 * 1000L

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

    // Alarmes repetidos por "Adiar", guardados como "idDaNotificacao:codigoDoAlarme" para poder cancelá-los.
    private fun adiados(context: Context): Set<String> =
        prefs(context).getStringSet(CHAVE_ADIADOS, emptySet()).orEmpty().toSet()

    fun guardarAdiado(context: Context, idNotificacao: Int, codigo: Int) {
        prefs(context).edit().putStringSet(CHAVE_ADIADOS, adiados(context) + "$idNotificacao:$codigo").apply()
    }

    /** Tira da lista os adiamentos desta dose e devolve os códigos dos alarmes, para quem chamou cancelá-los. */
    fun removerAdiados(context: Context, idNotificacao: Int): List<Int> {
        val (daDose, outros) = adiados(context).partition { it.startsWith("$idNotificacao:") }
        if (daDose.isEmpty()) return emptyList()
        prefs(context).edit().putStringSet(CHAVE_ADIADOS, outros.toSet()).apply()
        return daDose.mapNotNull { it.substringAfter(':').toIntOrNull() }
    }

    fun codigosAdiados(context: Context): List<Int> =
        adiados(context).mapNotNull { it.substringAfter(':').toIntOrNull() }

    // Alarmes já respondidos por "Tomei" ou "Adiar" (guardados como "codigo:instante"). Serve para a tela do
    // alarme se fechar sozinha quando a resposta foi dada na notificação, antes de ela aparecer.
    fun marcarRespondido(context: Context, codigo: Int) {
        val limite = System.currentTimeMillis() - UM_DIA_MS
        val recentes = respondidos(context).filter { it.substringAfter(':').toLongOrNull().let { t -> t != null && t > limite } }
        prefs(context).edit()
            .putStringSet(CHAVE_RESPONDIDOS, recentes.toSet() + "$codigo:${System.currentTimeMillis()}")
            .apply()
    }

    fun foiRespondido(context: Context, codigo: Int): Boolean =
        respondidos(context).any { it.substringBefore(':') == codigo.toString() }

    private fun respondidos(context: Context): Set<String> =
        prefs(context).getStringSet(CHAVE_RESPONDIDOS, emptySet()).orEmpty().toSet()

    fun jaPerguntouAlarmeExato(context: Context) = prefs(context).getBoolean(CHAVE_PERGUNTOU_EXATO, false)

    fun marcarPerguntouAlarmeExato(context: Context) {
        prefs(context).edit().putBoolean(CHAVE_PERGUNTOU_EXATO, true).apply()
    }

    /** Apaga os medicamentos e os alarmes guardados (ao sair da conta). Mantém só a pergunta já feita. */
    fun limpar(context: Context) {
        prefs(context).edit().remove(CHAVE_MEDICAMENTOS).remove(CHAVE_CODIGOS).remove(CHAVE_ADIADOS).apply()
    }
}
