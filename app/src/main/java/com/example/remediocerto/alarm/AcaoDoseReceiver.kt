package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/** Recebe os botões "Tomei" e "Adiar" da notificação do alarme (funciona com a tela bloqueada). */
class AcaoDoseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val lembrete = intent.lerLembrete() ?: return
        // Dá uns segundos para o registro ser gravado antes de o Android encerrar este receptor.
        val pendente = goAsync()
        when (intent.action) {
            AcaoDose.ACAO_TOMEI -> AcaoDose.tomei(context, lembrete)
            AcaoDose.ACAO_ADIAR -> AcaoDose.adiar(context, lembrete, intent.adiamentoMin())
        }
        Handler(Looper.getMainLooper()).postDelayed({ pendente.finish() }, 3_000)
    }
}
