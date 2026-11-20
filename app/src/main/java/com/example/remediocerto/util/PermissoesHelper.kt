package com.example.remediocerto.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Permissões que os avisos de dose precisam (Android 12 ou mais novo). */
object PermissoesHelper {

    /** No Android 13 ou mais novo é preciso pedir permissão para mostrar notificações. */
    fun faltaPermissaoDeNotificacao(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    /**
     * As notificações do app estão desligadas? Vale para quem recusou a permissão (Android 13 ou mais novo)
     * e para quem desligou depois, nas configurações do Android.
     */
    fun notificacoesDesligadas(context: Context): Boolean =
        !NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Tela do sistema onde se liga as notificações do app. */
    fun intentAjusteNotificacoes(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    /** No Android 12 ou mais novo, o aviso no minuto exato depende de "Alarmes e lembretes". */
    fun podeAgendarExato(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /** Tela do sistema onde a pessoa permite "Alarmes e lembretes"; null nos aparelhos que não precisam. */
    fun intentAjusteAlarmeExato(context: Context): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
        } else {
            null
        }
}
