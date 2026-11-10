package com.example.fincontrol

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fincontrol.data.model.Tip

/**
 * Fragmento de Dicas Financeiras e Educação Orçamentária.
 *
 * Apresenta o feed com os 5 artigos e notícias idênticos ao protótipo web original.
 */
class TipsFragment : Fragment(R.layout.fragment_tips) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTips = view.findViewById<RecyclerView>(R.id.rv_tips)

        // Conteúdo fiel ao protótipo web original
        val dicas = listOf(
            Tip(
                titulo = "Regra 50-30-20: como dividir seu salário",
                resumo = "Organize sua renda entre necessidades, desejos e planos para o futuro.",
                fonte = "Vida Financeira",
                tempo = "há 2 horas",
                tag = "Dica",
                corTagHex = "emerald",
                icone = "◒"
            ),
            Tip(
                titulo = "Cuidado com o parcelamento sem juros",
                resumo = "Parcelas pequenas também ocupam espaço no orçamento dos próximos meses.",
                fonte = "Seu Dinheiro",
                tempo = "há 5 horas",
                tag = "Atenção",
                corTagHex = "orange",
                icone = "⌁"
            ),
            Tip(
                titulo = "Como montar sua reserva de emergência",
                resumo = "Comece aos poucos e priorize um valor que possa resgatar quando precisar.",
                fonte = "Finanças na Prática",
                tempo = "ontem",
                tag = "Dica",
                corTagHex = "sky",
                icone = "◈"
            ),
            Tip(
                titulo = "Selic: o que muda no seu bolso",
                resumo = "A taxa básica influencia o crédito, os investimentos e o custo de vida.",
                fonte = "Radar Econômico",
                tempo = "ontem",
                tag = "Notícia",
                corTagHex = "violet",
                icone = "↗"
            ),
            Tip(
                titulo = "Dólar hoje: dicas para compras internacionais",
                resumo = "Considere IOF, conversão e tarifas antes de fechar a compra.",
                fonte = "Bolso Consciente",
                tempo = "há 2 dias",
                tag = "Investimentos",
                corTagHex = "amber",
                icone = "$"
            )
        )

        rvTips.layoutManager = LinearLayoutManager(requireContext())
        rvTips.adapter = TipAdapter(dicas)
    }
}
