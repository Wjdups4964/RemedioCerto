package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.Medicamento
import kotlinx.coroutines.flow.Flow

interface MedicamentoRepository {
    /** Emite a lista toda vez que algo muda, inclusive ao cadastrar offline. */
    fun observar(): Flow<List<Medicamento>>

    /** Cria (id vazio) ou atualiza. Não é suspend: grava no cache local na hora e sincroniza depois. */
    fun salvar(medicamento: Medicamento)

    fun apagar(id: String)
}