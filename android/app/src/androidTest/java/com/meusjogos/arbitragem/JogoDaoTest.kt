package com.meusjogos.arbitragem

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.meusjogos.arbitragem.data.local.AppDatabase
import com.meusjogos.arbitragem.data.local.JogoDao
import com.meusjogos.arbitragem.data.local.toDomain
import com.meusjogos.arbitragem.data.local.toEntity
import com.meusjogos.arbitragem.data.repository.JogoRepository
import com.meusjogos.arbitragem.core.model.Jogo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Testes instrumentados do Room — precisam rodar num dispositivo/emulador
 * Android (Android Studio: botão direito no arquivo > Run). Comprovam
 * exatamente o TESTE 8 do briefing ("fechar e abrir o app não pode apagar
 * os dados"): aqui simulado reabrindo o banco a partir do mesmo arquivo em
 * disco, como acontece quando o processo do app é reiniciado.
 */
@RunWith(AndroidJUnit4::class)
class JogoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: JogoDao
    private val nomeBancoTeste = "jogo_dao_test.db"

    @Before
    fun criarBanco() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(nomeBancoTeste)
        db = Room.databaseBuilder(context, AppDatabase::class.java, nomeBancoTeste).build()
        dao = db.jogoDao()
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    @Test
    fun inserirEBuscarJogo() = runBlocking {
        val jogo = Jogo(data = LocalDate.of(2026, 9, 3), valorCentavos = 40_000)
        val id = dao.inserir(jogo.toEntity())

        val salvo = dao.buscarPorId(id)?.toDomain()

        assertEquals(40_000L, salvo?.valorCentavos)
        assertEquals(LocalDate.of(2026, 9, 3), salvo?.data)
    }

    @Test
    fun jogosPersistemAposReabrirOBanco() = runBlocking {
        val repository = JogoRepository(dao)
        repository.salvar(Jogo(data = LocalDate.of(2026, 9, 3), valorCentavos = 50_000))
        db.close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(context, AppDatabase::class.java, nomeBancoTeste).build()
        dao = db.jogoDao()

        val jogosAposReabrir = dao.observarTodos().first()
        assertEquals(1, jogosAposReabrir.size)
        assertEquals(50_000L, jogosAposReabrir.first().valorCentavos)
    }

    @Test
    fun excluirJogoRemoveDaListagem() = runBlocking {
        val entidade = Jogo(data = LocalDate.now(), valorCentavos = 1000).toEntity()
        val id = dao.inserir(entidade)
        val salvo = dao.buscarPorId(id)!!

        dao.excluir(salvo)

        assertEquals(0, dao.observarTodos().first().size)
    }
}
