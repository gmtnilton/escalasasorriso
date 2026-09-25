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
import kotlin.math.roundToLong

/**
 * Backup e restauração completos da base de jogos (REGRA 19), em um único
 * arquivo JSON — simples de conferir, versionado, e fácil de restaurar em
 * outro aparelho. Também importa (somando, sem apagar nada) backups de
 * outro sistema de controle de jogos, no formato "records"/"qty"/"value".
 */
class BackupManager(private val repository: JogoRepository) {

    companion object {
        const val VERSAO_BACKUP = 3
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

    /**
     * Restaura um backup FEITO POR ESTE APP: substitui toda a base pelo
     * conteúdo do arquivo. Retorna quantos jogos foram restaurados.
     */
    suspend fun restaurar(conteudoJson: String): Int = withContext(Dispatchers.Default) {
        val raiz = JSONObject(conteudoJson)
        if (!raiz.has("jogos")) {
            throw BackupInvalidoException(
                "Esse arquivo não parece um backup deste app. Se ele vem de outro " +
                    "sistema de controle de jogos, use \"Importar de outro sistema\".",
            )
        }
        val jogos = jsonParaJogosFormatoProprio(raiz)
        repository.restaurarBackup(jogos)
        jogos.size
    }

    /**
     * Importa jogos de um arquivo .json de OUTRO sistema (formato
     * "records": data/cidade/modalidade/competição/estádio/quantidade de
     * partidas/valor por partida/pago) — SOMA aos jogos já cadastrados,
     * nunca apaga nada. Cada registro com "qty" > 1 vira um jogo por
     * partida, igual ao cadastro em lote do app. Retorna quantos jogos
     * foram criados.
     */
    suspend fun importarDeOutroSistema(conteudoJson: String): Int = withContext(Dispatchers.Default) {
        val raiz = JSONObject(conteudoJson)
        if (!raiz.has("records")) {
            throw BackupInvalidoException(
                "Não reconheci o formato desse arquivo (esperava uma lista \"records\").",
            )
        }
        val jogos = jsonParaJogosFormatoRecords(raiz)
        repository.importarJogos(jogos)
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

    private fun jsonParaJogosFormatoProprio(raiz: JSONObject): List<Jogo> {
        val array = raiz.getJSONArray("jogos")
        return (0 until array.length()).map { indice -> array.getJSONObject(indice).paraJogoFormatoProprio() }
    }

    private fun Jogo.paraJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("data", data.toString())
        put("horario", horario?.toString() ?: JSONObject.NULL)
        put("competicao", competicao ?: JSONObject.NULL)
        put("modalidade", modalidade ?: JSONObject.NULL)
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

    private fun JSONObject.paraJogoFormatoProprio(): Jogo = Jogo(
        id = optLong("id", 0L),
        data = LocalDate.parse(getString("data")),
        horario = optNullableString("horario")?.let { LocalTime.parse(it) },
        competicao = optNullableString("competicao"),
        modalidade = optNullableString("modalidade"),
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

    /**
     * Formato do outro sistema: {"records": [{id, date, city, modality,
     * competition, venue, qty, value, paid}, ...]}. "qty" é a quantidade de
     * partidas daquele registro e "value" o valor de CADA partida (em
     * reais, não centavos) — mesma lógica do cadastro em lote deste app:
     * cada partida vira um jogo independente.
     */
    private fun jsonParaJogosFormatoRecords(raiz: JSONObject): List<Jogo> {
        val array = raiz.getJSONArray("records")
        val jogos = mutableListOf<Jogo>()
        for (indice in 0 until array.length()) {
            jogos += array.getJSONObject(indice).paraJogosFormatoRecords()
        }
        return jogos
    }

    private fun JSONObject.paraJogosFormatoRecords(): List<Jogo> {
        val data = LocalDate.parse(getString("date"))
        val pago = optBoolean("paid", false)
        val valorPorPartidaCentavos = (optDouble("value", 0.0) * 100).roundToLong()
        val quantidade = optInt("qty", 1).coerceAtLeast(1)

        val jogoBase = Jogo(
            data = data,
            cidade = optNullableString("city"),
            modalidade = optNullableString("modality"),
            competicao = optNullableString("competition"),
            estadio = optNullableString("venue"),
            valorCentavos = valorPorPartidaCentavos,
            statusPagamento = if (pago) StatusPagamento.RECEBIDO else StatusPagamento.A_RECEBER,
            dataRecebimento = if (pago) data else null,
        )
        return List(quantidade) { jogoBase }
    }

    private fun JSONObject.optNullableString(chave: String): String? =
        if (isNull(chave)) null else optString(chave, null)?.takeIf(String::isNotBlank)
}

class BackupInvalidoException(mensagem: String) : Exception(mensagem)
