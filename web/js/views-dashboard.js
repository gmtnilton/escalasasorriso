// Tela "Início" — mesmos cards do DashboardScreen.kt: hero de saudação em
// gradiente, A RECEBER/RECEBIDO/TOTAL GERAL/JOGOS APITADOS, resumo
// financeiro com barra de proporção, e resumo do mês atual.
'use strict';

function renderDashboard() {
  const jogos = State.jogos;
  const totalAReceber = Core.totalAReceberCentavos(jogos);
  const totalRecebido = Core.totalRecebidoCentavos(jogos);
  const totalGeral = totalRecebido + totalAReceber;
  const totalJogos = jogos.length;

  const hoje = new Date();
  const ano = hoje.getFullYear(), mes = hoje.getMonth() + 1;
  const resumoMes = Core.resumoDoMes(jogos, ano, mes);
  const fracaoRecebidaGeral = totalGeral > 0 ? totalRecebido / totalGeral : 0;

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view">
      <div class="hero-gradient">
        <div class="headline">Olá, vamos para mais um jogo 👋</div>
        <p class="muted" style="margin-top:4px;">Controle dos seus jogos de arbitragem</p>
      </div>

      <div class="card-grid-2">
        ${statCard({ titulo: 'A RECEBER', icone: '💰', valor: Core.Currency.formatar(totalAReceber), corFundo: 'var(--status-areceber-bg-themed)', corValor: 'var(--status-areceber)' })}
        ${statCard({ titulo: 'RECEBIDO', icone: '✅', valor: Core.Currency.formatar(totalRecebido), corFundo: 'var(--status-recebido-bg-themed)', corValor: 'var(--status-recebido)' })}
      </div>
      <div class="card-grid-2">
        ${statCard({ titulo: 'TOTAL GERAL', icone: '📊', valor: Core.Currency.formatar(totalGeral), gradiente: true })}
        ${statCard({ titulo: 'JOGOS APITADOS', icone: '⚽', valor: String(totalJogos), subtitulo: totalJogos === 1 ? 'jogo' : 'jogos', corFundo: 'var(--tertiary-container)', corValor: 'var(--on-tertiary-container)' })}
      </div>

      <div class="card">
        <div class="subtitle">Resumo financeiro</div>
        <div class="proportion-bar" style="margin-top:14px;"><div style="width:${(fracaoRecebidaGeral * 100).toFixed(1)}%"></div></div>
        ${linhaResumo('A receber', Core.Currency.formatar(totalAReceber), 'var(--status-areceber)')}
        ${linhaResumo('Recebido', Core.Currency.formatar(totalRecebido), 'var(--status-recebido)')}
        ${linhaResumo('Total geral', Core.Currency.formatar(totalGeral), 'var(--on-surface)')}
      </div>

      <div class="card">
        <div class="card-row">
          <div class="subtitle">📅 ${escapeHtml(Core.Dates.nomeMesAno(ano, mes))}</div>
          <div class="muted">${resumoMes.totalJogos} ${resumoMes.totalJogos === 1 ? 'jogo' : 'jogos'}</div>
        </div>
        ${linhaResumo('Recebido', Core.Currency.formatar(resumoMes.totalRecebidoCentavos), 'var(--status-recebido)')}
        ${linhaResumo('A receber', Core.Currency.formatar(resumoMes.totalAReceberCentavos), 'var(--status-areceber)')}
        ${linhaResumo('Total', Core.Currency.formatar(resumoMes.totalGeralCentavos), 'var(--on-surface)')}
      </div>
    </div>
    <button class="fab" data-acao="novo-jogo">➕ Novo jogo</button>
  `;
  qs('[data-acao="novo-jogo"]', view).onclick = () => navigate('#/jogo/novo');
}

function statCard({ titulo, icone, valor, subtitulo, corFundo, corValor, gradiente }) {
  const classe = gradiente ? 'stat-card gradient' : 'stat-card';
  const estiloFundo = gradiente ? '' : `background:${corFundo};`;
  const estiloValor = gradiente ? '' : `color:${corValor};`;
  return `
    <div class="${classe}" style="${estiloFundo}">
      <div class="stat-head">
        <div class="icon-badge">${icone}</div>
        <div class="stat-label">${escapeHtml(titulo)}</div>
      </div>
      <div class="stat-value" style="${estiloValor}">${escapeHtml(valor)}</div>
      ${subtitulo ? `<div class="stat-sub">${escapeHtml(subtitulo)}</div>` : ''}
    </div>
  `;
}

function linhaResumo(rotulo, valor, cor) {
  return `
    <div class="card-row" style="margin-top:10px;">
      <span style="font-size:14px;">${escapeHtml(rotulo)}</span>
      <span style="font-size:15px; font-weight:700; color:${cor};">${escapeHtml(valor)}</span>
    </div>
  `;
}
