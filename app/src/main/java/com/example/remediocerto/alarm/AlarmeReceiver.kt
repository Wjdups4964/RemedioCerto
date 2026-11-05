package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recebe o disparo do AlarmManager (RF08). A notificação e a AlarmeActivity entram na etapa de alarmes. */
class AlarmeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: mostrar notificação com som e vibração e abrir a AlarmeActivity
    }
}
