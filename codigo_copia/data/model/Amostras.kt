package com.example.remediocerto.data.model

// Dados de exemplo que sobraram: só a tela de Vínculo ainda usa (ela passa a usar o servidor na entrega de 24/11).
data class VinculoItem(val nome: String)

object Amostras {
    val vinculos = listOf(VinculoItem("Maria (filha)"), VinculoItem("João (neto)"))
}
