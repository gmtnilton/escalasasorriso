// Formulário de jogo (novo / editar / duplicar) — só data e valor são
// obrigatórios, tudo o mais é opcional. Cadastro em lote (N partidas) cria
// N jogos de uma vez. Espelha JogoFormScreen.kt.
'use strict';

const COMPETICOES_PADRAO = ['Campeonato Estadual', 'Campeonato Municipal', 'Copa', 'Amistoso', 'Base', 'Feminino', 'Outro'];
const CATEGORIAS_PADRAO = ['Profissional', 'Amador', 'Sub-20', 'Sub-17', 'Sub-15', 'Feminino', 'Outro'];
const FUNCOES_PADRAO = ['Árbitro', 'Assistente 1', 'Assistente 2', 'Quarto árbitro', 'VAR', 'Anotador', 'Outro'];
const MODALIDADES_PADRAO = ['Futebol de Campo', 'Futebol Society', 'Futsal', 'Beach Soccer', 'Outro'];
const CIDADES_PADRAO = ['Sorriso', 'Lucas do Rio Verde', 'Nova Mutum', 'Tapurah', 'Ipiranga do Norte',
  'Vera', 'Feliz Natal', 'Sinop', 'Cláudia', 'União do Sul', 'Itaúba', 'Nova Ubiratã', 'Santa Carmem', 'Novo Mundo'];

async function renderJogoForm(jogoId, duplicado, editando) {
  let jogo = null;
  const modo = jogoId == null ? 'novo' : duplicado ? 'duplicar' : (editando ? 'editar' : 'editar');
  if (jogoId != null) {
    jogo = await Db.obterJogo(jogoId);
    if (duplicado) jogo = Core.duplicarJogo(jogo);
  }
  const estado = jogo ? { ...jogo } : {
    id: 0, data: Core.Dates.hojeISO(), horario: null, competicao: null, modalidade: null, categoria: null,
    equipeMandante: '', equipeVisitante: '', cidade: null, estadio: '', funcao: null,
    valorCentavos: 0, statusPagamento: Core.StatusPagamento.A_RECEBER, dataRecebimento: null, observacoes: '',
  };
  let quantidadePartidas = 1;
  const exigeAtivacao = modo !== 'editar';
  const titulo = modo === 'novo' ? 'Novo jogo' : modo === 'duplicar' ? 'Duplicar jogo' : 'Editar jogo';

  const view = qs('#app-view');
  view.innerHTML = `
    <div class="view view--no-nav-pad">
      ${topBar(titulo)}
      <p class="muted" style="margin:8px 0 16px;">Só data e valor são obrigatórios — complete o resto quando quiser.</p>

      <div class="card">
        <div class="section-header">Informações do jogo</div>
        <div class="field-row">
          <div class="field" style="flex:1.3;">
            <label>Data *</label>
            <input type="text" id="campo-data" placeholder="dd/mm/aaaa" value="${escapeHtml(Core.Dates.formatarData(estado.data))}" />
            <div class="error-text" id="erro-data" style="display:none;"></div>
          </div>
          <div class="field">
            <label>Horário</label>
            <input type="text" id="campo-horario" placeholder="hh:mm" value="${escapeHtml(estado.horario || '')}" />
          </div>
        </div>
        ${campoComOpcoes('competicao', 'Competição', estado.competicao, COMPETICOES_PADRAO)}
        ${campoComOpcoes('modalidade', 'Modalidade', estado.modalidade, MODALIDADES_PADRAO)}
        ${campoComOpcoes('categoria', 'Categoria', estado.categoria, CATEGORIAS_PADRAO)}
      </div>

      <div class="card">
        <div class="section-header">Equipes</div>
        <p class="muted" style="margin-bottom:10px;">Opcional — preencha se já souber os times.</p>
        <div class="field"><label>Equipe mandante</label><input type="text" id="campo-mandante" value="${escapeHtml(estado.equipeMandante || '')}" /></div>
        <div class="field"><label>Equipe visitante</label><input type="text" id="campo-visitante" value="${escapeHtml(estado.equipeVisitante || '')}" /></div>
      </div>

      <div class="card">
        <div class="section-header">Arbitragem</div>
        ${campoComOpcoes('cidade', 'Cidade', estado.cidade, CIDADES_PADRAO)}
        <div class="field"><label>Estádio / ginásio</label><input type="text" id="campo-estadio" value="${escapeHtml(estado.estadio || '')}" /></div>
        ${campoComOpcoes('funcao', 'Função', estado.funcao, FUNCOES_PADRAO)}
      </div>

      <div class="card">
        <div class="section-header">Pagamento</div>
        <div class="field">
          <label id="label-valor">Valor</label>
          <input type="text" id="campo-valor" value="${escapeHtml(Core.Currency.formatar(estado.valorCentavos))}" inputmode="numeric" />
          <div class="error-text" id="erro-valor" style="display:none;"></div>
        </div>
        ${modo !== 'editar' ? `
          <div class="card" style="background:var(--surface-variant); box-shadow:none; margin-bottom:14px;">
            <label style="font-size:13px; font-weight:600; color:var(--on-surface-variant);">Quantidade de partidas</label>
            <div class="stepper" style="margin-top:8px; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap:14px;">
                <button type="button" id="btn-menos" ${quantidadePartidas <= 1 ? 'disabled' : ''}>−</button>
                <span class="value" id="valor-quantidade">${quantidadePartidas}</span>
                <button type="button" id="btn-mais">+</button>
              </div>
              <div id="total-lote" style="text-align:right; display:none;">
                <div class="small muted">Total</div>
                <div style="font-weight:800;" id="total-lote-valor"></div>
              </div>
            </div>
            <p class="muted small" id="texto-lote" style="margin-top:8px; display:none;"></p>
          </div>
        ` : ''}
        <div class="field">
          <label>Status</label>
          <div class="segmented" style="margin-top:6px;">
            <button type="button" data-status="A_RECEBER" class="${estado.statusPagamento === 'A_RECEBER' ? 'active' : ''}">🔴 A receber</button>
            <button type="button" data-status="RECEBIDO" class="${estado.statusPagamento === 'RECEBIDO' ? 'active' : ''}">🟢 Recebido</button>
          </div>
        </div>
        <div class="field" id="campo-data-recebimento-wrap" style="display:${estado.statusPagamento === 'RECEBIDO' ? 'block' : 'none'};">
          <label>Data do recebimento</label>
          <input type="text" id="campo-data-recebimento" placeholder="dd/mm/aaaa" value="${escapeHtml(Core.Dates.formatarData(estado.dataRecebimento || Core.Dates.hojeISO()))}" />
        </div>
      </div>

      <div class="card">
        <div class="section-header">Observações</div>
        <textarea id="campo-observacoes" rows="3">${escapeHtml(estado.observacoes || '')}</textarea>
      </div>

      <button class="btn" id="btn-salvar" style="margin-top:4px;">Salvar</button>
    </div>
  `;
  wireTopBarVoltar(view, () => history.back());

  attachDateMask(qs('#campo-data', view));
  attachHoraMask(qs('#campo-horario', view));
  attachMoedaMask(qs('#campo-valor', view), (centavos) => { estado.valorCentavos = centavos; atualizarTotalLote(); });
  const campoDataReceb = qs('#campo-data-recebimento', view);
  if (campoDataReceb) attachDateMask(campoDataReceb);

  qsa('[data-status]', view).forEach((btn) => {
    btn.onclick = () => {
      estado.statusPagamento = btn.dataset.status;
      qsa('[data-status]', view).forEach((b) => b.classList.toggle('active', b === btn));
      qs('#campo-data-recebimento-wrap', view).style.display = estado.statusPagamento === 'RECEBIDO' ? 'block' : 'none';
    };
  });

  function atualizarTotalLote() {
    const labelValor = qs('#label-valor', view);
    const totalWrap = qs('#total-lote', view);
    const textoLote = qs('#texto-lote', view);
    if (!labelValor) return;
    if (quantidadePartidas > 1) {
      labelValor.textContent = 'Valor por partida';
      totalWrap.style.display = 'block';
      qs('#total-lote-valor', view).textContent = Core.Currency.formatar(estado.valorCentavos * quantidadePartidas);
      textoLote.style.display = 'block';
      textoLote.textContent = `${quantidadePartidas} jogos serão criados, um para cada partida.`;
    } else {
      labelValor.textContent = 'Valor';
      if (totalWrap) totalWrap.style.display = 'none';
      if (textoLote) textoLote.style.display = 'none';
    }
  }
  const btnMenos = qs('#btn-menos', view), btnMais = qs('#btn-mais', view);
  if (btnMenos) btnMenos.onclick = () => { if (quantidadePartidas > 1) { quantidadePartidas--; qs('#valor-quantidade', view).textContent = quantidadePartidas; btnMenos.disabled = quantidadePartidas <= 1; atualizarTotalLote(); } };
  if (btnMais) btnMais.onclick = () => { if (quantidadePartidas < 30) { quantidadePartidas++; qs('#valor-quantidade', view).textContent = quantidadePartidas; btnMenos.disabled = false; atualizarTotalLote(); } };

  function coletarEValidar() {
    let valido = true;
    const dataTexto = qs('#campo-data', view).value;
    const dataParsed = Core.Dates.parseData(dataTexto);
    qs('#erro-data', view).style.display = 'none';
    if (!dataParsed) {
      qs('#erro-data', view).textContent = 'Informe uma data válida (dd/mm/aaaa).';
      qs('#erro-data', view).style.display = 'block';
      valido = false;
    }
    qs('#erro-valor', view).style.display = 'none';
    if (!estado.valorCentavos || estado.valorCentavos <= 0) {
      qs('#erro-valor', view).textContent = 'Informe um valor maior que zero.';
      qs('#erro-valor', view).style.display = 'block';
      valido = false;
    }
    if (!valido) return null;
    const horarioTexto = qs('#campo-horario', view).value;
    return {
      ...estado,
      data: dataParsed,
      horario: Core.Dates.parseHora(horarioTexto),
      competicao: valorCampo(view, 'competicao'),
      modalidade: valorCampo(view, 'modalidade'),
      categoria: valorCampo(view, 'categoria'),
      equipeMandante: qs('#campo-mandante', view).value.trim() || null,
      equipeVisitante: qs('#campo-visitante', view).value.trim() || null,
      cidade: valorCampo(view, 'cidade'),
      estadio: qs('#campo-estadio', view).value.trim() || null,
      funcao: valorCampo(view, 'funcao'),
      dataRecebimento: estado.statusPagamento === 'RECEBIDO' ? (Core.Dates.parseData(campoDataReceb.value) || Core.Dates.hojeISO()) : null,
      observacoes: qs('#campo-observacoes', view).value.trim() || null,
    };
  }

  async function salvar() {
    const dados = coletarEValidar();
    if (!dados) return;
    if (modo !== 'editar' && quantidadePartidas > 1) {
      const jogosLote = Array.from({ length: quantidadePartidas }, () => ({ ...dados, id: 0 }));
      await Db.salvarVariosJogos(jogosLote);
    } else {
      await Db.salvarJogo(dados.id ? dados : { ...dados, id: 0 });
    }
    await recarregarJogos();
    toast(modo === 'editar' ? 'Jogo atualizado.' : 'Jogo salvo.');
    history.back();
  }

  qs('#btn-salvar', view).onclick = () => {
    if (exigeAtivacao && !estaAtivado()) {
      mostrarAtivacaoDialog(salvar);
    } else {
      salvar();
    }
  };

  atualizarTotalLote();
}

function campoComOpcoes(id, label, valor, opcoes) {
  const listId = `datalist-${id}`;
  return `
    <div class="field">
      <label>${escapeHtml(label)}</label>
      <input type="text" id="campo-${id}" list="${listId}" value="${escapeHtml(valor || '')}" autocomplete="off" />
      <datalist id="${listId}">${opcoes.map((o) => `<option value="${escapeHtml(o)}"></option>`).join('')}</datalist>
    </div>
  `;
}
function valorCampo(root, id) {
  const v = qs(`#campo-${id}`, root).value.trim();
  return v || null;
}
