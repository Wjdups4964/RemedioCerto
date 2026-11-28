package com.example.remediocerto.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.ui.entrada.EntradaActivity
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
    const val EXTRA_ADIADO_PARA = "adiadoPara"
    const val EXTRA_ADIAMENTO_MIN = "adiamentoMin"

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
        (AgendaLocal.lerCodigos(app) + AgendaLocal.codigosAdiados(app)).forEach { cancelar(app, alarmManager, it) }
        AgendaLocal.limpar(app)
    }

    /** "Adiar": o alarme da dose toca de novo daqui a [minutos] minutos. */
    fun agendarAdiamento(context: Context, lembrete: Lembrete, minutos: Int) {
        val app = context.applicationContext
        val adiado = lembrete.copy(minutosAntes = 0, adiadoPara = System.currentTimeMillis() + minutos * 60_000L)
        agendarUm(app, app.getSystemService(AlarmManager::class.java), adiado, PermissoesHelper.podeAgendarExato(app))
        AgendaLocal.guardarAdiado(app, adiado.idNotificacao, adiado.codigo)
    }

    /** A dose foi tomada (ou o adiamento já tocou): cancela o alarme repetido que estiver esperando. */
    fun cancelarAdiamento(context: Context, idNotificacao: Int) {
        val app = context.applicationContext
        val codigos = AgendaLocal.removerAdiados(app, idNotificacao)
        if (codigos.isEmpty()) return
        val alarmManager = app.getSystemService(AlarmManager::class.java)
        codigos.forEach { cancelar(app, alarmManager, it) }
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
        val intent = intentBase(context).comLembrete(lembrete)
        val pendente = PendingIntent.getBroadcast(
            context, lembrete.codigo, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Os avisos de 10 e 5 minutos antes são aproximados de propósito. Com a tela apagada, o Android
        // deixa cada app disparar só um alarme exato a cada 9 minutos, e essa vez precisa ser a do alarme.
        if (lembrete.minutosAntes != 0) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
            return
        }

        try {
            if (exato) {
                // Despertador: o Android não adia por economia de bateria e mostra o ícone de alarme
                // na barra de status, igual ao app de Relógio.
                val info = AlarmManager.AlarmClockInfo(lembrete.disparaEm, telaInicial(context))
                alarmManager.setAlarmClock(info, pendente)
            } else {
                // Sem a permissão de alarme exato o Android pode atrasar o aviso em alguns minutos.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
            }
        } catch (e: SecurityException) {
            // A permissão foi retirada no meio do caminho: agenda do jeito aproximado.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lembrete.disparaEm, pendente)
        }
    }

    // Para onde o ícone de alarme da barra de status leva quando a pessoa toca nele.
    private fun telaInicial(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, EntradaActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

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
