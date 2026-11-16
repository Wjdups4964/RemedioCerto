package com.example.remediocerto.ui.hoje

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remediocerto.R
import com.example.remediocerto.data.model.DoseDoDia
import com.example.remediocerto.databinding.FragmentHojeBinding
import com.example.remediocerto.ui.alarme.AlarmeActivity
import com.example.remediocerto.util.coletar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HojeFragment : Fragment() {

    private var _binding: FragmentHojeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HojeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHojeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DoseDiaAdapter(
            aoTomar = { dose -> confirmarDose(dose) },
            aoAdiar = { dose -> adiarDose(dose) },
        )
        binding.rvDoses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDoses.adapter = adapter

        coletar(viewModel.dosesDeHoje) { doses ->
            adapter.submitList(doses)
            binding.tvVazio.isVisible = doses.isEmpty()
        }

        coletar(viewModel.primeiroNome) { nome ->
            binding.tvTitulo.text =
                if (nome.isBlank()) getString(R.string.hoje_titulo) else getString(R.string.hoje_ola, nome)
        }

        binding.btnTestarAlarme.setOnClickListener {
            startActivity(Intent(requireContext(), AlarmeActivity::class.java))
        }
    }

    // Um toque sem querer não registra nada: a dose só é gravada depois do "Sim, tomei".
    private fun confirmarDose(dose: DoseDoDia) {
        val descricao = listOfNotNull(dose.nome, dose.dose.ifBlank { null }).joinToString(" · ")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.hoje_confirmar_titulo)
            .setMessage(getString(R.string.hoje_confirmar_msg, descricao, dose.hora))
            .setNegativeButton(R.string.hoje_confirmar_nao, null)
            .setPositiveButton(R.string.hoje_confirmar_sim) { _, _ ->
                if (viewModel.tomei(dose)) {
                    Toast.makeText(requireContext(), R.string.alarme_tomei_msg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.hoje_ainda_nao, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun adiarDose(dose: DoseDoDia) {
        if (viewModel.adiar(dose)) {
            val msg = getString(R.string.hoje_adiada, dose.adiamentoMin)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), R.string.hoje_ainda_nao, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
