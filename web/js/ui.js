// Infraestrutura de UI: roteador por hash, estado global, toasts, sheets,
// diálogos e a barra de navegação inferior (mesmas 5 abas do Android).
'use strict';

const State = {
  jogos: [],
  carregado: false,
  filtroJogos: Core.filtroPadrao(),
  filtroRecebimento: { ...Core.filtroPadrao(), status: Core.FiltroStatus.A_RECEBER },
  selecionadosRecebimento: new Set(),
  agrupamentoJogosTela: null, // { tipo: 'cidade'|'modalidade', nome } — nível de drill-down atual na aba Jogos
};

const NAV_ITEMS = [
  { rota: '#/dashboard', icone: '🏠', titulo: 'Início' },
  { rota: '#/jogos', icone: '⚽', titulo: 'Jogos' },
  { rota: '#/recebimento', icone: '📥', titulo: 'Receber' },
  { rota: '#/resumo', icone: '📊', titulo: 'Resumo' },
  { rota: '#/configuracoes', icone: '⚙️', titulo: 'Config' },
];

// ---------------------------------------------------------------------
// Helpers de DOM
// ---------------------------------------------------------------------
function escapeHtml(s) {
  if (s == null) return '';
  return String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function h(html) {
  const tpl = document.createElement('template');
  tpl.innerHTML = html.trim();
  return tpl.content.firstElementChild;
}

function qs(sel, root = document) { return root.querySelector(sel); }
function qsa(sel, root = document) { return [...root.querySelectorAll(sel)]; }

/** Aplica a máscara dd/mm/aaaa enquanto o usuário digita num <input type=text>. */
function attachDateMask(input) {
  input.addEventListener('input', () => { input.value = Core.Dates.aplicarMascaraData(input.value); });
}
/** Aplica a máscara HH:mm enquanto o usuário digita num <input type=text>. */
function attachHoraMask(input) {
  input.addEventListener('input', () => { input.value = Core.Dates.aplicarMascaraHora(input.value); });
}
/** Dispara o download de um arquivo gerado em memória (string ou Blob). */
function baixarArquivo(conteudoOuBlob, nomeArquivo, tipo) {
  const blob = conteudoOuBlob instanceof Blob ? conteudoOuBlob : new Blob([conteudoOuBlob], { type: tipo });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = nomeArquivo;
  document.body.appendChild(a); a.click(); a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 2000);
}

/** Aplica a máscara de valor monetário (R$ 0,00) — dígitos entram pela direita. */
function attachMoedaMask(input, onChangeCentavos) {
  input.addEventListener('input', () => {
    const centavos = Core.Currency.paraCentavosMascarado(input.value);
    input.value = Core.Currency.formatar(centavos);
    if (onChangeCentavos) onChangeCentavos(centavos);
    const len = input.value.length;
    input.setSelectionRange(len, len);
  });
}

// ---------------------------------------------------------------------
// Toast
// ---------------------------------------------------------------------
function toast(mensagem, isError = false) {
  const host = qs('#toast-host');
  const t = h(`<div class="toast${isError ? ' error' : ''}">${escapeHtml(mensagem)}</div>`);
  host.appendChild(t);
  setTimeout(() => { t.style.transition = 'opacity 0.3s'; t.style.opacity = '0'; setTimeout(() => t.remove(), 300); }, 3200);
}

// ---------------------------------------------------------------------
// Bottom sheet
// ---------------------------------------------------------------------
let sheetOverlayEl = null;
function openSheet(innerHtml) {
  closeSheet();
  const overlay = h(`<div class="overlay"><div class="sheet"><div class="sheet-handle"></div>${innerHtml}</div></div>`);
  overlay.addEventListener('click', (ev) => { if (ev.target === overlay) closeSheet(); });
  document.body.appendChild(overlay);
  sheetOverlayEl = overlay;
  return overlay;
}
function closeSheet() {
  if (sheetOverlayEl) { sheetOverlayEl.remove(); sheetOverlayEl = null; }
}

// ---------------------------------------------------------------------
// Dialog (central)
// ---------------------------------------------------------------------
let dialogOverlayEl = null;
function openDialog(innerHtml) {
  closeDialog();
  const overlay = h(`<div class="overlay overlay--center"><div class="dialog">${innerHtml}</div></div>`);
  overlay.addEventListener('click', (ev) => { if (ev.target === overlay) closeDialog(); });
  document.body.appendChild(overlay);
  dialogOverlayEl = overlay;
  return overlay;
}
function closeDialog() {
  if (dialogOverlayEl) { dialogOverlayEl.remove(); dialogOverlayEl = null; }
}

function confirmarAcaoDialog({ titulo, mensagem, textoConfirmar = 'Confirmar', perigo = false, onConfirmar }) {
  const overlay = openDialog(`
    <div class="title">${escapeHtml(titulo)}</div>
    <p class="muted" style="margin-top:8px;">${escapeHtml(mensagem)}</p>
    <div class="btn-row-h">
      <button class="btn btn--text" data-acao="cancelar">Cancelar</button>
      <button class="btn btn--text${perigo ? '-error' : ''}" data-acao="confirmar">${escapeHtml(textoConfirmar)}</button>
    </div>
  `);
  qs('[data-acao="cancelar"]', overlay).onclick = closeDialog;
  qs('[data-acao="confirmar"]', overlay).onclick = () => { closeDialog(); onConfirmar(); };
}

// ---------------------------------------------------------------------
// Ativação (REGRA: só cadastrar jogo exige o código, nunca navegar/ver)
// ---------------------------------------------------------------------
function estaAtivado() { return localStorage.getItem('ativado') === '1'; }

function mostrarAtivacaoDialog(onAtivado) {
  const overlay = openDialog(`
    <div class="activation-lock-icon">🔐</div>
    <div class="title" style="text-align:center;">Ativação necessária</div>
    <p class="muted" style="margin-top:10px; text-align:center;">Para cadastrar uma escala, informe o código de ativação.</p>
    <div class="field" style="margin-top:14px;">
      <input type="text" id="campo-codigo-ativacao" placeholder="Digite o código de ativação" autocomplete="off" />
      <div class="error-text" id="erro-codigo-ativacao" style="display:none;">Código de ativação inválido.</div>
    </div>
    <div class="btn-row-h">
      <button class="btn btn--text" data-acao="cancelar">Cancelar</button>
      <button class="btn btn--sm" data-acao="ativar" style="width:auto;">ATIVAR</button>
    </div>
  `);
  qs('[data-acao="cancelar"]', overlay).onclick = closeDialog;
  const input = qs('#campo-codigo-ativacao', overlay);
  input.focus();
  function tentar() {
    if (Core.codigoAtivacaoValido(input.value)) {
      localStorage.setItem('ativado', '1');
      closeDialog();
      onAtivado();
    } else {
      qs('#erro-codigo-ativacao', overlay).style.display = 'block';
    }
  }
  qs('[data-acao="ativar"]', overlay).onclick = tentar;
  input.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') tentar(); });
}

// ---------------------------------------------------------------------
// Dados
// ---------------------------------------------------------------------
async function recarregarJogos() {
  State.jogos = await Db.listarJogos();
  State.carregado = true;
}

// ---------------------------------------------------------------------
// Roteador por hash
// ---------------------------------------------------------------------
const ROUTES = [
  { re: /^#\/dashboard$/, view: () => renderDashboard() },
  { re: /^#\/jogos$/, view: () => renderJogosList() },
  { re: /^#\/recebimento$/, view: () => renderRecebimento() },
  { re: /^#\/resumo$/, view: () => renderResumo() },
  { re: /^#\/configuracoes$/, view: () => renderConfiguracoes() },
  { re: /^#\/jogo\/novo$/, view: () => renderJogoForm(null, false) },
  { re: /^#\/jogo\/(\d+)\/duplicar$/, view: (m) => renderJogoForm(Number(m[1]), true) },
  { re: /^#\/jogo\/(\d+)\/editar$/, view: (m) => renderJogoForm(Number(m[1]), false, true) },
  { re: /^#\/jogo\/(\d+)$/, view: (m) => renderJogoDetail(Number(m[1])) },
  { re: /^#\/recibo\/(\d+)$/, view: (m) => renderRecibo(Number(m[1])) },
];

function navigate(hash) {
  if (location.hash === hash) { renderCurrentView(); } else { location.hash = hash; }
}

function currentMainRoute() {
  const h = location.hash || '#/dashboard';
  return NAV_ITEMS.find((n) => n.rota === h)?.rota || null;
}

function renderBottomNav() {
  const ativo = currentMainRoute();
  const host = qs('#bottom-nav');
  if (!ativo) { host.innerHTML = ''; host.style.display = 'none'; return; }
  host.style.display = 'flex';
  host.innerHTML = NAV_ITEMS.map((item) => `
    <button class="${item.rota === ativo ? 'active' : ''}" data-rota="${item.rota}">
      <span class="nav-icon">${item.icone}</span>
      <span>${item.titulo}</span>
    </button>
  `).join('');
  qsa('button', host).forEach((btn) => {
    btn.onclick = () => navigate(btn.dataset.rota);
  });
}

function topBar(titulo, onVoltar) {
  return `
    <div class="top-bar">
      <button class="icon-btn" data-acao="voltar">←</button>
      <div class="title">${escapeHtml(titulo)}</div>
    </div>
  `;
}
function wireTopBarVoltar(root, onVoltar) {
  const btn = qs('[data-acao="voltar"]', root);
  if (btn) btn.onclick = onVoltar;
}

async function renderCurrentView() {
  closeSheet();
  closeDialog();
  const hash = location.hash || '#/dashboard';
  const match = ROUTES.find((r) => r.re.test(hash));
  renderBottomNav();
  window.scrollTo(0, 0);
  if (!match) { navigate('#/dashboard'); return; }
  const m = hash.match(match.re);
  await match.view(m);
}

window.addEventListener('hashchange', renderCurrentView);

async function bootApp() {
  await recarregarJogos();
  if (!location.hash) location.hash = '#/dashboard';
  await renderCurrentView();
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(() => {});
  }
}
