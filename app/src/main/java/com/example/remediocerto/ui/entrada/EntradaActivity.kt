package com.example.remediocerto.ui.entrada

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remediocerto.R
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.databinding.ActivityEntradaBinding
import com.example.remediocerto.ui.main.MainActivity
import com.example.remediocerto.util.aplicarInsetsDoSistema

/**
 * Tela de entrada: aviso médico (RN02) e escolha do perfil.
 * Login e cadastro (RF25) entram na etapa do Firebase.
 */
class EntradaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntradaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntradaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        binding.btnIdoso.setOnClickListener { entrar(Perfil.IDOSO) }
        binding.btnFamiliar.setOnClickListener { entrar(Perfil.FAMILIAR) }
    }

    private fun entrar(perfil: Perfil) {
        if (!binding.checkAviso.isChecked) {
            Toast.makeText(this, R.string.entrada_aceite_obrigatorio, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_PERFIL, perfil.name)
        startActivity(intent)
        finish()
    }
}
