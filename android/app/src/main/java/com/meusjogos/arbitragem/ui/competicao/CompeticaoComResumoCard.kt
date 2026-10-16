package com.meusjogos.arbitragem.ui.competicao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.logic.CompeticaoComResumo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.ui.components.ProportionBar
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors
import kotlin.math.roundToInt

/**
 * Card de resumo financeiro de uma competição (VERSÃO 1.3/1.4) — compartilhado pelas listas
 * "Competições encerradas a receber" e "Competições em aberto a receber" para que as duas sigam
 * EXATAMENTE o mesmo padrão visual (seção 1 da V1.4), em vez de duplicar o layout. [statusTexto]/
 * [statusCor] e [rotuloData] são a única coisa que muda entre os dois módulos.
 */
@Composable
fun CompeticaoComResumoCard(
    item: CompeticaoComResumo,
    statusTexto: String,
    statusCor: Color,
    rotuloData: String?,
    onVerCompeticao: () -> Unit,
) {
    val coresStatus = LocalStatusColors.current
    val competicao = item.competicao
    val resumo = item.resumo

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(competicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            competicao.subtitulo?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                text = statusTexto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statusCor,
                modifier = Modifier.padding(top = 8.dp),
            )
            rotuloData?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LinhaInfoFinanceira("Jogos", resumo.totalJogos.toString())
            resumo.porFuncao.forEach { (funcao, quantidade) ->
                LinhaInfoFinanceira("  • $funcao", quantidade.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinhaInfoFinanceira("Jogos recebidos", resumo.jogosRecebidos.toString(), coresStatus.recebido)
            LinhaInfoFinanceira("Jogos pendentes", resumo.jogosPendentes.toString(), coresStatus.aReceber)

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LinhaInfoFinanceira("Valor total", CurrencyUtils.formatar(resumo.valorTotalCentavos))
            LinhaInfoFinanceira("Valor recebido", CurrencyUtils.formatar(resumo.valorRecebidoCentavos), coresStatus.recebido)
            LinhaInfoFinanceira("VALOR A RECEBER", CurrencyUtils.formatar(resumo.valorPendenteCentavos), coresStatus.aReceber)

            ProportionBar(
                fracaoRecebida = resumo.percentualRecebido,
                corRecebido = coresStatus.recebido,
                corAReceber = coresStatus.aReceber,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "${(resumo.percentualRecebido * 100).roundToInt()}% já recebido",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            Button(onClick = onVerCompeticao, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                Text("VER COMPETIÇÃO")
            }
        }
    }
}

@Composable
fun LinhaInfoFinanceira(rotulo: String, valor: String, cor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = cor)
    }
}
