package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.DadosPessoais
import kotlinx.coroutines.flow.Flow

interface DadosPessoaisRepository {
    /** Emite os dados pessoais (null se ainda não houver) e de novo a cada mudança. */
    fun observar(): Flow<DadosPessoais?>

    /** Grava no cache do celular na hora (funciona offline) e sincroniza depois. */
    fun salvar(dados: DadosPessoais)
}
