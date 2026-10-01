// Tela "Jogos" — pesquisa, filtros, marcar-todos-filtrados, chips "Por
// cidade"/"Por modalidade" e a lista de jogos (mesmo comportamento de
// JogosListScreen.kt/FiltroSheet.kt).
'use strict';

function jogosFiltradosAtuais() {
  return Core.ordenarMaisRecentePrimeiro(Core.filtrarEPesquisar(State.jogos, State.filtroJogos));
}

function renderJogosList() {
  const filtrados = jogosFiltradosAtuais();
  const pendentesFiltrados = filtrados.filter((j) => !Core.jogoRecebido(j)).length;
  const porCidade = Core.contarPorCidade(filtrados);
  const porModalidade = Core.contarPorModalidade(filtrados);

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view">
      <div class="headline" style="margin-bottom:12px;">⚽ Meus Jogos</div>

      ${buscaEFiltroRow(State.filtroJogos.pesquisa, Core.filtroAtivo(State.filtroJogos))}

      ${pendentesFiltrados > 0 ? `
        <div class="card-row" style="margin-bottom:10px;">
          <span class="muted">${pendentesFiltrados} ${pendentesFiltrados === 1 ? 'jogo a receber' : 'jogos a receber'} nesta lista</span>
          <button class="btn btn--text" data-acao="marcar-todos-filtrados">Marcar todos como recebidos</button>
        </div>
      ` : ''}

      ${(porCidade.length || porModalidade.length) ? `
        <div style="margin-bottom:6px;">
          ${linhaContagem('Por cidade', porCidade, 'cidade')}
          ${linhaContagem('Por modalidade', porModalidade, 'modalidade')}
        </div>
      ` : ''}

      <div id="lista-jogos">${listaJogosHtml(filtrados)}</div>
    </div>
    <button class="fab" data-acao="novo-jogo">➕ Novo jogo</button>
  `;

  qs('[data-acao="novo-jogo"]', view).onclick = () => navigate('#/jogo/novo');
  wireBuscaEFiltro(view, (texto) => { State.filtroJogos.pesquisa = texto; renderJogosList(); }, () => abrirFiltroJogosSheet());
  qsa('.jogo-item', view).forEach((elm) => { elm.onclick = () => navigate(`#/jogo/${elm.dataset.id}`); });
  qsa('[data-grupo-tipo]', view).forEach((elm) => {
    elm.onclick = () => abrirGrupoSheet(elm.dataset.grupoTipo, elm.dataset.grupoNome, filtrados);
  });
  const btnLote = qs('[data-acao="marcar-todos-filtrados"]', view);
  if (btnLote) btnLote.onclick = () => confirmarMarcarLote(filtrados.filter((j) => !Core.jogoRecebido(j)), async (data) => {
    await Db.marcarVariosComoRecebido(filtrados.filter((j) => !Core.jogoRecebido(j)), data);
    await recarregarJogos();
    toast('✓ Jogos marcados como recebidos.');
    renderJogosList();
  });
}

function listaJogosHtml(jogos) {
  if (jogos.length === 0) {
    if (State.jogos.length === 0) {
      return emptyState('⚽', 'Nenhum jogo cadastrado', 'Toque em "Novo jogo" para registrar seu primeiro jogo apitado.');
    }
    return emptyState('🔎', 'Nenhum jogo encontrado', 'Tente ajustar a pesquisa ou os filtros aplicados.');
  }
  return jogos.map((j) => jogoItemHtml(j)).join('');
}

function emptyState(icone, titulo, descricao) {
  return `<div class="empty-state"><div class="icon">${icone}</div><div class="title">${escapeHtml(titulo)}</div><div class="muted">${escapeHtml(descricao)}</div></div>`;
}

function jogoItemHtml(jogo, extraClasse = '') {
  const confronto = Core.confronto(jogo) || 'Escala Arbitragem';
  const dataHora = jogo.horario ? `${Core.Dates.formatarData(jogo.data)} • ${Core.Dates.formatarHora(jogo.horario)}` : Core.Dates.formatarData(jogo.data);
  const sub = [jogo.competicao, jogo.modalidade, jogo.cidade, jogo.funcao].filter((v) => v && v.trim()).join(' • ');
  const recebido = Core.jogoRecebido(jogo);
  return `
    <div class="jogo-item ${extraClasse}" data-id="${jogo.id}">
      <div class="top-row">
        <div class="title-row">
          <div class="icon-badge">⚽</div>
          <div class="confronto">${escapeHtml(confronto)}</div>
        </div>
        ${statusChipHtml(jogo.statusPagamento)}
      </div>
      <div class="datahora">${escapeHtml(dataHora)}</div>
      ${sub ? `<div class="subinfo">${escapeHtml(sub)}</div>` : ''}
      <div class="valor${recebido ? ' recebido' : ''}">${escapeHtml(Core.Currency.formatar(jogo.valorCentavos))}</div>
    </div>
  `;
}

function statusChipHtml(status) {
  return status === Core.StatusPagamento.RECEBIDO
    ? `<span class="chip chip--recebido">🟢 Recebido</span>`
    : `<span class="chip chip--areceber">🔴 A receber</span>`;
}

function linhaContagem(titulo, itens, tipo) {
  if (itens.length === 0) return '';
  return `
    <div style="margin-bottom:8px;">
      <div class="small muted" style="font-weight:700; margin-bottom:6px;">${escapeHtml(titulo)}</div>
      <div class="chip-row">
        ${itens.map(([nome, qtd]) => `<button class="chip-btn" data-grupo-tipo="${tipo}" data-grupo-nome="${escapeHtml(nome)}">${escapeHtml(nome)} · ${qtd}</button>`).join('')}
      </div>
    </div>
  `;
}

function abrirGrupoSheet(tipo, nome, jogosBase) {
  const jogos = tipo === 'cidade' ? Core.jogosDaCidade(jogosBase, nome) : Core.jogosDaModalidade(jogosBase, nome);
  const icone = tipo === 'cidade' ? '📍' : '⚽';
  const overlay = openSheet(`
    <div class="title">${icone} ${escapeHtml(nome)}</div>
    <p class="muted" style="margin-top:4px;">${jogos.length} ${jogos.length === 1 ? 'jogo' : 'jogos'}</p>
    <div style="margin-top:14px; max-height:50vh; overflow-y:auto;">
      ${jogos.map((j) => jogoItemHtml(j)).join('')}
    </div>
  `);
  qsa('.jogo-item', overlay).forEach((elm) => {
    elm.onclick = () => { closeSheet(); navigate(`#/jogo/${elm.dataset.id}`); };
  });
}

// ---------------- Busca + botão filtrar (compartilhado com Recebimento) ----------------

function buscaEFiltroRow(pesquisa, filtroAtivo, placeholder = '🔎 Pesquisar jogos...') {
  return `
    <div class="search-row">
      <div class="search-box">
        <span>🔎</span>
        <input type="search" id="campo-pesquisa" placeholder="${escapeHtml(placeholder)}" value="${escapeHtml(pesquisa)}" />
        ${pesquisa ? `<button class="icon-btn" data-acao="limpar-pesquisa" style="width:28px;height:28px;font-size:14px;">✕</button>` : ''}
      </div>
      <button class="filter-btn" data-acao="abrir-filtro">🔧 Filtrar${filtroAtivo ? '<span class="badge-dot"></span>' : ''}</button>
    </div>
  `;
}

function wireBuscaEFiltro(root, onPesquisaChange, onAbrirFiltro) {
  const input = qs('#campo-pesquisa', root);
  let timer;
  input.addEventListener('input', () => {
    clearTimeout(timer);
    timer = setTimeout(() => onPesquisaChange(input.value), 180);
  });
  const limpar = qs('[data-acao="limpar-pesquisa"]', root);
  if (limpar) limpar.onclick = () => onPesquisaChange('');
  qs('[data-acao="abrir-filtro"]', root).onclick = onAbrirFiltro;
}

function confirmarMarcarLote(jogos, onConfirmar) {
  const overlay = openDialog(`
    <div class="title">Marcar ${jogos.length} jogo(s) como recebido(s)?</div>
    <p class="muted" style="margin-top:8px;">Isso marca como recebidos todos os jogos a receber exibidos agora, considerando a pesquisa e os filtros aplicados.</p>
    <div class="field" style="margin-top:12px;">
      <label>Data do recebimento</label>
      <input type="text" id="campo-data-lote" value="${escapeHtml(Core.Dates.formatarData(Core.Dates.hojeISO()))}" placeholder="dd/mm/aaaa" />
    </div>
    <div class="btn-row-h">
      <button class="btn btn--text" data-acao="cancelar">Cancelar</button>
      <button class="btn btn--sm" data-acao="confirmar" style="width:auto;">Confirmar</button>
    </div>
  `);
  const campoData = qs('#campo-data-lote', overlay);
  attachDateMask(campoData);
  qs('[data-acao="cancelar"]', overlay).onclick = closeDialog;
  qs('[data-acao="confirmar"]', overlay).onclick = () => {
    const data = Core.Dates.parseData(campoData.value) || Core.Dates.hojeISO();
    closeDialog();
    onConfirmar(data);
  };
}

// ---------------- Sheet de filtros ----------------

function abrirFiltroJogosSheet() {
  renderFiltroSheet({
    filtro: State.filtroJogos,
    camposExtras: ['competicao', 'funcao'],
    onAplicarParcial: (novoFiltro) => { State.filtroJogos = novoFiltro; renderJogosList(); },
    onLimpar: () => { State.filtroJogos = Core.filtroPadrao(); closeSheet(); renderJogosList(); },
    onFechar: () => { closeSheet(); renderJogosList(); },
  });
}

const PERIODOS_OPCOES = [
  ['Todos', 'Todos'], ['Hoje', 'Hoje'], ['Esta semana', 'EstaSemana'],
  ['Este mês', 'EsteMes'], ['Mês anterior', 'MesAnterior'], ['Este ano', 'EsteAno'],
];

function renderFiltroSheet({ filtro, camposExtras, onAplicarParcial, onLimpar, onFechar }) {
  let f = { ...filtro, periodo: { ...filtro.periodo } };
  const opcoesDisponiveis = {
    competicao: Core.competicoesDisponiveis(State.jogos),
    modalidade: Core.modalidadesDisponiveis(State.jogos),
    funcao: Core.funcoesDisponiveis(State.jogos),
    cidade: Core.cidadesDisponiveis(State.jogos),
  };
  const rotulos = { competicao: 'Competição', modalidade: 'Modalidade', funcao: 'Função', cidade: 'Cidade' };

  function render() {
    const overlay = openSheet(`
      <div class="title" style="margin-bottom:6px;">Filtros</div>

      <div class="small muted" style="font-weight:700; margin:14px 0 8px;">Status</div>
      <div class="chip-row" style="flex-wrap:wrap;">
        ${chipFiltro('Todos', f.status === 'TODOS', 'status', 'TODOS')}
        ${chipFiltro('A receber', f.status === 'A_RECEBER', 'status', 'A_RECEBER')}
        ${chipFiltro('Recebidos', f.status === 'RECEBIDOS', 'status', 'RECEBIDOS')}
      </div>

      <div class="small muted" style="font-weight:700; margin:14px 0 8px;">Período</div>
      <div class="chip-row" style="flex-wrap:wrap;">
        ${PERIODOS_OPCOES.map(([rotulo, tipo]) => chipFiltro(rotulo, f.periodo.tipo === tipo, 'periodo', tipo)).join('')}
        ${chipFiltro('Personalizado', f.periodo.tipo === 'Personalizado', 'periodo', 'Personalizado')}
      </div>
      ${f.periodo.tipo === 'Personalizado' ? `
        <div class="field-row" style="margin-top:10px;">
          <div class="field"><label>De</label><input type="text" id="periodo-inicio" value="${escapeHtml(Core.Dates.formatarData(f.periodo.inicio))}" placeholder="dd/mm/aaaa" /></div>
          <div class="field"><label>Até</label><input type="text" id="periodo-fim" value="${escapeHtml(Core.Dates.formatarData(f.periodo.fim))}" placeholder="dd/mm/aaaa" /></div>
        </div>
      ` : ''}

      ${camposExtras.map((campo) => opcoesDisponiveis[campo].length ? `
        <div class="small muted" style="font-weight:700; margin:14px 0 8px;">${rotulos[campo]}</div>
        <div class="field"><select data-campo-filtro="${campo}">
          <option value="">Todas</option>
          ${opcoesDisponiveis[campo].map((op) => `<option value="${escapeHtml(op)}" ${f[campo] === op ? 'selected' : ''}>${escapeHtml(op)}</option>`).join('')}
        </select></div>
      ` : '').join('')}

      <div class="divider" style="margin-top:16px;"></div>
      <div class="card-row">
        <button class="btn btn--text" data-acao="limpar">Limpar filtros</button>
        <button class="btn btn--text" data-acao="aplicar">Aplicar</button>
      </div>
    `);

    qsa('[data-chip-grupo]', overlay).forEach((btn) => {
      btn.onclick = () => {
        const grupo = btn.dataset.chipGrupo, valor = btn.dataset.chipValor;
        if (grupo === 'status') f.status = valor;
        else if (grupo === 'periodo') {
          if (valor === 'Personalizado') {
            if (f.periodo.tipo !== 'Personalizado') {
              const hoje = Core.Dates.hojeISO();
              f.periodo = { tipo: 'Personalizado', inicio: hoje.slice(0, 8) + '01', fim: hoje };
            }
          } else {
            f.periodo = { tipo: valor };
          }
        }
        render();
      };
    });
    const ini = qs('#periodo-inicio', overlay);
    const fim = qs('#periodo-fim', overlay);
    if (ini) { attachDateMask(ini); ini.addEventListener('change', () => { const v = Core.Dates.parseData(ini.value); if (v) f.periodo.inicio = v; onAplicarParcial(f); }); }
    if (fim) { attachDateMask(fim); fim.addEventListener('change', () => { const v = Core.Dates.parseData(fim.value); if (v) f.periodo.fim = v; onAplicarParcial(f); }); }

    camposExtras.forEach((campo) => {
      const sel = qs(`[data-campo-filtro="${campo}"]`, overlay);
      if (sel) sel.onchange = () => { f[campo] = sel.value || null; onAplicarParcial(f); };
    });

    qs('[data-acao="limpar"]', overlay).onclick = onLimpar;
    qs('[data-acao="aplicar"]', overlay).onclick = onFechar;
    onAplicarParcial(f);
  }
  render();
}

function chipFiltro(rotulo, selecionado, grupo, valor) {
  return `<button class="chip-btn${selecionado ? ' active' : ''}" data-chip-grupo="${grupo}" data-chip-valor="${valor}">${escapeHtml(rotulo)}</button>`;
}
