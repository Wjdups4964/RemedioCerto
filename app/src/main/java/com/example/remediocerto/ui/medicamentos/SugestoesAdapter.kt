package com.example.remediocerto.ui.medicamentos

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.example.remediocerto.R
import java.text.Normalizer

/** "Losartana" e "losartana" (ou "Cápsula" e "capsula") contam como o mesmo nome. */
internal fun mesmoNome(a: String, b: String): Boolean = normalizarNome(a) == normalizarNome(b)

private fun normalizarNome(texto: String): String =
    Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

/**
 * Lista de sugestões do campo de nome. A lista já chega filtrada pelo servidor, então este adaptador
 * não filtra de novo (o filtro padrão do Android esconderia, por exemplo, "Ácido" ao digitar "acido").
 *
 * A lista continua aberta enquanto a pessoa digita (com as sugestões anteriores) e se atualiza quando
 * o servidor responde. Ela fecha sozinha quando o texto digitado já é igual a uma sugestão (nome completo).
 */
class SugestoesAdapter(context: Context) :
    ArrayAdapter<String>(context, R.layout.item_sugestao) {

    fun trocar(nomes: List<String>) {
        setNotifyOnChange(false) // troca tudo de uma vez, sem a lista piscar vazia no meio
        clear()
        addAll(nomes)
        notifyDataSetChanged()
    }

    private fun nomesAtuais(): List<String> = (0 until count).mapNotNull { getItem(it) }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val digitado = constraint?.toString().orEmpty()
            val nomeCompleto = nomesAtuais().any { mesmoNome(it, digitado) }
            return FilterResults().apply { count = if (nomeCompleto) 0 else this@SugestoesAdapter.count }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
    }
}
