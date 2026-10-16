package com.meusjogos.arbitragem.data.repository

import com.meusjogos.arbitragem.core.logic.encerrarCompeticao
import com.meusjogos.arbitragem.core.logic.encontrarEquivalente
import com.meusjogos.arbitragem.core.logic.reabrirCompeticao
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.data.local.CompeticaoDao
import com.meusjogos.arbitragem.data.local.toDomain
import com.meusjogos.arbitragem.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Fonte única de verdade do cadastro central de competições (VERSÃO 1.2).
 * [buscarOuCriar] garante a REGRA 16 (nunca duas competições equivalentes):
 * toda vez que um jogo é salvo com uma competição nova, passa primeiro por
 * aqui — se já existir uma equivalente (mesmo nome+cidade+modalidade
 * normalizados), reaproveita; senão, cria.
 */
class CompeticaoRepository(private val dao: CompeticaoDao) {

    fun observarCompeticoes(): Flow<List<Competicao>> = dao.observarTodas().map { lista -> lista.map { it.toDomain() } }

    suspend fun listarTudoUmaVez(): List<Competicao> = dao.observarTodas().first().map { it.toDomain() }

    suspend fun buscarPorId(id: Long): Competicao? = dao.buscarPorId(id)?.toDomain()

    /** Resolve a competição de [nome]/[cidade]/[modalidade]: reaproveita uma equivalente já
     * existente, ou cria uma nova — nunca duplica (REGRA 3/16). */
    suspend fun buscarOuCriar(nome: String, cidade: String?, modalidade: String?): Competicao {
        val existente = listarTudoUmaVez().encontrarEquivalente(nome, cidade, modalidade)
        if (existente != null) return existente
        val nova = Competicao(nome = nome.trim(), cidade = cidade?.trim()?.ifBlank { null }, modalidade = modalidade?.trim()?.ifBlank { null })
        val id = dao.inserir(nova.toEntity())
        return nova.copy(id = id)
    }

    suspend fun encerrar(competicao: Competicao) {
        dao.atualizar(encerrarCompeticao(competicao).toEntity())
    }

    suspend fun reabrir(competicao: Competicao) {
        dao.atualizar(reabrirCompeticao(competicao).toEntity())
    }

    /**
     * Edita nome/cidade/modalidade de uma competição (REGRA 2 da V1.2). Se o novo valor coincidir
     * (nome+cidade+modalidade normalizados) com OUTRA competição já existente, unifica: os jogos
     * da competição editada passam a usar a já existente (que sobrevive, por já ser a identidade
     * "de destino"), e a editada é removida — nunca duplica jogos nem recebimentos, e os jogos
     * vinculados têm seu texto de competição sincronizado com o nome final.
     *
     * Retorna a competição "sobrevivente" — normalmente a própria [competicao] editada, mas,
     * quando o novo nome/cidade/modalidade unifica com outra já existente, a já existente (quem
     * chamou precisa passar a tratar ESSE id como o atual, já que o id de [competicao] deixa de
     * existir).
     */
    suspend fun editar(competicao: Competicao, novoNome: String, novaCidade: String?, novaModalidade: String?): Competicao {
        val outras = listarTudoUmaVez().filter { it.id != competicao.id }
        val equivalente = outras.encontrarEquivalente(novoNome, novaCidade, novaModalidade)
        if (equivalente != null) {
            dao.repontarJogos(deId = competicao.id, paraId = equivalente.id)
            dao.sincronizarNomeDosJogos(equivalente.id, equivalente.nome)
            dao.excluirPorId(competicao.id)
            return equivalente
        }
        val atualizada = competicao.copy(
            nome = novoNome.trim(),
            cidade = novaCidade?.trim()?.ifBlank { null },
            modalidade = novaModalidade?.trim()?.ifBlank { null },
        )
        dao.atualizar(atualizada.toEntity())
        dao.sincronizarNomeDosJogos(atualizada.id, atualizada.nome)
        return atualizada
    }
}
