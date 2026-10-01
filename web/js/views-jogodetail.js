// Detalhe do jogo — editar, marcar/desfazer recebimento, duplicar, gerar
// recibo (se recebido) e excluir. Espelha JogoDetailScreen.kt.
'use strict';

async function renderJogoDetail(jogoId) {
  const jogo = await Db.obterJogo(jogoId);
  const view = qs('#app-view');
  if (!jogo) {
    view.innerHTML = `${topBar('Detalhes do jogo')}<div class="view"><p class="muted">Jogo não encontrado.</p></div>`;
    wireTopBarVoltar(view, () => navigate('#/jogos'));
    return;
  }
  const temRecibo = !!(await Db.reciboPorJogoId(jogoId));
  const confronto = Core.confronto(jogo) || 'Escala Arbitragem';
  const dataHora = jogo.horario ? `${Core.Dates.formatarData(jogo.data)} • ${Core.Dates.formatarHora(jogo.horario)}` : Core.Dates.formatarData(jogo.data);
  const recebido = Core.jogoRecebido(jogo);
  const competicaoInfo = [jogo.competicao, jogo.modalidade, jogo.categoria].filter((v) => v && v.trim()).join(' • ');
  const localInfo = [jogo.cidade, jogo.estadio, jogo.funcao ? `Função: ${jogo.funcao}` : null].filter((v) => v && String(v).trim()).join(' • ');

  view.innerHTML = `
    <div class="view view--no-nav-pad">
      ${topBar('Detalhes do jogo')}
      <div class="card" style="margin-top:8px;">
        <div class="title">⚽ ${escapeHtml(confronto)}</div>
        <div class="muted" style="margin-top:4px;">${escapeHtml(dataHora)}</div>
        <div class="card-row" style="margin-top:16px;">
          <span style="font-size:24px; font-weight:800; color:${recebido ? 'var(--status-recebido)' : 'var(--on-surface)'};">${escapeHtml(Core.Currency.formatar(jogo.valorCentavos))}</span>
          ${statusChipHtml(jogo.statusPagamento)}
        </div>
        ${competicaoInfo ? secaoDetalhe('Competição', competicaoInfo) : ''}
        ${localInfo ? secaoDetalhe('Local e arbitragem', localInfo) : ''}
        ${jogo.dataRecebimento ? secaoDetalhe('Pagamento', `Recebido em ${Core.Dates.formatarData(jogo.dataRecebimento)}`) : ''}
        ${jogo.observacoes && jogo.observacoes.trim() ? secaoDetalhe('Observações', jogo.observacoes) : ''}
      </div>

      <div class="btn-row" style="margin-top:4px;">
        <button class="btn" data-acao="editar">✏️ Editar</button>
        ${!recebido
          ? `<button class="btn btn--tertiary" data-acao="marcar-recebido">💰 Marcar como recebido</button>`
          : `<button class="btn btn--outline" data-acao="desfazer">↩️ Desfazer recebimento</button>`}
        <button class="btn btn--outline" data-acao="duplicar">📋 Duplicar jogo</button>
        ${recebido ? `<button class="btn btn--outline" data-acao="recibo">${temRecibo ? '🧾 Recibo gerado' : '🧾 Gerar recibo'}</button>` : ''}
      </div>

      <div class="divider" style="margin-top:20px;"></div>
      <button class="btn btn--danger-outline" data-acao="excluir">🗑️ Excluir jogo</button>
    </div>
  `;
  wireTopBarVoltar(view, () => navigate('#/jogos'));

  qs('[data-acao="editar"]', view).onclick = () => navigate(`#/jogo/${jogoId}/editar`);
  qs('[data-acao="duplicar"]', view).onclick = () => navigate(`#/jogo/${jogoId}/duplicar`);
  const btnRecibo = qs('[data-acao="recibo"]', view);
  if (btnRecibo) btnRecibo.onclick = () => navigate(`#/recibo/${jogoId}`);

  const btnMarcar = qs('[data-acao="marcar-recebido"]', view);
  if (btnMarcar) btnMarcar.onclick = () => abrirMarcarRecebidoDialog(jogo);

  const btnDesfazer = qs('[data-acao="desfazer"]', view);
  if (btnDesfazer) btnDesfazer.onclick = async () => {
    await Db.salvarJogo(Core.desfazerRecebimento(jogo));
    await recarregarJogos();
    renderJogoDetail(jogoId);
  };

  qs('[data-acao="excluir"]', view).onclick = () => {
    confirmarAcaoDialog({
      titulo: 'Excluir jogo?', mensagem: 'Essa ação não pode ser desfeita.', textoConfirmar: 'Excluir', perigo: true,
      onConfirmar: async () => {
        await Db.excluirJogo(jogoId);
        await recarregarJogos();
        toast('Jogo excluído.');
        navigate('#/jogos');
      },
    });
  };
}

function secaoDetalhe(titulo, texto) {
  return `
    <div class="divider" style="margin-top:16px;"></div>
    <div class="section-header">${escapeHtml(titulo)}</div>
    <div style="font-size:15px;">${escapeHtml(texto)}</div>
  `;
}

function abrirMarcarRecebidoDialog(jogo) {
  const overlay = openDialog(`
    <div class="title">Marcar como recebido?</div>
    <p class="muted" style="margin-top:8px;">Confirma o recebimento deste jogo? Você pode ajustar a data do recebimento abaixo.</p>
    <div class="field" style="margin-top:12px;">
      <label>Data do recebimento</label>
      <input type="text" id="campo-data-rec" value="${escapeHtml(Core.Dates.formatarData(Core.Dates.hojeISO()))}" placeholder="dd/mm/aaaa" />
    </div>
    <div class="btn-row-h">
      <button class="btn btn--text" data-acao="cancelar">Cancelar</button>
      <button class="btn btn--sm" data-acao="confirmar" style="width:auto;">Confirmar</button>
    </div>
  `);
  const campo = qs('#campo-data-rec', overlay);
  attachDateMask(campo);
  qs('[data-acao="cancelar"]', overlay).onclick = closeDialog;
  qs('[data-acao="confirmar"]', overlay).onclick = async () => {
    const data = Core.Dates.parseData(campo.value) || Core.Dates.hojeISO();
    closeDialog();
    await Db.salvarJogo(Core.marcarComoRecebido(jogo, data));
    await recarregarJogos();
    renderJogoDetail(jogo.id);
  };
}
