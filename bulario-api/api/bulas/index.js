'use strict';

const { buscar, normalizar } = require('../../lib/catalogo');
const { enviarJson, erro, lerQuery, soGet } = require('../../lib/resposta');

// GET /api/bulas?nome=losartana&pagina=1
module.exports = function pesquisar(req, res) {
  if (!soGet(req, res)) return;
  const { nome, pagina } = lerQuery(req);
  if (normalizar(nome).length < 2) {
    return erro(res, 400, 'Informe o parâmetro "nome" com pelo menos 2 letras.');
  }
  return enviarJson(res, 200, buscar(nome, pagina), 'public, max-age=3600');
};
