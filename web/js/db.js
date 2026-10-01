// Camada de dados — IndexedDB local (equivalente ao Room/SQLite do app
// Android). Os dados ficam só neste navegador/aparelho; nunca saem daqui
// sem o usuário exportar um backup manualmente.
'use strict';

const DB_NOME = 'escalas-arbitragem-db';
const DB_VERSAO = 1;

const Db = {
  _db: null,

  async abrir() {
    if (this._db) return this._db;
    this._db = await new Promise((resolve, reject) => {
      const req = indexedDB.open(DB_NOME, DB_VERSAO);
      req.onupgradeneeded = (ev) => {
        const db = ev.target.result;
        if (!db.objectStoreNames.contains('jogos')) {
          const jogos = db.createObjectStore('jogos', { keyPath: 'id', autoIncrement: true });
          jogos.createIndex('data', 'data');
          jogos.createIndex('statusPagamento', 'statusPagamento');
        }
        if (!db.objectStoreNames.contains('recibos')) {
          const recibos = db.createObjectStore('recibos', { keyPath: 'id', autoIncrement: true });
          recibos.createIndex('jogoId', 'jogoId', { unique: true });
        }
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
    return this._db;
  },

  async _tx(storeNames, modo, fn) {
    const db = await this.abrir();
    return new Promise((resolve, reject) => {
      const tx = db.transaction(storeNames, modo);
      const stores = Array.isArray(storeNames) ? storeNames.map((n) => tx.objectStore(n)) : tx.objectStore(storeNames);
      let resultado;
      Promise.resolve(fn(stores, tx)).then((r) => { resultado = r; }).catch(reject);
      tx.oncomplete = () => resolve(resultado);
      tx.onerror = () => reject(tx.error);
      tx.onabort = () => reject(tx.error);
    });
  },

  _reqProm(req) {
    return new Promise((resolve, reject) => {
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
  },

  // ---------------- Jogos ----------------

  async listarJogos() {
    return this._tx('jogos', 'readonly', (store) => this._reqProm(store.getAll()));
  },

  /** Cria (id=0/null) ou atualiza (id existente) um jogo. Retorna o jogo salvo (com id). */
  async salvarJogo(jogo) {
    return this._tx('jogos', 'readwrite', async (store) => {
      const agora = new Date().toISOString();
      if (jogo.id) {
        const atual = { ...jogo, dataAtualizacao: agora };
        await this._reqProm(store.put(atual));
        return atual;
      } else {
        const { id, ...semId } = jogo;
        const novo = { ...semId, dataCriacao: jogo.dataCriacao || agora, dataAtualizacao: agora };
        const novoId = await this._reqProm(store.add(novo));
        return { ...novo, id: novoId };
      }
    });
  },

  /** Cria vários jogos de uma vez (cadastro em lote de N partidas). */
  async salvarVariosJogos(jogos) {
    return this._tx('jogos', 'readwrite', async (store) => {
      const agora = new Date().toISOString();
      const salvos = [];
      for (const jogo of jogos) {
        const { id, ...semId } = jogo;
        const novo = { ...semId, dataCriacao: agora, dataAtualizacao: agora };
        const novoId = await this._reqProm(store.add(novo));
        salvos.push({ ...novo, id: novoId });
      }
      return salvos;
    });
  },

  async excluirJogo(id) {
    return this._tx('jogos', 'readwrite', (store) => this._reqProm(store.delete(id)));
  },

  async obterJogo(id) {
    return this._tx('jogos', 'readonly', (store) => this._reqProm(store.get(id)));
  },

  /** Marca vários jogos (já carregados) como recebidos numa única transação — nunca cria/duplica. */
  async marcarVariosComoRecebido(jogosParaMarcar, dataRecebimentoISO) {
    return this._tx('jogos', 'readwrite', async (store) => {
      const agora = new Date().toISOString();
      for (const jogo of jogosParaMarcar) {
        const atualizado = { ...jogo, statusPagamento: Core.StatusPagamento.RECEBIDO, dataRecebimento: dataRecebimentoISO, dataAtualizacao: agora };
        await this._reqProm(store.put(atualizado));
      }
    });
  },

  /** Restaura um backup deste app: APAGA tudo e substitui pelos jogos informados. */
  async restaurarBackup(jogos) {
    return this._tx('jogos', 'readwrite', async (store) => {
      await this._reqProm(store.clear());
      for (const jogo of jogos) {
        const { id, ...resto } = jogo;
        await this._reqProm(store.add(id ? { ...resto, id } : resto));
      }
    });
  },

  /** Importa jogos de outro sistema ou mescla um backup — SOMA, nunca apaga nada existente. */
  async importarJogos(jogos) {
    return this._tx('jogos', 'readwrite', async (store) => {
      for (const jogo of jogos) {
        const { id, ...resto } = jogo;
        await this._reqProm(store.add(resto));
      }
    });
  },

  // ---------------- Recibos ----------------

  async reciboPorJogoId(jogoId) {
    return this._tx('recibos', 'readonly', async (store) => {
      const idx = store.index('jogoId');
      return this._reqProm(idx.get(jogoId));
    });
  },

  /** Upsert por jogoId — regenerar o recibo de um jogo reaproveita o mesmo registro, nunca duplica. */
  async salvarRecibo(recibo) {
    return this._tx('recibos', 'readwrite', async (store) => {
      const idx = store.index('jogoId');
      const existente = await this._reqProm(idx.get(recibo.jogoId));
      const agora = new Date().toISOString();
      if (existente) {
        const atualizado = { ...recibo, id: existente.id, criadoEm: existente.criadoEm };
        await this._reqProm(store.put(atualizado));
        return atualizado;
      } else {
        const { id, ...semId } = recibo;
        const novo = { ...semId, criadoEm: agora };
        const novoId = await this._reqProm(store.add(novo));
        return { ...novo, id: novoId };
      }
    });
  },
};

if (typeof window !== 'undefined') window.Db = Db;
