package com.example.remediocerto.ui.alarme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remediocerto.R
import com.example.remediocerto.alarm.AcaoDose
import com.example.remediocerto.alarm.AgendaLocal
import com.example.remediocerto.alarm.adiamentoMin
import com.example.remediocerto.alarm.lerLembrete
import com.example.remediocerto.databinding.ActivityAlarmeBinding
import com.example.remediocerto.util.aplicarInsetsDoSistema

/**
 * Tela do alarme, mostrada por cima da tela bloqueada na hora de tomar o remédio.
 * Sem dados de um medicamento no Intent, funciona só como demonstração.
 */
class AlarmeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aparecer com a tela bloqueada e acender a tela está declarado no AndroidManifest.xml.
        binding = ActivityAlarmeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        mostrarDados()
        binding.btnTomei.setOnClickListener { tomei() }
        binding.btnAdiar.setOnClickListener { adiar() }
    }

    // Em alguns celulares a tela do alarme só aparece depois de desbloquear. Se a pessoa já respondeu
    // pelos botões da notificação, não há mais nada a perguntar: fecha sozinha.
    override fun onResume() {
        super.onResume()
        val lembrete = intent.lerLembrete() ?: return
        if (AgendaLocal.foiRespondido(this, lembrete.codigo)) finish()
    }

    // Outro alarme chegou com a tela do alarme já aberta: mostra o novo.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mostrarDados()
    }

    private fun mostrarDados() {
        val lembrete = intent.lerLembrete()
        if (lembrete == null) { // demonstração
            binding.tvNomeMedicamento.text = "Losartana"
            binding.tvDose.text = "50 mg · 1 comprimido"
            return
        }
        binding.tvNomeMedicamento.text = lembrete.nome
        binding.tvDose.text = if (lembrete.dose.isBlank()) {
            getString(R.string.notif_texto_sem_dose, lembrete.hora)
        } else {
            getString(R.string.notif_texto_com_dose, lembrete.dose, lembrete.hora)
        }
    }

    private fun tomei() {
        intent.lerLembrete()?.let { AcaoDose.tomei(this, it) }
        Toast.makeText(this, R.string.alarme_tomei_msg, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun adiar() {
        val minutos = intent.adiamentoMin()
        intent.lerLembrete()?.let { AcaoDose.adiar(this, it, minutos) }
        Toast.makeText(this, getString(R.string.alarme_adiar_msg_min, minutos), Toast.LENGTH_SHORT).show()
        finish()
    }
}
