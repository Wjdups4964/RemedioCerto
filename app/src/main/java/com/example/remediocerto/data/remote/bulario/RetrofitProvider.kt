package com.example.remediocerto.data.remote.bulario

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Cria o cliente Retrofit do serviço bulario-api. */
object RetrofitProvider {

    /**
     * Endereço do servidor bulario-api depois do deploy na Vercel (veja bulario-api/README.md).
     * Precisa terminar com "/". Para testar no emulador com o servidor local (node server.js),
     * use "http://10.0.2.2:3000/" (e libere tráfego sem HTTPS no AndroidManifest para esse teste).
     */
    const val BASE_URL = "https://remedio-certo-eight.vercel.app/"

    /** Falso enquanto o endereço acima ainda for o de exemplo. */
    val configurado: Boolean get() = !BASE_URL.contains("SEU-PROJETO")

    val bulario: BularioApi by lazy {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BularioApi::class.java)
    }
}
