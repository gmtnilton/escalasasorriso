// Núcleo de regras de negócio — porta fiel de android/core (Kotlin) para
// JavaScript puro, sem dependências. Mantém os MESMOS nomes de campo e a
// MESMA lógica do app Android, para que os dois lados nunca divirjam.
'use strict';

const Core = {};

// ---------------------------------------------------------------------
// StatusPagamento
// ---------------------------------------------------------------------
Core.StatusPagamento = { A_RECEBER: 'A_RECEBER', RECEBIDO: 'RECEBIDO' };

Core.jogoRecebido = (jogo) => jogo.statusPagamento === Core.StatusPagamento.RECEBIDO;

Core.temEquipes = (jogo) => !!(jogo.equipeMandante && jogo.equipeMandante.trim()) &&
  !!(jogo.equipeVisitante && jogo.equipeVisitante.trim());

Core.confronto = (jogo) => Core.temEquipes(jogo) ? `${jogo.equipeMandante} x ${jogo.equipeVisitante}` : null;

// ---------------------------------------------------------------------
// Ativação (REGRA: código de ativação local, 100% offline)
// ---------------------------------------------------------------------
const CODIGO_ATIVACAO = 'Apito Ativo';
Core.codigoAtivacaoValido = (digitado) =>
  (digitado || '').trim().toLowerCase() === CODIGO_ATIVACAO.toLowerCase();

// ---------------------------------------------------------------------
// CurrencyUtils
// ---------------------------------------------------------------------
Core.Currency = {
  // 125000 -> "R$ 1.250,00"
  formatar(centavos) {
    const negativo = centavos < 0;
    const abs = Math.abs(Math.round(centavos));
    const reais = Math.floor(abs / 100);
    const parteCentavos = abs % 100;
    const sinal = negativo ? '-' : '';
    return `${sinal}R$ ${Core.Currency._milhar(reais)},${String(parteCentavos).padStart(2, '0')}`;
  },
  _milhar(valor) {
    const texto = String(valor);
    let out = '';
    for (let i = 0; i < texto.length; i++) {
      const posDaDireita = texto.length - i;
      if (i !== 0 && posDaDireita % 3 === 0) out += '.';
      out += texto[i];
    }
    return out;
  },
  // Máscara de digitação: cada dígito entra pela direita (ex.: "4","0","0" -> R$ 4,00)
  paraCentavosMascarado(textoDigitado) {
    const somenteDigitos = (textoDigitado || '').replace(/\D/g, '').replace(/^0+/, '');
    if (!somenteDigitos) return 0;
    const n = parseInt(somenteDigitos, 10);
    return Number.isFinite(n) ? n : 0;
  },
  // "R$ 1.250,00" / "1250,50" / "1250.50" -> centavos (ou null)
  paraCentavosDeTextoFormatado(texto) {
    let limpo = (texto || '').trim().replace(/^R\$/i, '').trim();
    if (!limpo) return null;
    let normalizado;
    if (limpo.includes(',')) {
      normalizado = limpo.replace(/\./g, '').replace(',', '.');
    } else {
      normalizado = limpo;
    }
    const valor = Number(normalizado);
    if (!Number.isFinite(valor)) return null;
    return Math.round(valor * 100);
  },
};

// ---------------------------------------------------------------------
// DateUtils (formato brasileiro dd/MM/yyyy) — datas guardadas como string
// ISO "YYYY-MM-DD" (equivalente a LocalDate) e horas como "HH:mm".
// ---------------------------------------------------------------------
const NOMES_MESES = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];

Core.Dates = {
  hojeISO() {
    const d = new Date();
    return Core.Dates.dateParaISO(d);
  },
  dateParaISO(d) {
    const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  },
  // "YYYY-MM-DD" -> "dd/MM/yyyy"
  formatarData(iso) {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  },
  formatarDataCurta(iso) {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y.slice(2)}`;
  },
  formatarHora(hhmm) {
    return hhmm || '';
  },
  nomeMesAno(ano, mes) {
    return `${NOMES_MESES[mes - 1].toUpperCase()}/${ano}`;
  },
  nomeMes(mes) {
    return NOMES_MESES[mes - 1];
  },
  // "dd/MM/yyyy" digitado -> "YYYY-MM-DD" ou null
  parseData(texto) {
    const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec((texto || '').trim());
    if (!m) return null;
    const [, d, mo, y] = m;
    const dia = Number(d), mes = Number(mo), ano = Number(y);
    if (mes < 1 || mes > 12) return null;
    const diasNoMes = new Date(ano, mes, 0).getDate();
    if (dia < 1 || dia > diasNoMes) return null;
    return `${y}-${mo}-${d}`;
  },
  aplicarMascaraData(digitos) {
    const d = (digitos || '').replace(/\D/g, '').slice(0, 8);
    let out = '';
    for (let i = 0; i < d.length; i++) {
      if (i === 2 || i === 4) out += '/';
      out += d[i];
    }
    return out;
  },
  aplicarMascaraHora(digitos) {
    const h = (digitos || '').replace(/\D/g, '').slice(0, 4);
    let out = '';
    for (let i = 0; i < h.length; i++) {
      if (i === 2) out += ':';
      out += h[i];
    }
    return out;
  },
  parseHora(texto) {
    const m = /^(\d{2}):(\d{2})$/.exec((texto || '').trim());
    if (!m) return null;
    const [, h, mi] = m;
    if (Number(h) > 23 || Number(mi) > 59) return null;
    return `${h}:${mi}`;
  },
  // Comparação lexicográfica funciona pois ambas são "YYYY-MM-DD"
  cmp(a, b) { return a < b ? -1 : a > b ? 1 : 0; },
};

// ---------------------------------------------------------------------
// FiltroJogos / FiltroLogica
// ---------------------------------------------------------------------
Core.FiltroStatus = { TODOS: 'TODOS', A_RECEBER: 'A_RECEBER', RECEBIDOS: 'RECEBIDOS' };

Core.filtroPadrao = () => ({
  status: Core.FiltroStatus.TODOS,
  periodo: { tipo: 'Todos' },
  competicao: null,
  modalidade: null,
  funcao: null,
  cidade: null,
  pesquisa: '',
});

Core.filtroAtivo = (f) => f.status !== Core.FiltroStatus.TODOS ||
  f.periodo.tipo !== 'Todos' ||
  !!(f.competicao && f.competicao.trim()) ||
  !!(f.modalidade && f.modalidade.trim()) ||
  !!(f.funcao && f.funcao.trim()) ||
  !!(f.cidade && f.cidade.trim());

function segunda(d) { const dia = d.getDay(); const diff = (dia === 0 ? -6 : 1) - dia; const r = new Date(d); r.setDate(d.getDate() + diff); return r; }
function domingo(d) { const s = segunda(d); const r = new Date(s); r.setDate(s.getDate() + 6); return r; }

Core.periodoParaIntervalo = (periodo, hojeISO = Core.Dates.hojeISO()) => {
  const hoje = new Date(hojeISO + 'T00:00:00');
  switch (periodo.tipo) {
    case 'Todos': return null;
    case 'Hoje': return [hojeISO, hojeISO];
    case 'EstaSemana': return [Core.Dates.dateParaISO(segunda(hoje)), Core.Dates.dateParaISO(domingo(hoje))];
    case 'EsteMes': {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
      const fim = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);
      return [Core.Dates.dateParaISO(inicio), Core.Dates.dateParaISO(fim)];
    }
    case 'MesAnterior': {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
      const fim = new Date(hoje.getFullYear(), hoje.getMonth(), 0);
      return [Core.Dates.dateParaISO(inicio), Core.Dates.dateParaISO(fim)];
    }
    case 'EsteAno': return [`${hoje.getFullYear()}-01-01`, `${hoje.getFullYear()}-12-31`];
    case 'Personalizado': return [periodo.inicio, periodo.fim];
    default: return null;
  }
};

function correspondeA(jogo, query) {
  if (!query || !query.trim()) return true;
  const termo = query.trim().toLowerCase();
  const campos = [jogo.equipeMandante, jogo.equipeVisitante, jogo.competicao, jogo.modalidade,
    jogo.categoria, jogo.cidade, jogo.estadio, jogo.funcao, jogo.observacoes];
  return campos.some((c) => c && String(c).toLowerCase().includes(termo));
}

function eqCI(a, b) { return (a || '').trim().toLowerCase() === (b || '').trim().toLowerCase(); }

Core.filtrarEPesquisar = (jogos, filtro, hojeISO = Core.Dates.hojeISO()) => {
  const intervalo = Core.periodoParaIntervalo(filtro.periodo, hojeISO);
  return jogos.filter((jogo) => {
    const statusOk = filtro.status === Core.FiltroStatus.TODOS ? true :
      filtro.status === Core.FiltroStatus.A_RECEBER ? jogo.statusPagamento === Core.StatusPagamento.A_RECEBER :
        jogo.statusPagamento === Core.StatusPagamento.RECEBIDO;
    const periodoOk = !intervalo || (jogo.data >= intervalo[0] && jogo.data <= intervalo[1]);
    const competicaoOk = !(filtro.competicao && filtro.competicao.trim()) || eqCI(jogo.competicao, filtro.competicao);
    const modalidadeOk = !(filtro.modalidade && filtro.modalidade.trim()) || eqCI(jogo.modalidade, filtro.modalidade);
    const funcaoOk = !(filtro.funcao && filtro.funcao.trim()) || eqCI(jogo.funcao, filtro.funcao);
    const cidadeOk = !(filtro.cidade && filtro.cidade.trim()) || eqCI(jogo.cidade, filtro.cidade);
    const pesquisaOk = correspondeA(jogo, filtro.pesquisa);
    return statusOk && periodoOk && competicaoOk && modalidadeOk && funcaoOk && cidadeOk && pesquisaOk;
  });
};

Core.ordenarMaisRecentePrimeiro = (jogos) =>
  [...jogos].sort((a, b) => Core.Dates.cmp(b.data, a.data) || (b.id - a.id));

// ---------------------------------------------------------------------
// Totais
// ---------------------------------------------------------------------
Core.totalAReceberCentavos = (jogos) => jogos.filter((j) => j.statusPagamento === Core.StatusPagamento.A_RECEBER).reduce((s, j) => s + j.valorCentavos, 0);
Core.totalRecebidoCentavos = (jogos) => jogos.filter((j) => j.statusPagamento === Core.StatusPagamento.RECEBIDO).reduce((s, j) => s + j.valorCentavos, 0);
Core.totalGeralCentavos = (jogos) => jogos.reduce((s, j) => s + j.valorCentavos, 0);
Core.quantidadeRecebidos = (jogos) => jogos.filter((j) => j.statusPagamento === Core.StatusPagamento.RECEBIDO).length;
Core.quantidadeAReceber = (jogos) => jogos.filter((j) => j.statusPagamento === Core.StatusPagamento.A_RECEBER).length;

Core.paraResumoPeriodo = (jogos) => ({
  totalJogos: jogos.length,
  totalRecebidoCentavos: Core.totalRecebidoCentavos(jogos),
  totalAReceberCentavos: Core.totalAReceberCentavos(jogos),
  get totalGeralCentavos() { return this.totalRecebidoCentavos + this.totalAReceberCentavos; },
});

Core.resumoDoMes = (jogos, ano, mes) =>
  Core.paraResumoPeriodo(jogos.filter((j) => Number(j.data.slice(0, 4)) === ano && Number(j.data.slice(5, 7)) === mes));

Core.resumoDoAno = (jogos, ano) => {
  const doAno = jogos.filter((j) => Number(j.data.slice(0, 4)) === ano);
  const totalRecebidoCentavos = Core.totalRecebidoCentavos(doAno);
  const totalAReceberCentavos = Core.totalAReceberCentavos(doAno);
  const totalGeralCentavos = totalRecebidoCentavos + totalAReceberCentavos;
  return {
    ano, totalJogos: doAno.length, totalRecebidoCentavos, totalAReceberCentavos, totalGeralCentavos,
    mediaPorJogoCentavos: doAno.length === 0 ? 0 : Math.trunc(totalGeralCentavos / doAno.length),
  };
};

Core.estatisticas = (jogos) => {
  if (jogos.length === 0) return { totalJogos: 0, totalRecebidoCentavos: 0, totalAReceberCentavos: 0, totalGeralCentavos: 0, jogosRecebidos: 0, jogosPendentes: 0, maiorValorCentavos: 0, mediaPorJogoCentavos: 0 };
  const totalRecebidoCentavos = Core.totalRecebidoCentavos(jogos);
  const totalAReceberCentavos = Core.totalAReceberCentavos(jogos);
  const totalGeralCentavos = totalRecebidoCentavos + totalAReceberCentavos;
  return {
    totalJogos: jogos.length, totalRecebidoCentavos, totalAReceberCentavos, totalGeralCentavos,
    jogosRecebidos: Core.quantidadeRecebidos(jogos), jogosPendentes: Core.quantidadeAReceber(jogos),
    maiorValorCentavos: Math.max(...jogos.map((j) => j.valorCentavos)),
    mediaPorJogoCentavos: Math.trunc(totalGeralCentavos / jogos.length),
  };
};

Core.serieMensal = (jogos, ano) => Array.from({ length: 12 }, (_, i) => {
  const mes = i + 1;
  const doMes = jogos.filter((j) => Number(j.data.slice(0, 4)) === ano && Number(j.data.slice(5, 7)) === mes);
  const totalRecebidoCentavos = Core.totalRecebidoCentavos(doMes);
  const totalAReceberCentavos = Core.totalAReceberCentavos(doMes);
  return { ano, mes, totalJogos: doMes.length, totalRecebidoCentavos, totalAReceberCentavos, totalGeralCentavos: totalRecebidoCentavos + totalAReceberCentavos };
});

Core.anosDisponiveis = (jogos) => [...new Set(jogos.map((j) => Number(j.data.slice(0, 4))))].sort((a, b) => b - a);

Core.competicoesDisponiveis = (jogos) => [...new Set(jogos.map((j) => j.competicao).filter((v) => v && v.trim()))].sort((a, b) => a.localeCompare(b, 'pt-BR'));
Core.funcoesDisponiveis = (jogos) => [...new Set(jogos.map((j) => j.funcao).filter((v) => v && v.trim()))].sort((a, b) => a.localeCompare(b, 'pt-BR'));
Core.modalidadesDisponiveis = (jogos) => [...new Set(jogos.map((j) => j.modalidade).filter((v) => v && v.trim()))].sort((a, b) => a.localeCompare(b, 'pt-BR'));

function agruparIgnorandoCaixa(nomes) {
  const grupos = new Map(); // chave upper-trim -> {variantes: Map(nomeTrim->count)}
  for (const nomeOriginal of nomes) {
    const nome = nomeOriginal.trim();
    const chave = nome.toUpperCase();
    if (!grupos.has(chave)) grupos.set(chave, new Map());
    const variantes = grupos.get(chave);
    variantes.set(nome, (variantes.get(nome) || 0) + 1);
  }
  const out = [];
  for (const variantes of grupos.values()) {
    let melhorNome = null, melhorCount = -1, total = 0;
    for (const [nome, count] of variantes) {
      total += count;
      if (count > melhorCount) { melhorCount = count; melhorNome = nome; }
    }
    out.push({ nome: melhorNome, quantidade: total });
  }
  return out;
}

Core.cidadesDisponiveis = (jogos) => agruparIgnorandoCaixa(jogos.map((j) => j.cidade).filter((v) => v && v.trim()))
  .map((g) => g.nome).sort((a, b) => a.localeCompare(b, 'pt-BR'));

Core.contarPorCidade = (jogos) => agruparIgnorandoCaixa(jogos.map((j) => j.cidade).filter((v) => v && v.trim()))
  .map((g) => [g.nome, g.quantidade]).sort((a, b) => b[1] - a[1]);

Core.jogosDaCidade = (jogos, cidade) => jogos.filter((j) => eqCI(j.cidade, cidade));

function contarPorCampo(jogos, campo) {
  const contagem = new Map();
  for (const j of jogos) {
    const v = j[campo];
    if (v && v.trim()) contagem.set(v, (contagem.get(v) || 0) + 1);
  }
  return [...contagem.entries()].sort((a, b) => b[1] - a[1]);
}
Core.contarPorModalidade = (jogos) => contarPorCampo(jogos, 'modalidade');
Core.contarPorCompeticao = (jogos) => contarPorCampo(jogos, 'competicao');
Core.jogosDaCompeticao = (jogos, competicao) => jogos.filter((j) => eqCI(j.competicao, competicao));
Core.jogosDaModalidade = (jogos, modalidade) => jogos.filter((j) => eqCI(j.modalidade, modalidade));

Core.duplicarJogo = (original, agoraISO = new Date().toISOString()) => ({
  ...original,
  id: 0,
  statusPagamento: Core.StatusPagamento.A_RECEBER,
  dataRecebimento: null,
  dataCriacao: agoraISO,
  dataAtualizacao: agoraISO,
});

Core.marcarComoRecebido = (jogo, dataRecebimentoISO = Core.Dates.hojeISO(), agoraISO = new Date().toISOString()) => ({
  ...jogo, statusPagamento: Core.StatusPagamento.RECEBIDO, dataRecebimento: dataRecebimentoISO, dataAtualizacao: agoraISO,
});

Core.desfazerRecebimento = (jogo, agoraISO = new Date().toISOString()) => ({
  ...jogo, statusPagamento: Core.StatusPagamento.A_RECEBER, dataRecebimento: null, dataAtualizacao: agoraISO,
});

if (typeof window !== 'undefined') window.Core = Core;
