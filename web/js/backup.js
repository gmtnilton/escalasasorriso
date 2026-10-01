// Backup/restauração/exportação — MESMO formato de arquivo usado pelo app
// Android (ver android/app/.../data/backup/BackupManager.kt e
// ExportManager.kt), para que o arquivo .json gerado por um lado possa ser
// restaurado no outro sem nenhuma conversão.
'use strict';

const Backup = {
  VERSAO_BACKUP: 3,

  nomeArquivoBackup() {
    return `meus-jogos-arbitragem-backup-${Core.Dates.hojeISO()}.json`;
  },

  nomeArquivoCsv() {
    return `meus-jogos-arbitragem-${Core.Dates.hojeISO()}.csv`;
  },

  gerarBackup(jogos) {
    const raiz = {
      versaoBackup: this.VERSAO_BACKUP,
      exportadoEm: new Date().toISOString(),
      totalJogos: jogos.length,
      jogos: jogos.map((j) => ({
        id: j.id,
        data: j.data,
        horario: j.horario ?? null,
        competicao: j.competicao ?? null,
        modalidade: j.modalidade ?? null,
        categoria: j.categoria ?? null,
        equipeMandante: j.equipeMandante ?? null,
        equipeVisitante: j.equipeVisitante ?? null,
        cidade: j.cidade ?? null,
        estadio: j.estadio ?? null,
        funcao: j.funcao ?? null,
        valorCentavos: j.valorCentavos,
        statusPagamento: j.statusPagamento,
        dataRecebimento: j.dataRecebimento ?? null,
        observacoes: j.observacoes ?? null,
        dataCriacao: j.dataCriacao,
        dataAtualizacao: j.dataAtualizacao,
      })),
    };
    return JSON.stringify(raiz, null, 2);
  },

  /** Lê um backup FEITO POR ESTE SISTEMA (Android ou Web) — retorna a lista de jogos. */
  parseBackupProprio(conteudoJson) {
    const raiz = JSON.parse(conteudoJson);
    if (!raiz || !Array.isArray(raiz.jogos)) {
      throw new Error('Esse arquivo não parece um backup deste app. Se ele vem de outro sistema de controle de jogos, use "Importar de outro sistema".');
    }
    return raiz.jogos.map((o) => ({
      id: o.id || 0,
      data: o.data,
      horario: o.horario ?? null,
      competicao: o.competicao ?? null,
      modalidade: o.modalidade ?? null,
      categoria: o.categoria ?? null,
      equipeMandante: o.equipeMandante ?? null,
      equipeVisitante: o.equipeVisitante ?? null,
      cidade: o.cidade ?? null,
      estadio: o.estadio ?? (o.local ?? null),
      funcao: o.funcao ?? null,
      valorCentavos: o.valorCentavos,
      statusPagamento: o.statusPagamento,
      dataRecebimento: o.dataRecebimento ?? null,
      observacoes: o.observacoes ?? null,
      dataCriacao: o.dataCriacao || new Date().toISOString(),
      dataAtualizacao: o.dataAtualizacao || new Date().toISOString(),
    }));
  },

  /** Formato de outro sistema: {"records":[{date,city,modality,competition,venue,qty,value,paid}]}. */
  parseFormatoRecords(conteudoJson) {
    const raiz = JSON.parse(conteudoJson);
    if (!raiz || !Array.isArray(raiz.records)) {
      throw new Error('Não reconheci o formato desse arquivo (esperava uma lista "records").');
    }
    const jogos = [];
    for (const r of raiz.records) {
      const data = r.date;
      const pago = !!r.paid;
      const valorPorPartidaCentavos = Math.round((Number(r.value) || 0) * 100);
      const quantidade = Math.max(1, parseInt(r.qty, 10) || 1);
      const base = {
        id: 0,
        data,
        horario: null,
        competicao: r.competition ?? null,
        modalidade: r.modality ?? null,
        categoria: null,
        equipeMandante: null,
        equipeVisitante: null,
        cidade: r.city ?? null,
        estadio: r.venue ?? null,
        funcao: null,
        valorCentavos: valorPorPartidaCentavos,
        statusPagamento: pago ? Core.StatusPagamento.RECEBIDO : Core.StatusPagamento.A_RECEBER,
        dataRecebimento: pago ? data : null,
        observacoes: null,
        dataCriacao: new Date().toISOString(),
        dataAtualizacao: new Date().toISOString(),
      };
      for (let i = 0; i < quantidade; i++) jogos.push({ ...base });
    }
    return jogos;
  },

  // ---------------- CSV ----------------

  CABECALHO_CSV: ['Data', 'Horário', 'Competição', 'Modalidade', 'Categoria', 'Equipe mandante', 'Equipe visitante',
    'Cidade', 'Estádio/Ginásio', 'Função', 'Valor', 'Status', 'Data do recebimento', 'Observações'],

  _csvCampo(valor) {
    const texto = valor == null ? '' : String(valor);
    const precisaAspas = texto.includes(';') || texto.includes('"') || texto.includes('\n') || texto.includes('\r');
    const escapado = texto.replace(/"/g, '""');
    return precisaAspas ? `"${escapado}"` : escapado;
  },

  gerarCsv(jogos) {
    const linhas = [];
    linhas.push(this.CABECALHO_CSV.map((c) => this._csvCampo(c)).join(';'));
    for (const j of jogos) {
      const status = j.statusPagamento === Core.StatusPagamento.RECEBIDO ? 'RECEBIDO' : 'A RECEBER';
      const campos = [
        Core.Dates.formatarData(j.data),
        j.horario ? Core.Dates.formatarHora(j.horario) : '',
        j.competicao || '',
        j.modalidade || '',
        j.categoria || '',
        j.equipeMandante || '',
        j.equipeVisitante || '',
        j.cidade || '',
        j.estadio || '',
        j.funcao || '',
        Core.Currency.formatar(j.valorCentavos),
        status,
        j.dataRecebimento ? Core.Dates.formatarData(j.dataRecebimento) : '',
        j.observacoes || '',
      ];
      linhas.push(campos.map((c) => this._csvCampo(c)).join(';'));
    }
    return '﻿' + linhas.join('\r\n') + '\r\n';
  },
};

if (typeof window !== 'undefined') window.Backup = Backup;
