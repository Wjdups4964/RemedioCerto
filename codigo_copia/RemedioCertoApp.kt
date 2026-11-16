package com.example.remediocerto

import android.app.Application
import com.example.remediocerto.util.Tema

class RemedioCertoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Aplica o tema escolhido (automático, claro ou escuro) antes de abrir qualquer tela.
        Tema.aplicarSalvo(this)
    }
}
