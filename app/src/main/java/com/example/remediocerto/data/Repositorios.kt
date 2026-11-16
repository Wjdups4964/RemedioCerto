package com.example.remediocerto.data

import com.example.remediocerto.data.remote.firestore.FirestoreConfiguracaoRepository
import com.example.remediocerto.data.remote.firestore.FirestoreDadosPessoaisRepository
import com.example.remediocerto.data.remote.firestore.FirestoreDoseRepository
import com.example.remediocerto.data.remote.firestore.FirestoreMedicamentoRepository
import com.example.remediocerto.data.repository.ConfiguracaoRepository
import com.example.remediocerto.data.repository.DadosPessoaisRepository
import com.example.remediocerto.data.repository.DoseRepository
import com.example.remediocerto.data.repository.MedicamentoRepository

/**
 * Ponto único de acesso aos repositórios. Os ViewModels pedem tudo daqui, então trocar
 * a fonte de dados (por exemplo, para um repositório de teste) é mudar só este arquivo.
 * O "by lazy" cria cada repositório só no primeiro uso, já com o usuário logado.
 */
object Repositorios {
    val medicamentos: MedicamentoRepository by lazy { FirestoreMedicamentoRepository() }
    val doses: DoseRepository by lazy { FirestoreDoseRepository() }
    val configuracao: ConfiguracaoRepository by lazy { FirestoreConfiguracaoRepository() }
    val dadosPessoais: DadosPessoaisRepository by lazy { FirestoreDadosPessoaisRepository() }
}
