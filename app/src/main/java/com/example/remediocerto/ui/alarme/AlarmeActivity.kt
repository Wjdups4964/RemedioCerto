package com.example.remediocerto.ui.alarme

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remediocerto.R
import com.example.remediocerto.databinding.ActivityAlarmeBinding
import com.example.remediocerto.util.aplicarInsetsDoSistema

/**
 * Tela de alarme: nome, dose e botões grandes "Tomei" e "Adiar" (RF09).
 * Abre sobre a tela bloqueada (declarado no manifest). Hoje mostra dados de
 * exemplo; depois recebe o id da dose e grava o registro (RF14).
 */
class AlarmeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        binding.tvNomeMedicamento.text = intent.getStringExtra(EXTRA_NOME) ?: "Losartana"
        binding.tvDose.text = intent.getStringExtra(EXTRA_DOSE) ?: "50 mg · 1 comprimido"

        binding.btnTomei.setOnClickListener {
            Toast.makeText(this, R.string.alarme_tomei_msg, Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.btnAdiar.setOnClickListener {
            Toast.makeText(this, R.string.alarme_adiar_msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_NOME = "nome"
        const val EXTRA_DOSE = "dose"
    }
}
