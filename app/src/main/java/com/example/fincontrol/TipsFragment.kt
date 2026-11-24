package com.example.fincontrol

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.api.TipsApiService
import com.example.fincontrol.data.model.Tip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragmento de Dicas Financeiras e Cotações em Tempo Real.
 *
 * Conectado a APIs externas de economia (AwesomeAPI e BrasilAPI) para trazer
 * taxas oficiais (Selic, IPCA) e cotações cambiais (Dólar, Euro, Bitcoin),
 * com fallback imediato para as dicas educativas recomendadas.
 */
class TipsFragment : Fragment(R.layout.fragment_tips) {

    private lateinit var adapter: TipAdapter
    private lateinit var tvApiStatus: TextView
    private lateinit var progressLoading: ProgressBar
    private lateinit var btnRefresh: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTips = view.findViewById<RecyclerView>(R.id.rv_tips)
        tvApiStatus = view.findViewById(R.id.tv_api_status)
        progressLoading = view.findViewById(R.id.progress_tips_loading)
        btnRefresh = view.findViewById(R.id.btn_refresh_tips)

        // Configuração inicial com dicas educativas para renderização instantânea
        val dicasIniciais = TipsApiService.getDicasEducativas()
        adapter = TipAdapter(dicasIniciais)
        adapter.onTipClickListener = { tip ->
            abrirDetalhesOuLink(tip)
        }

        rvTips.layoutManager = LinearLayoutManager(requireContext())
        rvTips.adapter = adapter

        btnRefresh.setOnClickListener {
            buscarDicasDaApi()
        }

        // Busca dados atualizados da API em segundo plano
        buscarDicasDaApi()
    }

    private fun buscarDicasDaApi() {
        progressLoading.visibility = View.VISIBLE
        tvApiStatus.text = "● Atualizando dados das APIs..."

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val dicas = try {
                TipsApiService.carregarDicasComApi()
            } catch (e: Exception) {
                TipsApiService.getDicasEducativas()
            }

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext

                adapter.atualizarDicas(dicas)
                progressLoading.visibility = View.GONE

                val temApi = dicas.any { it.url != null }
                if (temApi) {
                    tvApiStatus.text = "● Conectado: Cotações & Taxas em Tempo Real"
                } else {
                    tvApiStatus.text = "● Modo Offline: Exibindo dicas salvas"
                }
            }
        }
    }

    private fun abrirDetalhesOuLink(tip: Tip) {
        if (!tip.url.isNullOrEmpty()) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tip.url))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Não foi possível abrir o link externo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(tip.titulo)
                .setMessage("${tip.resumo}\n\nFonte: ${tip.fonte} (${tip.tempo})")
                .setPositiveButton("Entendi", null)
                .show()
        }
    }
}
