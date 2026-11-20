package com.example.remediocerto.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.util.PermissoesHelper

/**
 * Agenda no AlarmManager os avisos de cada dose (10 min antes, 5 min antes e na hora).
 *
 * A cada chamada, cancela os avisos agendados antes e agenda de novo os dos próximos dias. Por isso
 * tanto faz o que mudou (um remédio novo, um horário editado, um remédio apagado): é só chamar de novo.
 */
object AlarmeScheduler {
    const val ACAO_LEMBRETE = "com.example.remediocerto.LEMBRETE"
    const val EXTRA_MEDICAMENTO_ID = "medicamentoId"
    const val EXTRA_NOME = "nome"
    const val EXTRA_DOSE = "dose"
    const val EXTRA_HORA = "hora"
    const val EXTRA_PREVISTO_EM = "previstoEm"
    const val EXTRA_MINUTOS_ANTES = "minutosAntes"

    /** Guarda a lista de medicamentos no celular e reagenda os avisos. Chamado quando a lista muda. */
    fun atualizar(context: Context, medicamentos: List<Medicamento>) {
        AgendaLocal.salvarMedicamentos(context, medicamentos)
        agendar(context, medicamentos)
    }

    /** Reagenda usando a lista guardada: depois de reiniciar o aparelho, atualizar o app ou a cada aviso. */
    fun reagendarDoSalvo(context: Context) {
        agendar(context, AgendaLocal.lerMedicamentos(context))
    }

    /** Cancela tudo e apaga a cópia local. Chamado ao sair da conta. */
    fun cancelarTudo(context: Context) {
        val app = context.applicationContext
        val alarmManager = app.getSystemService(AlarmManager::class.java)
        AgendaLocal.lerCodigos(app).forEach { cancelar(app, alarmManager, it) }
        AgendaLocal.limpar(app)
    }

    private fun agendar(context: Context, medicamentos: List<Medicamento>) {
        val app = context.applicationContext
        val alarmManager = app.getSystemService(AlarmManager::class.java)

        AgendaLocal.lerCodigos(app).forEach { cancelar(app, alarmManager, it) }

        val lembretes = proximosLembretes(medicamentos, System.currentTimeMillis())
        val exato = PermissoesHelper.podeAgendarExato(app)
        lembretes.forEach { agendarUm(app, alarmManager, it, exato) }
        AgendaLocal.salvarCodigos(app, lembretes.map { it.codigo })
    }

    private fun agendarUm(context: Context, alarmManager: AlarmManager, lembrete: Lembrete, exato: Boolean) {
        val intent = intentBase(context)
            .putExtra(EXTRA_MEDICAMENTO_ID, lembrete.medicamentoId)
            .putExtra(EXTRA_NOME, lembrete.nome)
            .putExtra(EXTRA_DOSE, lembrete.dose)
            .putExtra(EXTRA_HORA, lembrete.hora)
            .putExtra(EXTRA_PREVISTO_EM, lembrete.previstoEm)
            .putExtra(EXTRA_MINUTOS_ANTES, lembrete.minutosAntes)
        val pendente = PendingIntent.getBroadcast(
            context, lembrete.codigo, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (exato) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
            } else {
                // Sem a permissão de alarme exato o Android pode atrasar o aviso em alguns minutos.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
            }
        } catch (e: SecurityException) {
            // A permissão foi retirada no meio do caminho: agenda do jeito aproximado.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
        }
    }

    private fun cancelar(context: Context, alarmManager: AlarmManager, codigo: Int) {
        val pendente = PendingIntent.getBroadcast(
            context, codigo, intentBase(context), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendente)
        pendente.cancel()
    }

    // O AlarmManager identifica o aviso pelo código + ação + destino. Os "extras" não contam.
    private fun intentBase(context: Context) =
        Intent(context, AlarmeReceiver::class.java).setAction(ACAO_LEMBRETE)
}
