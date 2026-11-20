'use strict';

const { medicamentos } = require('../lib/catalogo');
const { enviarJson, soGet } = require('../lib/resposta');

// GET /api/saude: serve para conferir se o servidor está no ar.
module.exports = function saude(req, res) {
  if (!soGet(req, res)) return;
  enviarJson(res, 200, { status: 'ok', medicamentos: medicamentos.length });
};
