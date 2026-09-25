package com.meusjogos.arbitragem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [JogoEntity::class, ReciboEntity::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogoDao(): JogoDao

    abstract fun reciboDao(): ReciboDao

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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instancia = it }
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
