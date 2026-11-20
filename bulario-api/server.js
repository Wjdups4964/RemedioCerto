'use strict';

// Servidor local para testar sem a Vercel: node server.js (porta 3000, ou PORT).
const http = require('node:http');
const { erro } = require('./lib/resposta');
const pesquisar = require('./api/bulas/index');
const detalhes = require('./api/bulas/[id]');
const saude = require('./api/saude');

function rotear(req, res) {
  const caminho = new URL(req.url || '/', 'http://localhost').pathname.replace(/\/+$/, '');
  if (caminho === '/api/saude') return saude(req, res);
  if (caminho === '/api/bulas') return pesquisar(req, res);
  if (/^\/api\/bulas\/[^/]+$/.test(caminho)) return detalhes(req, res);
  return erro(res, 404, 'Rota não encontrada.');
}

if (require.main === module) {
  const porta = Number(process.env.PORT) || 3000;
  http.createServer(rotear).listen(porta, '0.0.0.0', () => {
    console.log(`bulario-api no ar em http://localhost:${porta}/api/saude`);
  });
}

module.exports = { rotear };
