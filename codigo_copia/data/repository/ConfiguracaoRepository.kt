package com.example.remediocerto.data.repository

import kotlinx.coroutines.flow.Flow

interface ConfiguracaoRepository {
    /** Emite o estado da chave de acessibilidade, e de novo a cada mudança. */
    fun observarAcessibilidade(): Flow<Boolean>

    /** Grava no cache do celular na hora e sincroniza depois. */
    fun salvarAcessibilidade(ativo: Boolean)
}