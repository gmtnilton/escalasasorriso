package com.meusjogos.arbitragem.data.backup

import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Backup e restauração completos da base de jogos (REGRA 19), em um único
 * arquivo JSON — simples de conferir, versionado, e fácil de restaurar em
 * outro aparelho.
 */
class BackupManager(private val repository: JogoRepository) {

    companion object {
        const val VERSAO_BACKUP = 2
        const val EXTENSAO_ARQUIVO = "json"
        const val MIME_TYPE = "application/json"

        /** "meus-jogos-arbitragem-backup-2026-09-03.json" */
        fun nomeArquivoSugerido(): String =
            "meus-jogos-arbitragem-backup-${LocalDate.now()}.$EXTENSAO_ARQUIVO"
    }

    suspend fun gerarBackup(): String = withContext(Dispatchers.Default) {
        val jogos = repository.listarTudoUmaVez()
        jogosParaJson(jogos)
    }

    /** Restaura a partir do conteúdo de um arquivo de backup; retorna quantos jogos foram restaurados. */
    suspend fun restaurar(conteudoJson: String): Int = withContext(Dispatchers.Default) {
        val jogos = jsonParaJogos(conteudoJson)
        repository.restaurarBackup(jogos)
        jogos.size
    }

    private fun jogosParaJson(jogos: List<Jogo>): String {
        val raiz = JSONObject()
        raiz.put("versaoBackup", VERSAO_BACKUP)
        raiz.put("exportadoEm", Instant.now().toString())
        raiz.put("totalJogos", jogos.size)

        val array = JSONArray()
        for (jogo in jogos) array.put(jogo.paraJson())
        raiz.put("jogos", array)

        return raiz.toString(2)
    }

    private fun jsonParaJogos(conteudo: String): List<Jogo> {
        val raiz = JSONObject(conteudo)
        val array = raiz.optJSONArray("jogos") ?: throw BackupInvalidoException(
            "Arquivo de backup inválido: não contém a lista \"jogos\".",
        )
        return (0 until array.length()).map { indice -> array.getJSONObject(indice).paraJogo() }
    }

    private fun Jogo.paraJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("data", data.toString())
        put("horario", horario?.toString() ?: JSONObject.NULL)
        put("competicao", competicao ?: JSONObject.NULL)
        put("categoria", categoria ?: JSONObject.NULL)
        put("equipeMandante", equipeMandante ?: JSONObject.NULL)
        put("equipeVisitante", equipeVisitante ?: JSONObject.NULL)
        put("cidade", cidade ?: JSONObject.NULL)
        put("estadio", estadio ?: JSONObject.NULL)
        put("funcao", funcao ?: JSONObject.NULL)
        put("valorCentavos", valorCentavos)
        put("statusPagamento", statusPagamento.name)
        put("dataRecebimento", dataRecebimento?.toString() ?: JSONObject.NULL)
        put("observacoes", observacoes ?: JSONObject.NULL)
        put("dataCriacao", dataCriacao.toString())
        put("dataAtualizacao", dataAtualizacao.toString())
    }

    private fun JSONObject.paraJogo(): Jogo = Jogo(
        id = optLong("id", 0L),
        data = LocalDate.parse(getString("data")),
        horario = optNullableString("horario")?.let { LocalTime.parse(it) },
        competicao = optNullableString("competicao"),
        categoria = optNullableString("categoria"),
        equipeMandante = optNullableString("equipeMandante"),
        equipeVisitante = optNullableString("equipeVisitante"),
        // "cidade"/"estadio" (formato atual) ou, em backups antigos deste
        // app (versão 1, antes da separação cidade/estádio), "local".
        cidade = optNullableString("cidade"),
        estadio = optNullableString("estadio") ?: optNullableString("local"),
        funcao = optNullableString("funcao"),
        valorCentavos = getLong("valorCentavos"),
        statusPagamento = StatusPagamento.valueOf(getString("statusPagamento")),
        dataRecebimento = optNullableString("dataRecebimento")?.let { LocalDate.parse(it) },
        observacoes = optNullableString("observacoes"),
        dataCriacao = optNullableString("dataCriacao")?.let { Instant.parse(it) } ?: Instant.now(),
        dataAtualizacao = optNullableString("dataAtualizacao")?.let { Instant.parse(it) } ?: Instant.now(),
    )

    private fun JSONObject.optNullableString(chave: String): String? =
        if (isNull(chave)) null else optString(chave, null)?.takeIf(String::isNotBlank)
}

class BackupInvalidoException(mensagem: String) : Exception(mensagem)
