// Tela "Recebimento" — baixa em lote por competição/modalidade/cidade/jogo,
// seleção manual (toque longo ou botão "Selecionar") e marcação em massa.
// Espelha RecebimentoScreen.kt/RecebimentoViewModel.kt.
'use strict';

function recebimentoJogosFiltrados() {
  return Core.ordenarMaisRecentePrimeiro(Core.filtrarEPesquisar(State.jogos, State.filtroRecebimento));
}

function renderRecebimento() {
  const jogosFiltrados = recebimentoJogosFiltrados();
  const idsVisiveis = new Set(jogosFiltrados.map((j) => j.id));
  State.selecionadosRecebimento = new Set([...State.selecionadosRecebimento].filter((id) => idsVisiveis.has(id)));
  const modoSelecao = State.selecionadosRecebimento.size > 0;

  const jogosParaContadores = Core.filtrarEPesquisar(State.jogos, { ...State.filtroRecebimento, status: Core.FiltroStatus.TODOS });
  const totalPendentes = jogosParaContadores.filter((j) => !Core.jogoRecebido(j)).length;
  const totalRecebidos = jogosParaContadores.filter(Core.jogoRecebido).length;
  const totalGeral = jogosParaContadores.length;

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view">
      <div class="card-row" style="margin-bottom:4px;">
        ${modoSelecao ? `
          <div style="display:flex; align-items:center; gap:8px;">
            <button class="icon-btn" data-acao="desmarcar-todos">✕</button>
            <span class="subtitle">✓ ${State.selecionadosRecebimento.size} ${State.selecionadosRecebimento.size === 1 ? 'jogo selecionado' : 'jogos selecionados'}</span>
          </div>
        ` : `<div class="headline">📥 Recebimento</div>`}
        ${jogosFiltrados.length ? `
          <div>
            ${modoSelecao ? '<button class="btn btn--text" data-acao="desmarcar-todos2">Desmarcar todos</button>' : ''}
            <button class="btn btn--text" data-acao="selecionar-todos">Selecionar todos</button>
          </div>
        ` : ''}
      </div>

      ${!modoSelecao ? `
        <div class="count-row">
          <div class="count-item"><div class="value" style="color:var(--status-areceber);">🔴 ${totalPendentes}</div><div class="label">Pendentes</div></div>
          <div class="count-item"><div class="value" style="color:var(--status-recebido);">🟢 ${totalRecebidos}</div><div class="label">Recebidos</div></div>
          <div class="count-item"><div class="value">⚽ ${totalGeral}</div><div class="label">Total</div></div>
        </div>
        <button class="btn" data-acao="abrir-receber" style="margin-bottom:14px;">➕ RECEBER</button>
      ` : ''}

      ${buscaEFiltroRow(State.filtroRecebimento.pesquisa, Core.filtroAtivo(State.filtroRecebimento), '🔎 Pesquisar jogo, equipe ou cidade...')}

      <div id="lista-recebimento">
        ${jogosFiltrados.length === 0
          ? emptyState('✅', 'Nenhum jogo pendente aqui', 'Ajuste os filtros para ver outros jogos, ou aproveite — está tudo em dia!')
          : jogosFiltrados.map((j) => recebimentoItemHtml(j, State.selecionadosRecebimento.has(j.id), modoSelecao)).join('')}
      </div>
    </div>
    ${modoSelecao ? `
      <div style="position:fixed; left:50%; transform:translateX(-50%); bottom:0; width:100%; max-width:560px; background:var(--surface); box-shadow:0 -2px 10px rgba(0,0,0,0.15); padding:16px 16px calc(16px + env(safe-area-inset-bottom)); z-index:45;">
        <button class="btn btn--tertiary" data-acao="marcar-selecionados">📥 MARCAR COMO RECEBIDOS (${State.selecionadosRecebimento.size})</button>
      </div>
    ` : ''}
  `;

  wireBuscaEFiltro(view, (texto) => { State.filtroRecebimento.pesquisa = texto; renderRecebimento(); }, () => abrirFiltroRecebimentoSheet());

  const btnAbrir = qs('[data-acao="abrir-receber"]', view);
  if (btnAbrir) btnAbrir.onclick = abrirEscolherTipoRecebimentoSheet;

  const btnDesmarcar = qs('[data-acao="desmarcar-todos"]', view) || qs('[data-acao="desmarcar-todos2"]', view);
  qsa('[data-acao="desmarcar-todos"], [data-acao="desmarcar-todos2"]', view).forEach((b) => { b.onclick = () => { State.selecionadosRecebimento.clear(); renderRecebimento(); }; });

  const btnSelTodos = qs('[data-acao="selecionar-todos"]', view);
  if (btnSelTodos) btnSelTodos.onclick = () => { State.selecionadosRecebimento = new Set(jogosFiltrados.map((j) => j.id)); renderRecebimento(); };

  qsa('.jogo-item', view).forEach((elm) => {
    const id = Number(elm.dataset.id);
    elm.onclick = () => {
      if (modoSelecao) {
        toggleSelecaoRecebimento(id);
      } else {
        navigate(`#/jogo/${id}`);
      }
    };
    let pressTimer;
    elm.addEventListener('touchstart', () => { pressTimer = setTimeout(() => toggleSelecaoRecebimento(id), 480); }, { passive: true });
    elm.addEventListener('touchend', () => clearTimeout(pressTimer));
    elm.addEventListener('touchmove', () => clearTimeout(pressTimer));
    const checkbox = qs('input[type=checkbox]', elm);
    if (checkbox) checkbox.onclick = (ev) => { ev.stopPropagation(); toggleSelecaoRecebimento(id); };
  });

  const btnMarcar = qs('[data-acao="marcar-selecionados"]', view);
  if (btnMarcar) btnMarcar.onclick = () => abrirConfirmarRecebimentoLote(jogosFiltrados);
}

function toggleSelecaoRecebimento(id) {
  if (State.selecionadosRecebimento.has(id)) State.selecionadosRecebimento.delete(id); else State.selecionadosRecebimento.add(id);
  renderRecebimento();
}

function recebimentoItemHtml(jogo, selecionado, modoSelecao) {
  const confronto = Core.confronto(jogo) || 'Escala Arbitragem';
  const horario = jogo.horario ? ` &nbsp; ⏰ ${escapeHtml(Core.Dates.formatarHora(jogo.horario))}` : '';
  const cidade = jogo.cidade && jogo.cidade.trim() ? ` &nbsp; 📍 ${escapeHtml(jogo.cidade)}` : '';
  const corValor = Core.jogoRecebido(jogo) ? 'var(--status-recebido)' : 'var(--status-areceber)';
  return `
    <div class="jogo-item${selecionado ? ' selected' : ''}" data-id="${jogo.id}">
      ${modoSelecao ? `<input type="checkbox" ${selecionado ? 'checked' : ''} />` : ''}
      ${jogo.competicao && jogo.competicao.trim() ? `<div class="comp-tag">🏆 ${escapeHtml(jogo.competicao.toUpperCase())}</div>` : ''}
      <div class="confronto">⚽ ${escapeHtml(confronto)}</div>
      <div class="datahora">📅 ${escapeHtml(Core.Dates.formatarData(jogo.data))}${horario}${cidade}</div>
      <div class="top-row" style="margin-top:8px;">
        ${statusChipHtml(jogo.statusPagamento)}
        <span style="font-weight:800; color:${corValor};">${escapeHtml(Core.Currency.formatar(jogo.valorCentavos))}</span>
      </div>
    </div>
  `;
}

function abrirFiltroRecebimentoSheet() {
  renderFiltroSheet({
    filtro: State.filtroRecebimento,
    camposExtras: ['competicao', 'modalidade', 'cidade'],
    onAplicarParcial: (novoFiltro) => { State.filtroRecebimento = novoFiltro; renderRecebimento(); },
    onLimpar: () => { State.filtroRecebimento = { ...Core.filtroPadrao(), status: Core.FiltroStatus.A_RECEBER }; closeSheet(); renderRecebimento(); },
    onFechar: () => { closeSheet(); renderRecebimento(); },
  });
}

function abrirConfirmarRecebimentoLote(jogosFiltrados) {
  const n = State.selecionadosRecebimento.size;
  const overlay = openDialog(`
    <div class="title">📥 Confirmar recebimento</div>
    <p class="muted" style="margin-top:8px;">Você está prestes a marcar ${n} ${n === 1 ? 'jogo' : 'jogos'} como recebido(s).</p>
    <div class="field" style="margin-top:12px;">
      <label>Data do recebimento</label>
      <input type="text" id="campo-data-receb" value="${escapeHtml(Core.Dates.formatarData(Core.Dates.hojeISO()))}" placeholder="dd/mm/aaaa" />
    </div>
    <div class="btn-row-h">
      <button class="btn btn--text" data-acao="cancelar">CANCELAR</button>
      <button class="btn btn--sm" data-acao="confirmar" style="width:auto;">CONFIRMAR</button>
    </div>
  `);
  const campoData = qs('#campo-data-receb', overlay);
  attachDateMask(campoData);
  qs('[data-acao="cancelar"]', overlay).onclick = closeDialog;
  qs('[data-acao="confirmar"]', overlay).onclick = async () => {
    const data = Core.Dates.parseData(campoData.value) || Core.Dates.hojeISO();
    closeDialog();
    const jogosParaMarcar = jogosFiltrados.filter((j) => State.selecionadosRecebimento.has(j.id) && !Core.jogoRecebido(j));
    await Db.marcarVariosComoRecebido(jogosParaMarcar, data);
    State.selecionadosRecebimento.clear();
    await recarregarJogos();
    toast(`✓ Recebimento concluído — ${jogosParaMarcar.length} ${jogosParaMarcar.length === 1 ? 'jogo marcado' : 'jogos marcados'} como recebido(s) com sucesso.`);
    renderRecebimento();
  };
}

// ---------------- Botão "➕ RECEBER" ----------------

const TIPOS_RECEBIMENTO = [
  { tipo: 'competicao', emoji: '🏆', titulo: 'Competição' },
  { tipo: 'modalidade', emoji: '⚽', titulo: 'Modalidade' },
  { tipo: 'jogo', emoji: '🎯', titulo: 'Jogo' },
  { tipo: 'cidade', emoji: '📍', titulo: 'Cidade' },
];

function abrirEscolherTipoRecebimentoSheet() {
  const overlay = openSheet(`
    <div class="title">Como você deseja receber?</div>
    <div style="margin-top:12px;">
      ${TIPOS_RECEBIMENTO.map((t) => `
        <button class="option-row" data-tipo="${t.tipo}">
          <span class="emoji">${t.emoji}</span>
          <span class="label">${t.titulo}</span>
          <span class="chevron">›</span>
        </button>
      `).join('')}
    </div>
  `);
  qsa('[data-tipo]', overlay).forEach((btn) => {
    btn.onclick = () => {
      closeSheet();
      if (btn.dataset.tipo === 'jogo') {
        State.filtroRecebimento = { ...State.filtroRecebimento, competicao: null, modalidade: null, cidade: null };
        renderRecebimento();
      } else {
        abrirEscolherGrupoRecebimentoSheet(btn.dataset.tipo);
      }
    };
  });
}

function abrirEscolherGrupoRecebimentoSheet(tipo) {
  const pendentes = State.jogos.filter((j) => !Core.jogoRecebido(j));
  const grupos = tipo === 'competicao' ? Core.contarPorCompeticao(pendentes)
    : tipo === 'modalidade' ? Core.contarPorModalidade(pendentes)
      : Core.contarPorCidade(pendentes);
  const info = TIPOS_RECEBIMENTO.find((t) => t.tipo === tipo);

  const overlay = openSheet(`
    <div class="title">${info.emoji} Receber por ${info.titulo.toLowerCase()}</div>
    ${grupos.length === 0 ? `<p class="muted" style="margin-top:16px;">Nenhum jogo pendente encontrado.</p>` : `
      <div style="margin-top:12px;">
        ${grupos.map(([nome, qtd]) => `
          <button class="option-row" data-nome="${escapeHtml(nome)}">
            <span style="flex:1;">
              <div class="label">${info.emoji} ${escapeHtml(nome)}</div>
              <div class="sub">${qtd} ${qtd === 1 ? 'jogo pendente' : 'jogos pendentes'}</div>
            </span>
            <span class="chevron">›</span>
          </button>
        `).join('')}
      </div>
    `}
  `);
  qsa('[data-nome]', overlay).forEach((btn) => {
    btn.onclick = () => {
      closeSheet();
      State.filtroRecebimento = { ...State.filtroRecebimento, [tipo]: btn.dataset.nome };
      renderRecebimento();
    };
  });
}
