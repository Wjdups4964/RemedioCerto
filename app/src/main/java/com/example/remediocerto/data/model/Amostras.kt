package com.example.remediocerto.data.model

// DADOS DE EXEMPLO — usados só para mostrar as telas funcionando.
// Serão substituídos pelo Room (local) e pelo Firestore (familiar).

data class DoseDia(val hora: String, val nome: String, val dose: String, val status: String)

data class MedicamentoItem(val nome: String, val dose: String, val horarios: String)

data class HistoricoItem(val data: String, val nome: String, val status: String)

data class VinculoItem(val nome: String)

object Amostras {
    val dosesHoje = listOf(
        DoseDia("08:00", "Losartana", "50 mg · 1 comprimido", "Tomada"),
        DoseDia("12:00", "Metformina", "850 mg · 1 comprimido", "Próxima"),
        DoseDia("20:00", "Losartana", "50 mg · 1 comprimido", "Próxima"),
    )

    val medicamentos = listOf(
        MedicamentoItem("Losartana", "50 mg", "08:00 e 20:00"),
        MedicamentoItem("Metformina", "850 mg", "12:00"),
        MedicamentoItem("Omeprazol", "20 mg", "07:00"),
    )

    val historico = listOf(
        HistoricoItem("03/11 · 20:00", "Losartana", "Tomada"),
        HistoricoItem("03/11 · 12:00", "Metformina", "Tomada"),
        HistoricoItem("03/11 · 08:00", "Losartana", "Não confirmada"),
        HistoricoItem("02/11 · 20:00", "Losartana", "Tomada"),
    )

    val vinculos = listOf(VinculoItem("Maria (filha)"), VinculoItem("João (neto)"))
}
