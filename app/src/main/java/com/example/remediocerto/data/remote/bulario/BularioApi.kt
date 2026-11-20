package com.example.remediocerto.data.remote.bulario

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Rotas do servidor bulario-api (pasta bulario-api do repositório). */
interface BularioApi {

    /** GET api/bulas?nome=losartana&pagina=1 */
    @GET("api/bulas")
    suspend fun pesquisar(
        @Query("nome") nome: String,
        @Query("pagina") pagina: Int,
    ): PesquisaBulaDto

    /** GET api/bulas/losartana */
    @GET("api/bulas/{id}")
    suspend fun detalhes(@Path("id") id: String): BulaDto
}
