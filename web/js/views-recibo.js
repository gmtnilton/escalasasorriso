// Recibo de pagamento em PDF — pré-preenchido a partir do jogo (ou do
// recibo já existente), tudo editável antes de gerar. Upsert por jogoId:
// nunca duplica o recibo de um mesmo jogo. Espelha GerarReciboScreen.kt /
// GerarReciboViewModel.kt.
'use strict';

const NOME_RECEBEDOR_PADRAO = 'NILTON RODRIGO RIBEIRO';

async function renderRecibo(jogoId) {
  const jogo = await Db.obterJogo(jogoId);
  const existente = await Db.reciboPorJogoId(jogoId);
  const estado = existente ? {
    pagadorNome: existente.pagadorNome, pagadorDocumento: existente.pagadorDocumento,
    recebedorNome: existente.recebedorNome, recebedorDocumento: existente.recebedorDocumento,
    valorCentavos: existente.valorCentavos, dataPagamentoTexto: Core.Dates.formatarData(existente.dataPagamento),
    descricao: existente.descricao || '', numeroFormatado: reciboNumeroFormatado(existente),
  } : {
    pagadorNome: '', pagadorDocumento: '', recebedorNome: NOME_RECEBEDOR_PADRAO, recebedorDocumento: '',
    valorCentavos: jogo ? jogo.valorCentavos : 0,
    dataPagamentoTexto: Core.Dates.formatarData((jogo && jogo.dataRecebimento) || Core.Dates.hojeISO()),
    descricao: jogo && Core.confronto(jogo) ? `Pagamento referente à arbitragem da partida ${Core.confronto(jogo)}.` : '',
    numeroFormatado: null,
  };

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view view--no-nav-pad">
      ${topBar('🧾 Recibo de pagamento')}
      ${estado.numeroFormatado ? `<p class="muted" style="margin:8px 0 14px;">Recibo ${escapeHtml(estado.numeroFormatado)} já gerado para este jogo — os dados abaixo podem ser ajustados a qualquer momento.</p>` : ''}

      <div class="card">
        <div class="section-header">Pagador</div>
        <div class="field"><label>Nome / Razão social</label><input type="text" id="r-pagador-nome" value="${escapeHtml(estado.pagadorNome)}" /></div>
        <div class="field"><label>CPF / CNPJ</label><input type="text" id="r-pagador-doc" value="${escapeHtml(estado.pagadorDocumento)}" /></div>
      </div>

      <div class="card">
        <div class="section-header">Recebedor</div>
        <div class="field"><label>Nome completo</label><input type="text" id="r-receb-nome" value="${escapeHtml(estado.recebedorNome)}" /></div>
        <div class="field"><label>CPF</label><input type="text" id="r-receb-doc" value="${escapeHtml(estado.recebedorDocumento)}" /></div>
      </div>

      <div class="card">
        <div class="section-header">Pagamento</div>
        <div class="field"><label>Valor recebido</label><input type="text" id="r-valor" value="${escapeHtml(Core.Currency.formatar(estado.valorCentavos))}" inputmode="numeric" /></div>
        <div class="field"><label>Data do pagamento</label><input type="text" id="r-data" value="${escapeHtml(estado.dataPagamentoTexto)}" placeholder="dd/mm/aaaa" /></div>
        <div class="field"><label>Descrição / Referente a (opcional)</label><textarea id="r-descricao" rows="2">${escapeHtml(estado.descricao)}</textarea></div>
      </div>

      <div class="btn-row">
        <button class="btn" id="btn-gerar">${existente ? '🧾 Atualizar e compartilhar' : '🧾 Gerar recibo em PDF'}</button>
        <button class="btn btn--outline" id="btn-salvar-pdf">💾 Salvar PDF no aparelho</button>
      </div>
    </div>
  `;
  wireTopBarVoltar(view, () => navigate(`#/jogo/${jogoId}`));

  attachMoedaMask(qs('#r-valor', view), (c) => { estado.valorCentavos = c; });
  attachDateMask(qs('#r-data', view));

  function coletar() {
    return {
      jogoId,
      pagadorNome: qs('#r-pagador-nome', view).value.trim(),
      pagadorDocumento: qs('#r-pagador-doc', view).value.trim(),
      recebedorNome: qs('#r-receb-nome', view).value.trim(),
      recebedorDocumento: qs('#r-receb-doc', view).value.trim(),
      valorCentavos: estado.valorCentavos,
      dataPagamento: Core.Dates.parseData(qs('#r-data', view).value) || Core.Dates.hojeISO(),
      descricao: qs('#r-descricao', view).value.trim() || null,
    };
  }

  async function gerarEBaixar(compartilhar) {
    const recibo = await Db.salvarRecibo(coletar());
    const blob = Pdf.gerarReciboBlob(recibo, jogo);
    const nomeArquivo = Pdf.nomeArquivo(jogoId);
    if (compartilhar && navigator.share && navigator.canShare) {
      const file = new File([blob], nomeArquivo, { type: 'application/pdf' });
      if (navigator.canShare({ files: [file] })) {
        try { await navigator.share({ files: [file], title: 'Recibo de pagamento' }); return; } catch (e) { /* usuário cancelou ou falhou — cai no download */ }
      }
    }
    baixarArquivo(blob, nomeArquivo, 'application/pdf');
  }

  qs('#btn-gerar', view).onclick = () => gerarEBaixar(true);
  qs('#btn-salvar-pdf', view).onclick = () => gerarEBaixar(false);
}
