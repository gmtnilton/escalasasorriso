// Tela "Resumo" — seletor de mês/ano, card do mês em destaque, resumo
// anual, estatísticas gerais e os 3 gráficos (jogos por mês, por
// modalidade, recebido x a receber por mês). Espelha ResumoScreen.kt.
'use strict';

const ResumoState = { mes: new Date().getMonth() + 1, ano: new Date().getFullYear() };

function renderResumo() {
  const jogos = State.jogos;
  const anosDisponiveis = Core.anosDisponiveis(jogos);
  if (!anosDisponiveis.includes(ResumoState.ano) && anosDisponiveis.length) ResumoState.ano = anosDisponiveis[0];
  const anos = anosDisponiveis.includes(ResumoState.ano) ? anosDisponiveis : [ResumoState.ano, ...anosDisponiveis];

  const resumoMensal = Core.resumoDoMes(jogos, ResumoState.ano, ResumoState.mes);
  const resumoAnual = Core.resumoDoAno(jogos, ResumoState.ano);
  const stats = Core.estatisticas(jogos);
  const serieMensal = Core.serieMensal(jogos, ResumoState.ano);
  const contagemModalidade = Core.contarPorModalidade(jogos);
  const fracaoMes = resumoMensal.totalGeralCentavos > 0 ? resumoMensal.totalRecebidoCentavos / resumoMensal.totalGeralCentavos : 0;

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view">
      <div class="card-row" style="margin-bottom:14px;">
        <div class="headline">📊 Resumo</div>
        <button class="icon-btn" data-acao="exportar-pdf" title="Exportar relatório em PDF">📄</button>
      </div>

      <div class="field-row" style="margin-bottom:14px;">
        <select id="seletor-mes" style="flex:1; border:1.5px solid var(--outline); border-radius:14px; padding:10px 12px; background:var(--surface); color:var(--on-surface); font-family:inherit;">
          ${Array.from({ length: 12 }, (_, i) => i + 1).map((m) => `<option value="${m}" ${m === ResumoState.mes ? 'selected' : ''}>${Core.Dates.nomeMes(m)}</option>`).join('')}
        </select>
        <select id="seletor-ano" style="flex:1; border:1.5px solid var(--outline); border-radius:14px; padding:10px 12px; background:var(--surface); color:var(--on-surface); font-family:inherit;">
          ${anos.map((a) => `<option value="${a}" ${a === ResumoState.ano ? 'selected' : ''}>${a}</option>`).join('')}
        </select>
      </div>

      <div class="card" style="background:var(--primary-container); color:var(--on-primary-container);">
        <div class="card-row">
          <div class="subtitle" style="color:var(--on-primary-container);">📅 ${escapeHtml(Core.Dates.nomeMesAno(ResumoState.ano, ResumoState.mes))}</div>
          <div class="small" style="color:var(--on-primary-container);">${resumoMensal.totalJogos} ${resumoMensal.totalJogos === 1 ? 'jogo' : 'jogos'}</div>
        </div>
        <div style="font-size:30px; font-weight:800; margin-top:8px;">${escapeHtml(Core.Currency.formatar(resumoMensal.totalGeralCentavos))}</div>
        <div class="proportion-bar" style="margin-top:14px;"><div style="width:${(fracaoMes * 100).toFixed(1)}%"></div></div>
        <div class="card-row" style="margin-top:14px;"><span>Recebido</span><strong>${escapeHtml(Core.Currency.formatar(resumoMensal.totalRecebidoCentavos))}</strong></div>
        <div class="card-row" style="margin-top:6px;"><span>A receber</span><strong>${escapeHtml(Core.Currency.formatar(resumoMensal.totalAReceberCentavos))}</strong></div>
      </div>

      <div class="card">
        <div class="title">${ResumoState.ano}</div>
        ${linhaValor('Total de jogos', String(resumoAnual.totalJogos))}
        ${linhaValor('Total recebido', Core.Currency.formatar(resumoAnual.totalRecebidoCentavos), 'var(--status-recebido)')}
        ${linhaValor('Total a receber', Core.Currency.formatar(resumoAnual.totalAReceberCentavos), 'var(--status-areceber)')}
        ${linhaValor('Total geral', Core.Currency.formatar(resumoAnual.totalGeralCentavos))}
        ${linhaValor('Média por jogo', Core.Currency.formatar(resumoAnual.mediaPorJogoCentavos))}
      </div>

      <div class="card">
        <div class="title">Estatísticas gerais</div>
        ${linhaValor('Total de jogos', String(stats.totalJogos))}
        ${linhaValor('Recebido', Core.Currency.formatar(stats.totalRecebidoCentavos), 'var(--status-recebido)')}
        ${linhaValor('A receber', Core.Currency.formatar(stats.totalAReceberCentavos), 'var(--status-areceber)')}
        ${linhaValor('Total geral', Core.Currency.formatar(stats.totalGeralCentavos))}
        ${linhaValor('Média por jogo', Core.Currency.formatar(stats.mediaPorJogoCentavos))}
        ${linhaValor('Maior valor de jogo', Core.Currency.formatar(stats.maiorValorCentavos))}
        ${linhaValor('Jogos recebidos', String(stats.jogosRecebidos))}
        ${linhaValor('Jogos pendentes', String(stats.jogosPendentes))}
      </div>

      <div class="card">
        <div class="title">Jogos por mês — ${ResumoState.ano}</div>
        <div style="margin-top:14px;">${barChartHtml(serieMensal.map((p) => p.totalJogos), serieMensal.map((p) => Core.Dates.nomeMes(p.mes).slice(0, 3)), 'var(--primary)')}</div>
      </div>

      ${contagemModalidade.length ? `
        <div class="card">
          <div class="title">📊 Jogos por modalidade</div>
          <div style="margin-top:14px;">${hBarChartHtml(contagemModalidade, 'var(--primary)')}</div>
        </div>
      ` : ''}

      <div class="card">
        <div class="title">Recebido x A receber por mês — ${ResumoState.ano}</div>
        <div style="margin-top:14px;">${dualBarChartHtml(serieMensal.map((p) => p.totalRecebidoCentavos), serieMensal.map((p) => p.totalAReceberCentavos), serieMensal.map((p) => Core.Dates.nomeMes(p.mes).slice(0, 3)))}</div>
        <div class="legend-row" style="margin-top:10px;">
          <span class="legend-dot"><span class="dot" style="background:var(--status-recebido);"></span>Recebido</span>
          <span class="legend-dot"><span class="dot" style="background:var(--status-areceber);"></span>A receber</span>
        </div>
      </div>
    </div>
  `;

  qs('#seletor-mes', view).onchange = (ev) => { ResumoState.mes = Number(ev.target.value); renderResumo(); };
  qs('#seletor-ano', view).onchange = (ev) => { ResumoState.ano = Number(ev.target.value); renderResumo(); };
  qs('[data-acao="exportar-pdf"]', view).onclick = () => exportarRelatorioPdf(jogos);
}

function linhaValor(rotulo, valor, cor = 'var(--on-surface)') {
  return `<div class="card-row" style="margin-top:8px;"><span class="muted">${escapeHtml(rotulo)}</span><span style="font-weight:700; color:${cor};">${escapeHtml(valor)}</span></div>`;
}

function barChartHtml(valores, rotulos, cor) {
  const max = Math.max(1, ...valores);
  return `<div class="bar-chart">${valores.map((v, i) => `
    <div class="bar-col">
      <div class="bar" style="height:${Math.max(2, (v / max) * 100)}%; background:${cor};"></div>
      <div class="bar-label">${rotulos[i]}</div>
    </div>
  `).join('')}</div>`;
}

function dualBarChartHtml(valoresA, valoresB, rotulos) {
  const max = Math.max(1, ...valoresA, ...valoresB);
  return `<div class="bar-chart">${valoresA.map((a, i) => `
    <div class="bar-col">
      <div style="display:flex; align-items:flex-end; gap:2px; width:100%; height:100%;">
        <div class="bar" style="height:${Math.max(2, (a / max) * 100)}%; background:var(--status-recebido); flex:1;"></div>
        <div class="bar" style="height:${Math.max(2, (valoresB[i] / max) * 100)}%; background:var(--status-areceber); flex:1;"></div>
      </div>
      <div class="bar-label">${rotulos[i]}</div>
    </div>
  `).join('')}</div>`;
}

function hBarChartHtml(itens, cor) {
  const max = Math.max(1, ...itens.map(([, q]) => q));
  return itens.map(([nome, qtd]) => `
    <div class="hbar-row">
      <div class="hbar-label"><span>${escapeHtml(nome)}</span><span class="muted">${qtd}</span></div>
      <div class="hbar-track"><div style="width:${(qtd / max) * 100}%; background:${cor};"></div></div>
    </div>
  `).join('');
}

function exportarRelatorioPdf(jogos) {
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  const stats = Core.estatisticas(jogos);
  const resumoAnual = Core.resumoDoAno(jogos, ResumoState.ano);
  const resumoMensal = Core.resumoDoMes(jogos, ResumoState.ano, ResumoState.mes);
  let y = 50;
  doc.setFont('helvetica', 'bold'); doc.setFontSize(18); doc.setTextColor(11, 90, 108);
  doc.text('Relatório — Escala S Arbitragem', 40, y); y += 28;

  doc.setFont('helvetica', 'bold'); doc.setFontSize(13); doc.setTextColor(20, 20, 20);
  doc.text(`${Core.Dates.nomeMesAno(ResumoState.ano, ResumoState.mes)}`, 40, y); y += 18;
  doc.setFont('helvetica', 'normal'); doc.setFontSize(11);
  [
    ['Jogos no mês', String(resumoMensal.totalJogos)],
    ['Recebido no mês', Core.Currency.formatar(resumoMensal.totalRecebidoCentavos)],
    ['A receber no mês', Core.Currency.formatar(resumoMensal.totalAReceberCentavos)],
  ].forEach(([a, b]) => { doc.text(`${a}: ${b}`, 40, y); y += 16; });

  y += 14;
  doc.setFont('helvetica', 'bold'); doc.setFontSize(13);
  doc.text(`Resumo anual — ${ResumoState.ano}`, 40, y); y += 18;
  doc.setFont('helvetica', 'normal'); doc.setFontSize(11);
  [
    ['Total de jogos', String(resumoAnual.totalJogos)],
    ['Total recebido', Core.Currency.formatar(resumoAnual.totalRecebidoCentavos)],
    ['Total a receber', Core.Currency.formatar(resumoAnual.totalAReceberCentavos)],
    ['Total geral', Core.Currency.formatar(resumoAnual.totalGeralCentavos)],
    ['Média por jogo', Core.Currency.formatar(resumoAnual.mediaPorJogoCentavos)],
  ].forEach(([a, b]) => { doc.text(`${a}: ${b}`, 40, y); y += 16; });

  y += 14;
  doc.setFont('helvetica', 'bold'); doc.setFontSize(13);
  doc.text('Estatísticas gerais', 40, y); y += 18;
  doc.setFont('helvetica', 'normal'); doc.setFontSize(11);
  [
    ['Total de jogos', String(stats.totalJogos)],
    ['Recebido', Core.Currency.formatar(stats.totalRecebidoCentavos)],
    ['A receber', Core.Currency.formatar(stats.totalAReceberCentavos)],
    ['Total geral', Core.Currency.formatar(stats.totalGeralCentavos)],
    ['Média por jogo', Core.Currency.formatar(stats.mediaPorJogoCentavos)],
    ['Maior valor de jogo', Core.Currency.formatar(stats.maiorValorCentavos)],
    ['Jogos recebidos', String(stats.jogosRecebidos)],
    ['Jogos pendentes', String(stats.jogosPendentes)],
  ].forEach(([a, b]) => { doc.text(`${a}: ${b}`, 40, y); y += 16; });

  doc.save(`relatorio-arbitragem-${ResumoState.ano}-${String(ResumoState.mes).padStart(2, '0')}.pdf`);
}
