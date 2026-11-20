package com.example.remediocerto.ui.medicamentos

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remediocerto.R
import com.example.remediocerto.data.model.MotivoFalhaBula
import com.example.remediocerto.data.model.ResultadoBula
import com.example.remediocerto.databinding.FragmentDetalhesMedicamentoBinding
import com.example.remediocerto.util.coletar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class DetalhesMedicamentoFragment : Fragment() {

    private var _binding: FragmentDetalhesMedicamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalhesMedicamentoViewModel by viewModels()

    // Nome do medicamento na tela; é com ele que se consulta a bula.
    private var nomeAtual: String = ""

    // Endereço da bula oficial da última consulta; o botão "Abrir a bula oficial" só aparece com ele.
    private var urlBulaOficial: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetalhesMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = arguments?.getString(ARG_MEDICAMENTO_ID)
        if (id == null) {
            findNavController().popBackStack()
            return
        }

        coletar(viewModel.medicamento(id)) { medicamento ->
            if (medicamento != null) {
                nomeAtual = medicamento.nome
                binding.tvNome.text = medicamento.nome
                binding.tvDoseHorarios.text = medicamento.detalhes(requireContext())
            }
        }

        coletar(viewModel.bula) { mostrarBula(it) }
        binding.btnConsultarBula.setOnClickListener { viewModel.consultarBula(nomeAtual) }
        binding.btnBulaOficial.setOnClickListener { abrirBulaOficial() }
        binding.btnEditar.setOnClickListener {
            findNavController().navigate(R.id.action_detalhes_to_cadastro, bundleOf(ARG_MEDICAMENTO_ID to id))
        }
        binding.btnApagar.setOnClickListener { confirmarExclusao(id) }
    }

    private fun mostrarBula(estado: EstadoBula) {
        binding.btnConsultarBula.isEnabled = estado !is EstadoBula.Consultando
        urlBulaOficial = when (estado) {
            // Só abre endereços seguros (https) vindos do servidor.
            is EstadoBula.Pronta -> estado.resultado.bula.urlBulaOficial.takeIf { it.startsWith("https://") }
            // Remédio fora do nosso catálogo: abre a busca do bulário da Anvisa já com o nome preenchido.
            is EstadoBula.Falha ->
                if (estado.resultado.motivo == MotivoFalhaBula.NAO_ENCONTRADO && nomeAtual.isNotBlank()) {
                    urlBuscaAnvisa(nomeAtual)
                } else {
                    null
                }
            else -> null
        }
        binding.btnBulaOficial.isVisible = urlBulaOficial != null
        when (estado) {
            EstadoBula.Inicial -> Unit // continua o texto de orientação que a tela já mostra
            EstadoBula.Consultando -> binding.tvInfoBula.setText(R.string.detalhes_bula_consultando)
            is EstadoBula.Pronta -> binding.tvInfoBula.text = textoDaBula(estado.resultado)
            is EstadoBula.Falha -> binding.tvInfoBula.setText(
                when (estado.resultado.motivo) {
                    MotivoFalhaBula.SEM_INTERNET -> R.string.bula_erro_sem_internet
                    MotivoFalhaBula.NAO_ENCONTRADO -> R.string.bula_erro_nao_encontrado
                    MotivoFalhaBula.SERVIDOR -> R.string.bula_erro_servidor
                    MotivoFalhaBula.NAO_CONFIGURADO -> R.string.bula_erro_nao_configurado
                }
            )
        }
    }

    private fun textoDaBula(resultado: ResultadoBula.Sucesso): CharSequence {
        val bula = resultado.bula
        val texto = SpannableStringBuilder()
        if (resultado.doCache) {
            val quando = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(Date(resultado.salvoEm))
            texto.append(getString(R.string.bula_do_cache, quando)).append("\n\n")
        }
        val inicioTitulo = texto.length
        texto.append(bula.nome)
        texto.setSpan(StyleSpan(Typeface.BOLD), inicioTitulo, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        texto.append("\n").append(getString(R.string.bula_principio, bula.principioAtivo))
        texto.append("\n").append(getString(R.string.bula_classe, bula.classe))
        texto.append("\n\n").append(getString(R.string.bula_indicacao, bula.indicacao))
        bula.observacao?.let { texto.append("\n\n").append(getString(R.string.bula_observacao, it)) }
        if (bula.aviso.isNotBlank()) texto.append("\n\n").append(bula.aviso)
        if (bula.atualizadoEm.isNotBlank()) {
            texto.append("\n").append(getString(R.string.bula_revisado, dataBrasileira(bula.atualizadoEm)))
        }
        return texto
    }

    private fun urlBuscaAnvisa(nome: String) =
        "https://consultas.anvisa.gov.br/#/bulario/q/?nomeProduto=" + Uri.encode(nome.trim())

    private fun abrirBulaOficial() {
        val url = urlBulaOficial ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.detalhe_abrir_bula_erro, Toast.LENGTH_LONG).show()
        }
    }

    /** "2026-11-20" vira "20/11/2026"; se vier em outro formato, mostra como veio. */
    private fun dataBrasileira(iso: String): String =
        runCatching { LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(iso)

    private fun confirmarExclusao(id: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.detalhes_apagar_titulo)
            .setMessage(R.string.detalhes_apagar_msg)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.detalhes_apagar_sim) { _, _ ->
                viewModel.apagar(id)
                Toast.makeText(requireContext(), R.string.detalhes_apagado, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
