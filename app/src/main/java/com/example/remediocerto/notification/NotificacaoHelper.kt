package com.example.remediocerto.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.remediocerto.R
import com.example.remediocerto.alarm.Lembrete
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.ui.main.MainActivity

/** Cria o canal de notificações e mostra os avisos de dose. */
object NotificacaoHelper {
    const val CANAL_LEMBRETES = "lembretes_dose"

    /** Pode ser chamado quantas vezes quiser: se o canal já existe, o Android ignora. */
    fun criarCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_LEMBRETES,
            context.getString(R.string.notif_canal_nome),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notif_canal_descricao)
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    @SuppressLint("MissingPermission") // a permissão é conferida em areNotificationsEnabled()
    fun mostrarLembrete(context: Context, lembrete: Lembrete) {
        val gerenciador = NotificationManagerCompat.from(context)
        if (!gerenciador.areNotificationsEnabled()) return
        criarCanal(context)

        val titulo = if (lembrete.minutosAntes > 0) {
            context.getString(R.string.notif_titulo_antes, lembrete.minutosAntes, lembrete.nome)
        } else {
            context.getString(R.string.notif_titulo_agora, lembrete.nome)
        }
        val texto = if (lembrete.dose.isBlank()) {
            context.getString(R.string.notif_texto_sem_dose, lembrete.hora)
        } else {
            context.getString(R.string.notif_texto_com_dose, lembrete.dose, lembrete.hora)
        }

        // Tocar na notificação abre o app na tela Hoje, onde se confirma a dose.
        val abrirApp = PendingIntent.getActivity(
            context,
            lembrete.idNotificacao,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_PERFIL, Perfil.IDOSO.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_LEMBRETES)
            .setSmallIcon(R.drawable.ic_medicamentos)
            .setColor(ContextCompat.getColor(context, R.color.rc_primaria))
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(abrirApp)
            .build()

        // Os três avisos da mesma dose têm o mesmo id: o de 5 min troca o de 10 min, e assim por diante.
        gerenciador.notify(lembrete.idNotificacao, notificacao)
    }
}
