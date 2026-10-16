// Service worker — cacheia o app shell para funcionar 100% offline depois
// do primeiro acesso (equivalente ao app Android não depender de internet).
'use strict';

const CACHE_NOME = 'escalas-arbitragem-v1';
const ARQUIVOS_PRECACHE = [
  './',
  'index.html',
  'manifest.json',
  'css/styles.css',
  'js/vendor/jspdf.umd.min.js',
  'js/core.js',
  'js/db.js',
  'js/backup.js',
  'js/pdf.js',
  'js/ui.js',
  'js/views-dashboard.js',
  'js/views-jogos.js',
  'js/views-recebimento.js',
  'js/views-resumo.js',
  'js/views-configuracoes.js',
  'js/views-jogoform.js',
  'js/views-jogodetail.js',
  'js/views-recibo.js',
  'icons/icon-192.png',
  'icons/icon-512.png',
  'icons/apple-touch-icon.png',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NOME).then((cache) => cache.addAll(ARQUIVOS_PRECACHE)).then(() => self.skipWaiting()),
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((chaves) => Promise.all(chaves.filter((c) => c !== CACHE_NOME).map((c) => caches.delete(c))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  const url = new URL(event.request.url);
  if (url.origin !== location.origin) return; // deixa passar CDNs (jsPDF) direto para a rede

  event.respondWith(
    caches.match(event.request).then((cacheado) => {
      const redeFetch = fetch(event.request).then((resposta) => {
        if (resposta && resposta.ok) {
          const copia = resposta.clone();
          caches.open(CACHE_NOME).then((cache) => cache.put(event.request, copia));
        }
        return resposta;
      }).catch(() => cacheado);
      return cacheado || redeFetch;
    }),
  );
});
