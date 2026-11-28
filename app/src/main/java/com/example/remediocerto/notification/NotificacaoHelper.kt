package com.example.remediocerto.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.remediocerto.R
import com.example.remediocerto.alarm.AcaoDose
import com.example.remediocerto.alarm.AcaoDoseReceiver
import com.example.remediocerto.alarm.Lembrete
import com.example.remediocerto.alarm.comLembrete
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.ui.alarme.AlarmeActivity
import com.example.remediocerto.ui.main.MainActivity

/** Cria o canal de notificações e mostra os avisos de dose. */
object NotificacaoHelper {
    const val CANAL_LEMBRETES = "lembretes_dose"
    const val CANAL_ALARME = "alarme_dose"

    // Se ninguém tocar em Tomei ou Adiar, o alarme para de tocar sozinho depois deste tempo.
    private const val TEMPO_MAXIMO_ALARME_MS = 5 * 60 * 1000L

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

    /** Canal do alarme da hora da dose: som de alarme, vibração e tela cheia. Também é seguro chamar várias vezes. */
    fun criarCanalAlarme(context: Context) {
        val som = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val canal = NotificationChannel(
            CANAL_ALARME,
            context.getString(R.string.notif_canal_alarme_nome),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notif_canal_alarme_descricao)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), som)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 700, 400, 700, 400, 700)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun cancelar(context: Context, idNotificacao: Int) {
        NotificationManagerCompat.from(context).cancel(idNotificacao)
    }

    /**
     * O alarme da hora de tomar: abre a tela do alarme por cima da tela bloqueada, toca e vibra repetidamente
     * até alguém tocar em Tomei ou Adiar (ou até [TEMPO_MAXIMO_ALARME_MS]). Os mesmos dois botões ficam na
     * notificação, para quando o aparelho está em uso e o Android mostra só o aviso no topo da tela.
     */
    @SuppressLint("MissingPermission") // a permissão é conferida em areNotificationsEnabled()
    fun mostrarAlarme(context: Context, lembrete: Lembrete, adiamentoMin: Int) {
        val gerenciador = NotificationManagerCompat.from(context)
        if (!gerenciador.areNotificationsEnabled()) return
        criarCanalAlarme(context)

        val titulo = context.getString(R.string.notif_titulo_agora, lembrete.nome)
        val texto = if (lembrete.dose.isBlank()) {
            context.getString(R.string.notif_texto_sem_dose, lembrete.hora)
        } else {
            context.getString(R.string.notif_texto_com_dose, lembrete.dose, lembrete.hora)
        }

        val telaDoAlarme = PendingIntent.getActivity(
            context,
            lembrete.idNotificacao,
            Intent(context, AlarmeActivity::class.java)
                .comLembrete(lembrete, adiamentoMin)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_ALARME)
            .setSmallIcon(R.drawable.ic_medicamentos)
            .setColor(ContextCompat.getColor(context, R.color.rc_primaria))
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setTimeoutAfter(TEMPO_MAXIMO_ALARME_MS)
            .setContentIntent(telaDoAlarme)
            .setFullScreenIntent(telaDoAlarme, true)
            .addAction(0, context.getString(R.string.alarme_tomei), acaoDaDose(context, lembrete, adiamentoMin, AcaoDose.ACAO_TOMEI, 1))
            .addAction(0, context.getString(R.string.alarme_adiar_min, adiamentoMin), acaoDaDose(context, lembrete, adiamentoMin, AcaoDose.ACAO_ADIAR, 2))
            .build()
        // Repete o som e a vibração até a notificação ser respondida ou sair.
        notificacao.flags = notificacao.flags or Notification.FLAG_INSISTENT

        gerenciador.notify(lembrete.idNotificacao, notificacao)
    }

    private fun acaoDaDose(context: Context, lembrete: Lembrete, adiamentoMin: Int, acao: String, numero: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            lembrete.idNotificacao + numero,
            Intent(context, AcaoDoseReceiver::class.java).setAction(acao).comLembrete(lembrete, adiamentoMin),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

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
