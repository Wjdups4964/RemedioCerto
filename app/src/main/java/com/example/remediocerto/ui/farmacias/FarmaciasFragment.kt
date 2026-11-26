package com.example.remediocerto.ui.farmacias

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentFarmaciasBinding

/**
 * Em vez de desenhar um mapa dentro do app, abre o aplicativo de mapas do celular
 * já pesquisando "farmácia". O próprio Maps usa a localização do aparelho, então o app
 * não precisa pedir permissão de localização nem de chave de API.
 */
class FarmaciasFragment : Fragment() {

    private var _binding: FragmentFarmaciasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFarmaciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAbrirMapas.setOnClickListener { abrirMapas() }
    }

    private fun abrirMapas() {
        val busca = Uri.encode(getString(R.string.farmacias_busca))
        val geo = Uri.parse("geo:0,0?q=$busca")
        val tentativas = listOf(
            // 1º: o Google Maps, se estiver instalado.
            Intent(Intent.ACTION_VIEW, geo).setPackage(PACOTE_GOOGLE_MAPS),
            // 2º: qualquer outro app de mapas que entenda "geo:".
            Intent(Intent.ACTION_VIEW, geo),
            // 3º: o navegador, com o endereço da busca no Google Maps.
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$busca"))
        )
        for (intencao in tentativas) {
            try {
                startActivity(intencao)
                return
            } catch (_: ActivityNotFoundException) {
                // tenta a próxima opção
            }
        }
        Toast.makeText(requireContext(), R.string.farmacias_sem_app, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val PACOTE_GOOGLE_MAPS = "com.google.android.apps.maps"
    }
}
