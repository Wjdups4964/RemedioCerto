package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remediocerto.notification.NotificacaoHelper

/**
 * Recebe o aviso na hora marcada. Os avisos de 10 e 5 minutos antes viram uma notificação comum; o da hora
 * da dose (e o repetido depois de "Adiar") vira o alarme, com som, tela cheia e os botões Tomei e Adiar.
 */
class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmeScheduler.ACAO_LEMBRETE) return
        val lembrete = intent.lerLembrete() ?: return

        if (lembrete.minutosAntes == 0) {
            if (lembrete.adiadoPara > 0L) AgendaLocal.removerAdiados(context, lembrete.idNotificacao)
            val adiamento = AgendaLocal.lerMedicamentos(context)
                .firstOrNull { it.id == lembrete.medicamentoId }?.adiamentoMin ?: ADIAMENTO_PADRAO_MIN
            NotificacaoHelper.mostrarAlarme(context, lembrete, adiamento)
        } else {
            NotificacaoHelper.mostrarLembrete(context, lembrete)
        }

        // Mantém sempre os próximos dias agendados, mesmo que a pessoa não abra o app por vários dias.
        AlarmeScheduler.reagendarDoSalvo(context)
    }
}
