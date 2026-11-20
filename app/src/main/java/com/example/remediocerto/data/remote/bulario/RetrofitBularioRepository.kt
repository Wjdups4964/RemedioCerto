package com.example.remediocerto.data.remote.bulario

import android.util.Log
import com.example.remediocerto.data.local.BulaCache
import com.example.remediocerto.data.model.Bula
import com.example.remediocerto.data.model.MotivoFalhaBula
import com.example.remediocerto.data.model.ResultadoBula
import com.example.remediocerto.data.repository.BularioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.Normalizer

/**
 * Consulta o servidor bulario-api e guarda cada resposta no cache do celular.
 * Fluxo: pesquisa pelo nome -> escolhe o melhor resultado -> busca os detalhes -> salva no cache.
 */
class RetrofitBularioRepository(
    private val api: BularioApi,
    private val cache: BulaCache,
    private val configurado: Boolean = RetrofitProvider.configurado,
) : BularioRepository {

    override suspend fun consultar(nomeMedicamento: String): ResultadoBula = withContext(Dispatchers.IO) {
        val termos = termosDeBusca(nomeMedicamento)
        if (termos.isEmpty()) return@withContext ResultadoBula.Falha(MotivoFalhaBula.NAO_ENCONTRADO)
        val chave = termos.first()

        if (!configurado) {
            return@withContext doCache(chave) ?: ResultadoBula.Falha(MotivoFalhaBula.NAO_CONFIGURADO)
        }

        try {
            val bula = buscarNoServidor(termos)
                ?: return@withContext ResultadoBula.Falha(MotivoFalhaBula.NAO_ENCONTRADO)
            cache.salvar(chave, bula)
            ResultadoBula.Sucesso(bula)
        } catch (e: CancellationException) {
            throw e // a tela foi fechada; não é erro
        } catch (e: IOException) {
            Log.w(TAG, "Sem acesso ao servidor de bulas", e)
            doCache(chave) ?: ResultadoBula.Falha(MotivoFalhaBula.SEM_INTERNET)
        } catch (e: HttpException) {
            Log.w(TAG, "Servidor de bulas respondeu ${e.code()}", e)
            if (e.code() == 404) ResultadoBula.Falha(MotivoFalhaBula.NAO_ENCONTRADO)
            else doCache(chave) ?: ResultadoBula.Falha(MotivoFalhaBula.SERVIDOR)
        } catch (e: Exception) {
            Log.e(TAG, "Resposta inesperada do servidor de bulas", e)
            doCache(chave) ?: ResultadoBula.Falha(MotivoFalhaBula.SERVIDOR)
        }
    }

    override suspend fun sugerir(trecho: String): List<String> = withContext(Dispatchers.IO) {
        val termo = semAcento(trecho).trim()
        if (!configurado || termo.length < 2) return@withContext emptyList()
        try {
            api.pesquisar(termo, pagina = 1).itens
                .map { it.nome }
                .filter { it.isNotBlank() }
                .distinct()
                .let { ordenarSugestoes(it, termo) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Sem sugestões de nomes agora", e)
            emptyList()
        }
    }

    /** Tenta cada termo, do mais específico ao mais geral, e devolve os detalhes do melhor resultado. */
    private suspend fun buscarNoServidor(termos: List<String>): Bula? {
        for (termo in termos) {
            val itens = api.pesquisar(termo, pagina = 1).itens
            val escolhido = escolherMelhor(itens, termo) ?: continue
            return api.detalhes(escolhido.id).paraModelo()
        }
        return null
    }

    private fun doCache(chave: String): ResultadoBula.Sucesso? =
        cache.ler(chave)?.let { ResultadoBula.Sucesso(it.bula, doCache = true, salvoEm = it.salvoEm) }

    private companion object {
        const val TAG = "BularioRepo"
    }
}

private val UNIDADES = setOf(
    "mg", "g", "mcg", "ml", "ui", "gotas", "gts", "comprimido", "comprimidos", "comp", "cp",
    "capsula", "capsulas", "caps", "xarope", "solucao", "sol",
)

private fun semAcento(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

/**
 * Transforma o nome cadastrado ("Losartana 50 mg") nos termos de busca: primeiro o nome sem números
 * e unidades, depois só a primeira palavra. Sem acento, em minúsculas e com pelo menos 2 letras.
 */
internal fun termosDeBusca(nome: String): List<String> {
    val palavras = semAcento(nome).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
    val uteis = palavras.filter { p -> p.none(Char::isDigit) && p !in UNIDADES }
    return listOf(uteis.joinToString(" "), uteis.firstOrNull().orEmpty())
        .filter { it.length >= 2 }
        .distinct()
}

/** Prefere o resultado cujo nome é igual ao termo; se não houver, o primeiro da lista. */
internal fun escolherMelhor(itens: List<BulaResumoDto>, termo: String): BulaResumoDto? =
    itens.firstOrNull { semAcento(it.nome) == termo || it.id == termo.replace(' ', '-') } ?: itens.firstOrNull()

/** Nomes que começam com o termo vêm primeiro; dentro de cada grupo, ordem alfabética. */
internal fun ordenarSugestoes(nomes: List<String>, termo: String): List<String> =
    nomes.sortedWith(
        compareBy<String> { !semAcento(it).startsWith(termo) }.thenBy { semAcento(it) }
    )
