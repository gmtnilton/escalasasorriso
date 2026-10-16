// Tela "Configurações" — tema, backup/restauração, exportação CSV,
// importação de outro sistema e informações do app. Espelha
// ConfiguracoesScreen.kt/ConfiguracoesViewModel.kt (mesmo formato de
// arquivo do Android — ver js/backup.js).
'use strict';

function temaAtual() { return localStorage.getItem('tema') || 'sistema'; }
function aplicarTema(tema) {
  localStorage.setItem('tema', tema);
  const root = document.documentElement;
  if (tema === 'claro') root.setAttribute('data-theme', 'light');
  else if (tema === 'escuro') root.setAttribute('data-theme', 'dark');
  else root.removeAttribute('data-theme');
}

function renderConfiguracoes() {
  const tema = temaAtual();
  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view">
      <div class="headline" style="margin-bottom:14px;">⚙️ Configurações</div>

      <div class="card">
        <div class="subtitle" style="margin-bottom:10px;">Aparência</div>
        <div class="segmented">
          <button data-tema="claro" class="${tema === 'claro' ? 'active' : ''}">☀️ Claro</button>
          <button data-tema="escuro" class="${tema === 'escuro' ? 'active' : ''}">🌙 Escuro</button>
          <button data-tema="sistema" class="${tema === 'sistema' ? 'active' : ''}">⚙️ Sistema</button>
        </div>
      </div>

      <div class="card" style="padding:6px 18px;">
        <div class="list-item" data-acao="backup">
          <span class="icon">☁️⬆️</span>
          <span class="texts"><div class="t1">Fazer backup</div><div class="t2">Salva todos os jogos em um arquivo .json</div></span>
        </div>
        <div class="list-item" data-acao="restaurar">
          <span class="icon">☁️⬇️</span>
          <span class="texts"><div class="t1">Restaurar backup</div><div class="t2">Substitui os jogos atuais pelos de um arquivo .json</div></span>
        </div>
        <div class="list-item" data-acao="exportar-csv">
          <span class="icon">📊</span>
          <span class="texts"><div class="t1">Exportar dados (CSV)</div><div class="t2">Gera uma planilha para abrir no Excel/Planilhas</div></span>
        </div>
        <div class="list-item" data-acao="importar-outro">
          <span class="icon">📥</span>
          <span class="texts"><div class="t1">Importar de outro sistema</div><div class="t2">Soma os jogos de um arquivo .json de outro app — nada é apagado</div></span>
        </div>
      </div>

      <div class="card" style="padding:6px 18px;">
        <div class="list-item" style="cursor:default;">
          <span class="icon">ℹ️</span>
          <span class="texts"><div class="t1">Escalas Árbitros — Web (PWA)</div><div class="t2">Versão 1.1.0 — funciona offline após o primeiro acesso; os dados ficam só neste navegador.</div></span>
        </div>
        <div class="list-item" style="cursor:default;">
          <span class="icon">👤</span>
          <span class="texts"><div class="t1">Proprietário do sistema</div><div class="t2">NILTON RODRIGO RIBEIRO</div></span>
        </div>
      </div>

      <div class="card" style="background:var(--surface-variant);">
        <div class="small muted">📱 Esta é a versão para navegador (iPhone/Android/computador). Os dados ficam salvos só neste aparelho/navegador — use "Fazer backup" regularmente e, se quiser, "Restaurar backup" para trazer os jogos do app Android para cá (ou vice-versa).</div>
      </div>
    </div>
    <input type="file" id="input-restaurar" accept="application/json,.json" style="display:none;" />
    <input type="file" id="input-importar" accept="application/json,.json" style="display:none;" />
  `;

  qsa('[data-tema]', view).forEach((btn) => {
    btn.onclick = () => { aplicarTema(btn.dataset.tema); renderConfiguracoes(); };
  });

  qs('[data-acao="backup"]', view).onclick = () => {
    const conteudo = Backup.gerarBackup(State.jogos);
    baixarArquivo(conteudo, Backup.nomeArquivoBackup(), 'application/json');
    toast('Backup salvo com sucesso.');
  };

  qs('[data-acao="exportar-csv"]', view).onclick = () => {
    const conteudo = Backup.gerarCsv(State.jogos);
    baixarArquivo(conteudo, Backup.nomeArquivoCsv(), 'text/csv');
    toast('Dados exportados em CSV.');
  };

  const inputRestaurar = qs('#input-restaurar', view);
  qs('[data-acao="restaurar"]', view).onclick = () => inputRestaurar.click();
  inputRestaurar.onchange = async () => {
    const file = inputRestaurar.files[0];
    if (!file) return;
    try {
      const texto = await file.text();
      const jogos = Backup.parseBackupProprio(texto);
      await Db.restaurarBackup(jogos);
      await recarregarJogos();
      toast(`Backup restaurado: ${jogos.length} jogo(s).`);
      renderConfiguracoes();
    } catch (e) {
      toast(`Não foi possível restaurar este arquivo: ${e.message || 'formato inválido'}.`, true);
    }
    inputRestaurar.value = '';
  };

  const inputImportar = qs('#input-importar', view);
  qs('[data-acao="importar-outro"]', view).onclick = () => inputImportar.click();
  inputImportar.onchange = async () => {
    const file = inputImportar.files[0];
    if (!file) return;
    try {
      const texto = await file.text();
      const jogos = Backup.parseFormatoRecords(texto);
      await Db.importarJogos(jogos);
      await recarregarJogos();
      toast(`${jogos.length} jogo(s) importado(s) e somado(s) aos seus jogos.`);
      renderConfiguracoes();
    } catch (e) {
      toast(`Não foi possível importar este arquivo: ${e.message || 'formato inválido'}.`, true);
    }
    inputImportar.value = '';
  };
}
