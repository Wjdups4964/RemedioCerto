package com.example.remediocerto.ui.vinculo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.data.model.MotivoFalhaVinculo
import com.example.remediocerto.data.model.Perfil
import com.example.remediocerto.data.model.Vinculo
import com.example.remediocerto.data.model.formatarCodigo
import com.example.remediocerto.databinding.FragmentVinculoBinding
import com.example.remediocerto.ui.main.MainActivity
import com.example.remediocerto.util.coletar
import java.text.SimpleDateFormat
import java.util.Locale

/** O idoso gera o código e vê quem o acompanha; o familiar digita o código e vê quem acompanha. */
class VinculoFragment : Fragment() {

    private var _binding: FragmentVinculoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VinculoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVinculoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val perfil = Perfil.valueOf(
            requireActivity().intent.getStringExtra(MainActivity.EXTRA_PERFIL) ?: Perfil.IDOSO.name
        )
        val ehIdoso = perfil == Perfil.IDOSO

        binding.cardGerar.isVisible = ehIdoso
        binding.cardUsar.isVisible = !ehIdoso
        binding.tvListaTitulo.setText(if (ehIdoso) R.string.vinculo_lista_titulo else R.string.vinculo_lista_familiar)
        binding.tvListaVazia.setText(if (ehIdoso) R.string.vinculo_lista_vazia_idoso else R.string.vinculo_lista_vazia_familiar)

        val adapter = VinculoAdapter(
            nomeDe = { if (ehIdoso) it.familiarNome else it.idosoNome },
            rotuloBotao = if (ehIdoso) R.string.vinculo_revogar else R.string.vinculo_deixar,
            aoEncerrar = { confirmarEncerrar(it, ehIdoso) },
        )
        binding.rvVinculos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVinculos.adapter = adapter

        coletar(if (ehIdoso) viewModel.doIdoso else viewModel.doFamiliar) { lista ->
            adapter.submitList(lista)
            binding.tvListaVazia.isVisible = lista.isEmpty()
        }

        binding.btnGerarCodigo.setOnClickListener { viewModel.gerarCodigo() }
        binding.btnVincular.setOnClickListener {
            binding.tilCodigoConvite.error = null
            viewModel.vincular(binding.etCodigoConvite.text?.toString().orEmpty())
        }
        binding.btnCompartilhar.setOnClickListener { compartilharCodigo() }

        coletar(viewModel.ocupado) { ocupado ->
            binding.btnGerarCodigo.isEnabled = !ocupado
            binding.btnVincular.isEnabled = !ocupado
        }
        coletar(viewModel.convite) { convite ->
            binding.tvCodigo.text = convite?.let { formatarCodigo(it.codigo) } ?: getString(R.string.vinculo_codigo_vazio)
            binding.tvValidade.isVisible = convite != null
            binding.btnCompartilhar.isVisible = convite != null
            if (convite != null) {
                val formato = SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR"))
                binding.tvValidade.text = getString(R.string.vinculo_valido_ate, formato.format(convite.expiraEm))
            }
        }
        coletar(viewModel.eventos) { tratar(it) }
    }

    private fun tratar(evento: EventoVinculo) {
        when (evento) {
            EventoVinculo.CodigoGerado -> aviso(R.string.vinculo_codigo_gerado)
            EventoVinculo.ErroAoGerar -> aviso(R.string.vinculo_erro_gerar)
            EventoVinculo.Vinculado -> {
                binding.etCodigoConvite.text = null
                aviso(R.string.vinculo_vinculado)
            }
            is EventoVinculo.FalhaAoVincular ->
                binding.tilCodigoConvite.error = getString(mensagemDeFalha(evento.motivo))
            is EventoVinculo.Encerrado ->
                aviso(if (evento.confirmado) R.string.vinculo_revogado else R.string.vinculo_revogado_pendente)
        }
    }

    private fun mensagemDeFalha(motivo: MotivoFalhaVinculo) = when (motivo) {
        MotivoFalhaVinculo.CODIGO_INVALIDO -> R.string.vinculo_erro_codigo_invalido
        MotivoFalhaVinculo.EXPIRADO -> R.string.vinculo_erro_expirado
        MotivoFalhaVinculo.JA_USADO -> R.string.vinculo_erro_ja_usado
        MotivoFalhaVinculo.JA_VINCULADO -> R.string.vinculo_erro_ja_vinculado
        MotivoFalhaVinculo.PROPRIO_CODIGO -> R.string.vinculo_erro_proprio
        MotivoFalhaVinculo.SEM_INTERNET -> R.string.vinculo_erro_sem_internet
        MotivoFalhaVinculo.OUTRO -> R.string.vinculo_erro_outro
    }

    private fun confirmarEncerrar(vinculo: Vinculo, ehIdoso: Boolean) {
        val nome = if (ehIdoso) vinculo.familiarNome else vinculo.idosoNome
        AlertDialog.Builder(requireContext())
            .setTitle(if (ehIdoso) R.string.vinculo_revogar_titulo else R.string.vinculo_deixar_titulo)
            .setMessage(
                getString(if (ehIdoso) R.string.vinculo_revogar_msg else R.string.vinculo_deixar_msg, nome)
            )
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(if (ehIdoso) R.string.vinculo_revogar else R.string.vinculo_deixar) { _, _ ->
                viewModel.encerrar(vinculo)
            }
            .show()
    }

    private fun compartilharCodigo() {
        val convite = viewModel.convite.value ?: return
        val texto = getString(R.string.vinculo_texto_compartilhar, formatarCodigo(convite.codigo))
        val envio = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto)
        startActivity(Intent.createChooser(envio, getString(R.string.vinculo_compartilhar)))
    }

    private fun aviso(texto: Int) = Toast.makeText(requireContext(), texto, Toast.LENGTH_LONG).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
