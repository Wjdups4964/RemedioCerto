package com.example.remediocerto.ui.medicamentos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.remediocerto.data.model.Medicamento
import com.example.remediocerto.data.model.TipoFrequencia
import com.example.remediocerto.databinding.ItemMedicamentoBinding
import com.example.remediocerto.util.formatarDiaLocal
import com.example.remediocerto.util.somarDias

/** Nome do argumento que leva o id do medicamento da lista para Detalhes e Cadastro (edição). */
const val ARG_MEDICAMENTO_ID = "medicamentoId"

/**
 * Resumo da agenda em uma linha, por exemplo:
 * "500 mg · a cada 8 h (06:00, 14:00, 22:00) · a cada 7 dias · até 18/12/2026"
 * Cada parte só aparece se existir (dose vazia, todo dia e uso contínuo não aparecem).
 */
fun Medicamento.resumo(): String {
    val quando = if (tipoFrequencia == TipoFrequencia.INTERVALO_HORAS && intervaloHoras > 0) {
        "a cada $intervaloHoras h (${horarios.joinToString(", ")})"
    } else {
        horarios.joinToString(" e ")
    }
    val dias = if (intervaloDias > 1) "a cada $intervaloDias dias" else null
    // Com data de início, mostra o dia em que termina; sem ela (medicamento antigo), só a quantidade de dias.
    val duracao = when {
        duracaoDias > 0 && inicioEm > 0 -> "até ${formatarDiaLocal(somarDias(inicioEm, duracaoDias - 1))}"
        duracaoDias > 0 -> "por $duracaoDias dias"
        else -> null
    }
    return listOfNotNull(dose.ifBlank { null }, quando.ifBlank { null }, dias, duracao).joinToString(" · ")
}

class MedicamentoAdapter(
    private val aoClicar: (Medicamento) -> Unit,
) : ListAdapter<Medicamento, MedicamentoAdapter.ViewHolder>(Comparador) {

    class ViewHolder(val binding: ItemMedicamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMedicamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvNome.text = item.nome
        holder.binding.tvDoseHorarios.text = item.resumo()
        holder.binding.root.setOnClickListener { aoClicar(item) }
    }

    private object Comparador : DiffUtil.ItemCallback<Medicamento>() {
        override fun areItemsTheSame(a: Medicamento, b: Medicamento) = a.id == b.id
        override fun areContentsTheSame(a: Medicamento, b: Medicamento) = a == b
    }
}
