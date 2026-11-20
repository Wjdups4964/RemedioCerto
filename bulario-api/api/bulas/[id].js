'use strict';

const { obter } = require('../../lib/catalogo');
const { enviarJson, erro, lerQuery, soGet } = require('../../lib/resposta');

// GET /api/bulas/losartana
module.exports = function detalhes(req, res) {
  if (!soGet(req, res)) return;
  const consulta = lerQuery(req);
  // Na Vercel o id vem em req.query.id; no servidor local, no final do caminho da URL.
  const id = consulta.id || decodeURIComponent(new URL(req.url || '/', 'http://localhost').pathname.split('/').pop());
  const medicamento = obter(id);
  if (!medicamento) return erro(res, 404, 'Medicamento não encontrado no catálogo.');
  return enviarJson(res, 200, medicamento, 'public, max-age=3600');
};
