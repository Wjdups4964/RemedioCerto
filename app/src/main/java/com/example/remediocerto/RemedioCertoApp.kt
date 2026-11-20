package com.example.remediocerto

import android.app.Application
import com.example.remediocerto.data.Repositorios
import com.example.remediocerto.notification.NotificacaoHelper
import com.example.remediocerto.util.Tema

class RemedioCertoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repositorios.iniciar(this)
        // Aplica o tema escolhido (automático, claro ou escuro) antes de abrir qualquer tela.
        Tema.aplicarSalvo(this)
        // O canal de notificações precisa existir antes do primeiro aviso.
        NotificacaoHelper.criarCanal(this)
    }
}
