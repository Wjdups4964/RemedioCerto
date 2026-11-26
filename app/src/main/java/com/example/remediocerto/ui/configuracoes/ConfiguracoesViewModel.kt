package com.example.remediocerto.ui.configuracoes

import androidx.lifecycle.ViewModel
import com.example.remediocerto.data.Repositorios

class ConfiguracoesViewModel : ViewModel() {

    private val repositorio = Repositorios.configuracao

    /** A chave vale neste aparelho (ver TamanhoFonte); aqui só se guarda uma cópia na conta. */
    fun alterarAcessibilidade(ativo: Boolean) = repositorio.salvarAcessibilidade(ativo)
}
