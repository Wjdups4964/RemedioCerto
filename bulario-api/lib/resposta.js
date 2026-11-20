'use strict';

/** Responde em JSON usando só a API básica do Node, que funciona na Vercel e no servidor local. */
function enviarJson(res, status, corpo, cache) {
  res.statusCode = status;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.setHeader('Cache-Control', cache || 'no-store');
  res.end(JSON.stringify(corpo));
}

function erro(res, status, mensagem) {
  enviarJson(res, status, { erro: mensagem });
}

/** Lê a query string da requisição, venha ela já pronta (Vercel) ou só como texto na URL. */
function lerQuery(req) {
  if (req.query && typeof req.query === 'object') return req.query;
  const url = new URL(req.url || '/', 'http://localhost');
  return Object.fromEntries(url.searchParams.entries());
}

function soGet(req, res) {
  if (req.method === 'GET' || req.method === 'HEAD') return true;
  res.setHeader('Allow', 'GET, HEAD');
  erro(res, 405, 'Use o método GET.');
  return false;
}

module.exports = { enviarJson, erro, lerQuery, soGet };
