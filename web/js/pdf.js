// Geração do recibo de pagamento em PDF — mesmo layout e cores do gerador
// Android (ReciboPdfGenerator.kt): título, número, valor em destaque, dados
// do pagador/recebedor, identificação do jogo e linhas de assinatura.
// Usa jsPDF (carregado via CDN no index.html).
'use strict';

const Pdf = {
  LARGURA_A4: 595,
  ALTURA_A4: 842,
  MARGEM: 40,

  nomeArquivo(jogoId) {
    return `recibo-jogo-${jogoId}.pdf`;
  },

  /** Retorna um Blob do PDF do recibo (jogo pode ser null). */
  gerarReciboBlob(recibo, jogo) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF({ unit: 'pt', format: [this.LARGURA_A4, this.ALTURA_A4] });

    const corTitulo = [15, 42, 82]; // #0F2A52
    const corDourado = [138, 109, 0]; // #8A6D00
    const corMuted = [92, 102, 120]; // #5C6678
    const corTexto = [21, 24, 29]; // #15181D
    const corLinha = [224, 228, 234]; // #E0E4EA
    const corFundoValor = [234, 241, 251]; // #EAF1FB

    const LARGURA = this.LARGURA_A4, MARGEM = this.MARGEM;
    let y = MARGEM + 18;

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(18);
    doc.setTextColor(...corTitulo);
    doc.text('RECIBO DE PAGAMENTO', MARGEM, y);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9.5);
    doc.setTextColor(...corMuted);
    doc.text(reciboNumeroFormatado(recibo), LARGURA - MARGEM, y, { align: 'right' });
    y += 14;
    doc.text(`Emitido em ${Core.Dates.formatarData(Core.Dates.hojeISO())}`, LARGURA - MARGEM, y, { align: 'right' });
    y += 18;

    doc.setDrawColor(...corDourado);
    doc.setLineWidth(1.4);
    doc.line(MARGEM, y, LARGURA - MARGEM, y);
    y += 26;

    const alturaCaixaValor = 64;
    doc.setFillColor(...corFundoValor);
    doc.rect(MARGEM, y, LARGURA - 2 * MARGEM, alturaCaixaValor, 'F');
    const centroX = LARGURA / 2;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(...corMuted);
    doc.text('VALOR PAGO', centroX, y + 22, { align: 'center' });
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(22);
    doc.setTextColor(...corTitulo);
    doc.text(Core.Currency.formatar(recibo.valorCentavos), centroX, y + 48, { align: 'center' });
    y += alturaCaixaValor + 28;

    function secao(nome) {
      doc.setFont('helvetica', 'bold');
      doc.setFontSize(11.5);
      doc.setTextColor(...corTitulo);
      doc.text(nome, MARGEM, y);
      y += 7;
      doc.setDrawColor(...corLinha);
      doc.setLineWidth(0.8);
      doc.line(MARGEM, y, LARGURA - MARGEM, y);
      y += 16;
    }

    function linha(campo, texto) {
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(9);
      doc.setTextColor(...corMuted);
      doc.text(campo, MARGEM, y);
      y += 13;
      doc.setFontSize(11);
      doc.setTextColor(...corTexto);
      doc.text(texto && String(texto).trim() ? String(texto) : '—', MARGEM, y);
      y += 18;
    }

    secao('Pagador');
    linha('Nome / Razão social', recibo.pagadorNome);
    linha('CPF / CNPJ', recibo.pagadorDocumento);
    y += 5;

    secao('Recebedor');
    linha('Nome completo', recibo.recebedorNome);
    linha('CPF', recibo.recebedorDocumento);
    y += 5;

    secao('Referente a');
    linha('Data do pagamento', Core.Dates.formatarData(recibo.dataPagamento));
    if (jogo) {
      const identificacao = [jogo.modalidade, jogo.competicao].filter((v) => v && String(v).trim()).join(' — ');
      if (identificacao) linha('Modalidade / Competição', identificacao);
      if (jogo.cidade && jogo.cidade.trim()) linha('Cidade', jogo.cidade);
      linha('Jogo', Core.confronto(jogo) || 'Escala Arbitragem');
      linha('Data do jogo', Core.Dates.formatarData(jogo.data));
    }
    if (recibo.descricao && recibo.descricao.trim()) linha('Descrição', recibo.descricao);

    y += 30;
    const larguraAssinatura = 200;
    const centroEsquerda = MARGEM + larguraAssinatura / 2;
    const centroDireita = LARGURA - MARGEM - larguraAssinatura / 2;
    doc.setDrawColor(...corTexto);
    doc.setLineWidth(0.8);
    doc.line(MARGEM, y, MARGEM + larguraAssinatura, y);
    doc.line(LARGURA - MARGEM - larguraAssinatura, y, LARGURA - MARGEM, y);
    y += 14;
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(...corMuted);
    doc.text('Assinatura do pagador', centroEsquerda, y, { align: 'center' });
    doc.text('Assinatura do recebedor', centroDireita, y, { align: 'center' });

    return doc.output('blob');
  },
};

function reciboNumeroFormatado(recibo) {
  return `Nº ${String(recibo.id).padStart(6, '0')}`;
}

if (typeof window !== 'undefined') window.Pdf = Pdf;
