package com.example.remediocerto.ui.entrada

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.data.repository.AuthRepository
import com.example.remediocerto.databinding.ActivityEntradaBinding
import com.example.remediocerto.ui.main.MainActivity
import com.example.remediocerto.util.aplicarInsetsDoSistema

class EntradaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntradaBinding
    private val authRepository = AuthRepository()
    private var loginPronto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntradaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        binding.btnIdoso.setOnClickListener { entrar(Perfil.IDOSO) }
        binding.btnFamiliar.setOnClickListener { entrar(Perfil.FAMILIAR) }

        entrarNoFirebase()
    }

    /** Login anônimo. Na primeira vez precisa de internet; depois o usuário fica salvo no aparelho. */
    private fun entrarNoFirebase() {
        authRepository.entrarAnonimo(
            onSucesso = { loginPronto = true },
            onErro = {
                Toast.makeText(this, R.string.entrada_erro_login, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun entrar(perfil: Perfil) {
        if (!binding.checkAviso.isChecked) {
            Toast.makeText(this, R.string.entrada_aceite_obrigatorio, Toast.LENGTH_SHORT).show()
            return
        }
        if (!loginPronto) {
            // Login ainda não terminou ou falhou: tenta de novo e pede para tocar outra vez.
            Toast.makeText(this, R.string.entrada_conectando, Toast.LENGTH_SHORT).show()
            entrarNoFirebase()
            return
        }
        authRepository.salvarPerfil(perfil)
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_PERFIL, perfil.name)
        startActivity(intent)
        finish()
    }
}