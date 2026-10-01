package com.meusjogos.arbitragem.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meusjogos.arbitragem.core.logic.agruparCompeticoesEquivalentes
import com.meusjogos.arbitragem.core.logic.chaveNormalizadaCompeticao
import com.meusjogos.arbitragem.core.model.StatusCompeticao

@Database(
    entities = [JogoEntity::class, ReciboEntity::class, CompeticaoEntity::class, ReciboCompeticaoEntity::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogoDao(): JogoDao

    abstract fun reciboDao(): ReciboDao

    abstract fun competicaoDao(): CompeticaoDao

    abstract fun reciboCompeticaoDao(): ReciboCompeticaoDao

    companion object {
        private const val NOME_BANCO = "meus_jogos_arbitragem.db"

        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOME_BANCO,
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { instancia = it }
            }
    }
}

/**
 * v1 -> v2: separa o antigo campo único "local" em "cidade" (predefinida +
 * editável, REGRA nova do usuário) e "estadio" (nome do estádio/ginásio).
 *
 * Reconstrói a tabela (SQLite não tem RENAME/DROP COLUMN confiável em todas
 * as versões do Android) preservando todos os jogos já cadastrados: o valor
 * antigo de "local" vira o novo "estadio", e "cidade" começa vazia — o
 * usuário completa depois, editando o jogo.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS jogos_new (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                data INTEGER NOT NULL,
                horario TEXT,
                competicao TEXT,
                categoria TEXT,
                equipe_mandante TEXT,
                equipe_visitante TEXT,
                cidade TEXT,
                estadio TEXT,
                funcao TEXT,
                valor_centavos INTEGER NOT NULL,
                status_pagamento TEXT NOT NULL,
                data_recebimento INTEGER,
                observacoes TEXT,
                data_criacao INTEGER NOT NULL,
                data_atualizacao INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO jogos_new (
                id, data, horario, competicao, categoria, equipe_mandante, equipe_visitante,
                cidade, estadio, funcao, valor_centavos, status_pagamento, data_recebimento,
                observacoes, data_criacao, data_atualizacao
            )
            SELECT
                id, data, horario, competicao, categoria, equipe_mandante, equipe_visitante,
                NULL, local, funcao, valor_centavos, status_pagamento, data_recebimento,
                observacoes, data_criacao, data_atualizacao
            FROM jogos
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE jogos")
        db.execSQL("ALTER TABLE jogos_new RENAME TO jogos")
    }
}

/** v2 -> v3: adiciona "modalidade" (Futebol de Campo, Society, Futsal...) — simples ADD COLUMN, nada é perdido. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE jogos ADD COLUMN modalidade TEXT")
    }
}

/**
 * v3 -> v4 (VERSÃO 1.1): cria a tabela "recibos" (recibo de pagamento em PDF
 * por jogo já recebido) — tabela nova, não mexe em "jogos" nem em nenhum
 * dado já existente. Um jogo só pode ter um recibo (índice único em
 * jogo_id), reaproveitado sempre que o recibo é gerado de novo.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recibos (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                jogo_id INTEGER NOT NULL,
                pagador_nome TEXT NOT NULL,
                pagador_documento TEXT NOT NULL,
                recebedor_nome TEXT NOT NULL,
                recebedor_documento TEXT NOT NULL,
                valor_centavos INTEGER NOT NULL,
                data_pagamento INTEGER NOT NULL,
                descricao TEXT,
                criado_em INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recibos_jogo_id ON recibos(jogo_id)")
    }
}

/**
 * v4 -> v5 (VERSÃO 1.2): transforma a competição (até então só um campo de texto livre em
 * "jogos") num cadastro central (REGRA 5), identificado por id — sem apagar ou alterar o texto
 * já salvo em cada jogo.
 *
 * Passos (tudo dentro da MESMA transação que o Room já usa para migrações — se qualquer passo
 * falhar, a migração inteira é desfeita e o banco permanece exatamente como estava na v4,
 * atendendo à REGRA 15 de "migração transacional, sem risco de perda de dados"):
 *
 * 1. Cria "competicoes" e "recibos_competicao" (tabelas novas).
 * 2. Adiciona a coluna "competicao_id" em "jogos" (nova, opcional — não mexe em nenhum dado
 *    existente).
 * 3. Lê o nome/cidade/modalidade já cadastrados em cada jogo e agrupa os equivalentes (mesmo
 *    nome+cidade+modalidade ignorando maiúsculas/minúsculas e espaços — REGRA 1) usando a MESMA
 *    função pura de :core usada pelo app daqui pra frente ([agruparCompeticoesEquivalentes]),
 *    para a limpeza da migração nunca divergir do comportamento normal do app.
 * 4. Cria uma linha em "competicoes" por grupo (preservando a grafia mais usada) e vincula cada
 *    jogo à sua competição por "competicao_id" — nunca duplica, nunca apaga um jogo.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS competicoes (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cidade TEXT,
                modalidade TEXT,
                status TEXT NOT NULL,
                data_criacao INTEGER NOT NULL,
                data_encerramento INTEGER
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recibos_competicao (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                competicao_id INTEGER NOT NULL,
                pagador_nome TEXT NOT NULL,
                pagador_documento TEXT NOT NULL,
                recebedor_nome TEXT NOT NULL,
                recebedor_documento TEXT NOT NULL,
                valor_centavos INTEGER NOT NULL,
                quantidade_jogos INTEGER NOT NULL,
                data_pagamento INTEGER NOT NULL,
                descricao TEXT,
                criado_em INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recibos_competicao_competicao_id ON recibos_competicao(competicao_id)")

        db.execSQL("ALTER TABLE jogos ADD COLUMN competicao_id INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_jogos_competicao_id ON jogos(competicao_id)")

        migrarCompeticoesExistentes(db)
    }

    /** REGRA 15: lê as competições (texto livre) já cadastradas nos jogos, agrupa as
     * equivalentes, cria um cadastro central para cada grupo e vincula os jogos a ele. */
    private fun migrarCompeticoesExistentes(db: SupportSQLiteDatabase) {
        data class LinhaJogo(val id: Long, val nome: String?, val cidade: String?, val modalidade: String?)

        val linhas = mutableListOf<LinhaJogo>()
        db.query("SELECT id, competicao, cidade, modalidade FROM jogos").use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow("id")
            val idxNome = cursor.getColumnIndexOrThrow("competicao")
            val idxCidade = cursor.getColumnIndexOrThrow("cidade")
            val idxModalidade = cursor.getColumnIndexOrThrow("modalidade")
            while (cursor.moveToNext()) {
                linhas += LinhaJogo(
                    id = cursor.getLong(idxId),
                    nome = if (cursor.isNull(idxNome)) null else cursor.getString(idxNome),
                    cidade = if (cursor.isNull(idxCidade)) null else cursor.getString(idxCidade),
                    modalidade = if (cursor.isNull(idxModalidade)) null else cursor.getString(idxModalidade),
                )
            }
        }

        val grupos = linhas.map { Triple(it.nome, it.cidade, it.modalidade) }.agruparCompeticoesEquivalentes()
        if (grupos.isEmpty()) return

        val agora = System.currentTimeMillis()
        val idPorChave = mutableMapOf<String, Long>()
        for (grupo in grupos) {
            val valores = ContentValues().apply {
                put("nome", grupo.nome)
                put("cidade", grupo.cidade)
                put("modalidade", grupo.modalidade)
                put("status", StatusCompeticao.EM_ANDAMENTO.name)
                put("data_criacao", agora)
                putNull("data_encerramento")
            }
            idPorChave[grupo.chave] = db.insert("competicoes", SQLiteDatabase.CONFLICT_ABORT, valores)
        }

        for (linha in linhas) {
            if (linha.nome.isNullOrBlank()) continue
            val chave = chaveNormalizadaCompeticao(linha.nome, linha.cidade, linha.modalidade)
            val competicaoId = idPorChave[chave] ?: continue
            db.execSQL("UPDATE jogos SET competicao_id = ? WHERE id = ?", arrayOf<Any>(competicaoId, linha.id))
        }
    }
}
