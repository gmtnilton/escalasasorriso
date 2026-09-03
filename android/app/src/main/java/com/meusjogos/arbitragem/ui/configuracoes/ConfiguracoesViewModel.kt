package com.meusjogos.arbitragem.ui.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.data.backup.BackupManager
import com.meusjogos.arbitragem.data.backup.ExportManager
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfiguracoesUiState(
    val processando: Boolean = false,
    val mensagem: String? = null,
    val mensagemErro: Boolean = false,
)

class ConfiguracoesViewModel(private val repository: JogoRepository) : ViewModel() {

    private val backupManager = BackupManager(repository)
    private val exportManager = ExportManager(repository)

    private val _uiState = MutableStateFlow(ConfiguracoesUiState())
    val uiState: StateFlow<ConfiguracoesUiState> = _uiState.asStateFlow()

    suspend fun gerarConteudoBackup(): String = backupManager.gerarBackup()

    suspend fun gerarConteudoCsv(): String = exportManager.gerarCsv()

    fun nomeArquivoBackup(): String = BackupManager.nomeArquivoSugerido()

    fun nomeArquivoCsv(): String = ExportManager.nomeArquivoSugerido()

    fun aoBackupConcluido() {
        _uiState.update { it.copy(mensagem = "Backup salvo com sucesso.", mensagemErro = false) }
    }

    fun aoExportarCsvConcluido() {
        _uiState.update { it.copy(mensagem = "Dados exportados em CSV.", mensagemErro = false) }
    }

    fun restaurarBackup(conteudo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(processando = true, mensagem = null) }
            try {
                val quantidade = backupManager.restaurar(conteudo)
                _uiState.update {
                    it.copy(
                        processando = false,
                        mensagem = "Backup restaurado: $quantidade jogo(s).",
                        mensagemErro = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        processando = false,
                        mensagem = "Não foi possível restaurar este arquivo: ${e.message ?: "formato inválido"}.",
                        mensagemErro = true,
                    )
                }
            }
        }
    }

    fun reportarErro(mensagem: String) {
        _uiState.update { it.copy(mensagem = mensagem, mensagemErro = true) }
    }

    fun limparMensagem() = _uiState.update { it.copy(mensagem = null) }
}
