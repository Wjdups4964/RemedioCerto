package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remediocerto.notification.NotificacaoHelper

/** Recebe o aviso na hora marcada, mostra a notificação e renova os próximos avisos. */
class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmeScheduler.ACAO_LEMBRETE) return

        val lembrete = Lembrete(
            medicamentoId = intent.getStringExtra(AlarmeScheduler.EXTRA_MEDICAMENTO_ID).orEmpty(),
            nome = intent.getStringExtra(AlarmeScheduler.EXTRA_NOME).orEmpty(),
            dose = intent.getStringExtra(AlarmeScheduler.EXTRA_DOSE).orEmpty(),
            hora = intent.getStringExtra(AlarmeScheduler.EXTRA_HORA).orEmpty(),
            previstoEm = intent.getLongExtra(AlarmeScheduler.EXTRA_PREVISTO_EM, 0L),
            minutosAntes = intent.getIntExtra(AlarmeScheduler.EXTRA_MINUTOS_ANTES, 0),
        )
        NotificacaoHelper.mostrarLembrete(context, lembrete)

        // Mantém sempre os próximos dias agendados, mesmo que a pessoa não abra o app por vários dias.
        AlarmeScheduler.reagendarDoSalvo(context)
    }
}
