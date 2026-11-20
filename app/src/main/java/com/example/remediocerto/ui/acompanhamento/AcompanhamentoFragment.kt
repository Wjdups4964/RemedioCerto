package com.example.remediocerto.ui.acompanhamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.databinding.FragmentAcompanhamentoBinding
import com.example.remediocerto.ui.hoje.DoseDiaAdapter
import com.example.remediocerto.util.coletar

/**
 * Visão do familiar: as doses do dia do idoso vinculado, em modo somente leitura.
 * Sem vínculo, a tela explica como vincular (Configurações > Vínculo).
 */
class AcompanhamentoFragment : Fragment() {

    private var _binding: FragmentAcompanhamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AcompanhamentoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAcompanhamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DoseDiaAdapter(somenteLeitura = true)
        binding.rvDoses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoses.adapter = adapter

        coletar(viewModel.dosesDeHoje) {
            adapter.submitList(it)
            atualizarVazio()
        }
        coletar(viewModel.idosoAtual) { idoso ->
            val temIdoso = idoso != null
            binding.tvIdoso.isVisible = temIdoso
            binding.tvSomenteLeitura.isVisible = temIdoso
            binding.rvDoses.isVisible = temIdoso
            // Só diz "você não acompanha ninguém" depois que a lista chegou (evita piscar ao abrir a tela).
            binding.grupoSemVinculo.isVisible = !temIdoso && viewModel.idosos.value != null
            if (idoso != null) binding.tvIdoso.text = getString(R.string.acompanhamento_acompanhando, idoso.idosoNome)
            atualizarVazio()
        }
        coletar(viewModel.idosos) { lista ->
            binding.btnTrocarIdoso.isVisible = (lista?.size ?: 0) > 1
            binding.grupoSemVinculo.isVisible = lista != null && lista.isEmpty()
        }

        binding.btnTrocarIdoso.setOnClickListener {
            val lista = viewModel.idosos.value.orEmpty()
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.acompanhamento_escolher)
                .setItems(lista.map { it.idosoNome }.toTypedArray()) { _, posicao ->
                    viewModel.escolher(lista[posicao].idosoUid)
                }
                .show()
        }
        binding.btnIrParaVinculo.setOnClickListener {
            findNavController().navigate(R.id.action_acompanhamento_to_vinculo)
        }
    }

    private fun atualizarVazio() {
        val vinculado = viewModel.idosoAtual.value != null
        binding.tvSemDoses.isVisible = vinculado && viewModel.dosesDeHoje.value.isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
