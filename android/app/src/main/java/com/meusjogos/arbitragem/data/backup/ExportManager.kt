package com.meusjogos.arbitragem.data.backup

import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Exportação para CSV (REGRA 19), para abrir em Excel/Planilhas Google.
 * Usa ";" como separador — padrão do Excel em configuração de idioma
 * português (evita conflito com a vírgula decimal dos valores em reais).
 */
class ExportManager(private val repository: JogoRepository) {

    companion object {
        const val EXTENSAO_ARQUIVO = "csv"
        const val MIME_TYPE = "text/csv"

        fun nomeArquivoSugerido(): String =
            "meus-jogos-arbitragem-${LocalDate.now()}.$EXTENSAO_ARQUIVO"

        private val CABECALHO = listOf(
            "Data", "Horário", "Competição", "Modalidade", "Categoria", "Equipe mandante", "Equipe visitante",
            "Cidade", "Estádio/Ginásio", "Função", "Valor", "Status", "Data do recebimento", "Observações",
        )
    }

    suspend fun gerarCsv(): String = withContext(Dispatchers.Default) {
        val jogos = repository.listarTudoUmaVez()
        jogosParaCsv(jogos)
    }

    private fun jogosParaCsv(jogos: List<Jogo>): String {
        val builder = StringBuilder()
        builder.append('﻿') // BOM, para acentos abrirem corretos no Excel
        builder.append(CABECALHO.joinToString(";") { it.csv() }).append("\r\n")

        for (jogo in jogos) {
            builder.append(jogo.paraLinhaCsv()).append("\r\n")
        }
        return builder.toString()
    }

    private fun Jogo.paraLinhaCsv(): String {
        val status = if (statusPagamento == StatusPagamento.RECEBIDO) "RECEBIDO" else "A RECEBER"
        val campos = listOf(
            DateUtils.formatarData(data),
            horario?.let { DateUtils.formatarHora(it) } ?: "",
            competicao ?: "",
            modalidade ?: "",
            categoria ?: "",
            equipeMandante ?: "",
            equipeVisitante ?: "",
            cidade ?: "",
            estadio ?: "",
            funcao ?: "",
            CurrencyUtils.formatar(valorCentavos),
            status,
            dataRecebimento?.let { DateUtils.formatarData(it) } ?: "",
            observacoes ?: "",
        )
        return campos.joinToString(";") { it.csv() }
    }

    /** Escapa um campo para CSV: aspas quando contém ";", quebra de linha ou aspas. */
    private fun String.csv(): String {
        val precisaAspas = contains(';') || contains('"') || contains('\n') || contains('\r')
        val escapado = replace("\"", "\"\"")
        return if (precisaAspas) "\"$escapado\"" else escapado
    }
}
