package com.example.remediocerto.alarm

import android.content.Context
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.data.model.RegistroDose
import com.example.remediocerto.data.model.StatusDose
import com.example.remediocerto.notification.NotificacaoHelper

/**
 * O que acontece quando a pessoa toca em "Tomei" ou "Adiar" no alarme (tela cheia ou botões da notificação).
 * Grava no cache do celular na hora (funciona sem internet) e o Firestore sincroniza depois.
 */
object AcaoDose {
    const val ACAO_TOMEI = "com.example.remediocerto.DOSE_TOMEI"
    const val ACAO_ADIAR = "com.example.remediocerto.DOSE_ADIAR"

    fun tomei(context: Context, lembrete: Lembrete) {
        registrar(lembrete, StatusDose.TOMADA)
        AgendaLocal.marcarRespondido(context, lembrete.codigo)
        AlarmeScheduler.cancelarAdiamento(context, lembrete.idNotificacao)
        NotificacaoHelper.cancelar(context, lembrete.idNotificacao)
    }

    /** Registra o adiamento e agenda um novo alarme para daqui a [minutos] minutos. */
    fun adiar(context: Context, lembrete: Lembrete, minutos: Int) {
        registrar(lembrete, StatusDose.ADIADA)
        AgendaLocal.marcarRespondido(context, lembrete.codigo)
        NotificacaoHelper.cancelar(context, lembrete.idNotificacao)
        AlarmeScheduler.agendarAdiamento(context, lembrete, minutos)
    }

    private fun registrar(lembrete: Lembrete, status: StatusDose) {
        Repositorios.doses.registrar(
            RegistroDose(
                medicamentoId = lembrete.medicamentoId,
                medicamentoNome = lembrete.nome,
                previstoEm = lembrete.previstoEm,
                registradoEm = System.currentTimeMillis(),
                status = status,
            )
        )
    }
}
