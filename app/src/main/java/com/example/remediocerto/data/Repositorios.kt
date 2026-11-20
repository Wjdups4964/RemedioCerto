package com.example.remediocerto.data

import android.content.Context
import com.example.remediocerto.data.local.BulaCache
import com.example.remediocerto.data.remote.bulario.RetrofitBularioRepository
import com.example.remediocerto.data.remote.bulario.RetrofitProvider
import com.example.remediocerto.data.remote.firestore.FirestoreAcompanhamentoRepository
import com.example.remediocerto.data.remote.firestore.FirestoreConfiguracaoRepository
import com.example.remediocerto.data.remote.firestore.FirestoreDadosPessoaisRepository
import com.example.remediocerto.data.remote.firestore.FirestoreDoseRepository
import com.example.remediocerto.data.remote.firestore.FirestoreMedicamentoRepository
import com.example.remediocerto.data.remote.firestore.FirestoreVinculoRepository
import com.example.remediocerto.data.repository.AcompanhamentoRepository
import com.example.remediocerto.data.repository.BularioRepository
import com.example.remediocerto.data.repository.ConfiguracaoRepository
import com.example.remediocerto.data.repository.DadosPessoaisRepository
import com.example.remediocerto.data.repository.DoseRepository
import com.example.remediocerto.data.repository.MedicamentoRepository
import com.example.remediocerto.data.repository.VinculoRepository

/**
 * Ponto único de acesso aos repositórios. Os ViewModels pedem tudo daqui, então trocar
 * a fonte de dados (por exemplo, para um repositório de teste) é mudar só este arquivo.
 * O "by lazy" cria cada repositório só no primeiro uso, já com o usuário logado.
 */
object Repositorios {
    private lateinit var contexto: Context

    /** Chamado uma vez pela RemedioCertoApp, para o cache de bulas saber onde gravar. */
    fun iniciar(context: Context) {
        contexto = context.applicationContext
    }

    val medicamentos: MedicamentoRepository by lazy { FirestoreMedicamentoRepository() }
    val doses: DoseRepository by lazy { FirestoreDoseRepository() }
    val configuracao: ConfiguracaoRepository by lazy { FirestoreConfiguracaoRepository() }
    val dadosPessoais: DadosPessoaisRepository by lazy { FirestoreDadosPessoaisRepository() }
    val vinculos: VinculoRepository by lazy { FirestoreVinculoRepository() }
    val acompanhamento: AcompanhamentoRepository by lazy { FirestoreAcompanhamentoRepository() }

    val bulario: BularioRepository by lazy {
        RetrofitBularioRepository(RetrofitProvider.bulario, BulaCache(contexto))
    }
}
