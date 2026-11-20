package com.example.remediocerto.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Os alarmes do Android se perdem ao reiniciar o celular, ao atualizar o app e (os horários) ao mudar o
 * fuso. Aqui eles são agendados de novo a partir da cópia guardada no aparelho, sem precisar de internet.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED -> AlarmeScheduler.reagendarDoSalvo(context)
        }
    }
}
