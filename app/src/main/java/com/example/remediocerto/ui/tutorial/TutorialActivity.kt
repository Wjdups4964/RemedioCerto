package com.example.remediocerto.ui.tutorial

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.remediocerto.R
import com.example.remediocerto.databinding.ActivityTutorialBinding
import com.example.remediocerto.util.TamanhoFonte
import com.example.remediocerto.util.Tutorial
import com.example.remediocerto.util.aplicarInsetsDoSistema

/** Um passo do tutorial: um emoji, um título e um texto curto. */
private data class Passo(val emoji: String, @StringRes val titulo: Int, @StringRes val texto: Int)

private val PASSOS = listOf(
    Passo("👋", R.string.tutorial_titulo_1, R.string.tutorial_texto_1),
    Passo("💊", R.string.tutorial_titulo_2, R.string.tutorial_texto_2),
    Passo("✅", R.string.tutorial_titulo_3, R.string.tutorial_texto_3),
    Passo("⏰", R.string.tutorial_titulo_4, R.string.tutorial_texto_4),
    Passo("📋", R.string.tutorial_titulo_5, R.string.tutorial_texto_5),
    Passo("👥", R.string.tutorial_titulo_6, R.string.tutorial_texto_6),
    Passo("⚙️", R.string.tutorial_titulo_7, R.string.tutorial_texto_7),
)

/**
 * Tutorial rápido, em passos. Abre sozinho na primeira vez que a conta entra neste celular e pode ser
 * revisto em Configurações > Ver tutorial.
 */
class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private var passo = 0

    // Aplica o tamanho de letra escolhido (padrão ou modo de acessibilidade).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(TamanhoFonte.aplicar(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsDoSistema()

        // Marca já ao abrir: se a pessoa sair no meio, ele não volta sozinho. Para rever, é só abrir em Configurações.
        Tutorial.marcarVisto(this)

        passo = savedInstanceState?.getInt(CHAVE_PASSO) ?: 0

        binding.btnProximo.setOnClickListener {
            if (passo == PASSOS.lastIndex) finish() else mostrar(passo + 1)
        }
        binding.btnAnterior.setOnClickListener { mostrar(passo - 1) }
        binding.btnPular.setOnClickListener { finish() }

        // O botão "voltar" do celular volta um passo; no primeiro, fecha o tutorial.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (passo > 0) mostrar(passo - 1) else finish()
            }
        })

        mostrar(passo)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHAVE_PASSO, passo)
    }

    private fun mostrar(novo: Int) {
        passo = novo.coerceIn(0, PASSOS.lastIndex)
        val atual = PASSOS[passo]
        val ultimo = passo == PASSOS.lastIndex

        binding.tvPasso.text = getString(R.string.tutorial_passo, passo + 1, PASSOS.size)
        binding.tvEmoji.text = atual.emoji
        binding.tvTitulo.setText(atual.titulo)
        binding.tvTexto.setText(atual.texto)

        binding.btnPular.isVisible = !ultimo
        binding.btnAnterior.isVisible = passo > 0
        binding.btnProximo.setText(if (ultimo) R.string.tutorial_concluir else R.string.tutorial_proximo)
    }

    private companion object {
        const val CHAVE_PASSO = "passo"
    }
}
