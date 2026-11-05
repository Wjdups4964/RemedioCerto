package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reagenda os alarmes depois que o aparelho reinicia (RF11). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: ler horários ativos no Room e chamar o AlarmeScheduler
    }
}
