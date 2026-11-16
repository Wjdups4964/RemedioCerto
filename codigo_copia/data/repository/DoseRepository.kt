package com.example.remediocerto.data.repository

import com.example.remediocerto.data.model.RegistroDose
import kotlinx.coroutines.flow.Flow

interface DoseRepository {
    fun observar(): Flow<List<RegistroDose>>
    fun registrar(registro: RegistroDose)
}