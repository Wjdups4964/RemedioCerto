'use strict';

// Importante: o caminho do require precisa ser fixo (texto literal) para a Vercel incluir o arquivo no pacote.
const medicamentos = require('../data/medicamentos.json');

const ATUALIZADO_EM = '2026-11-20';
const AVISO =
  'Conteúdo de apoio. Não substitui a orientação médica nem a bula oficial. Siga sempre a receita do seu médico.';
const URL_BULARIO_ANVISA = 'https://consultas.anvisa.gov.br/#/bulario/';
const TAMANHO_PAGINA = 10;

/** Minúsculas e sem acentos, para "acido" achar "Ácido". */
function normalizar(texto) {
  return String(texto || '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .trim();
}

function urlBulaOficial(nome) {
  return `${URL_BULARIO_ANVISA}q/?nomeProduto=${encodeURIComponent(nome)}`;
}

function resumo(m) {
  return { id: m.id, nome: m.nome, principioAtivo: m.principioAtivo, classe: m.classe };
}

function detalhe(m) {
  return {
    id: m.id,
    nome: m.nome,
    principioAtivo: m.principioAtivo,
    classe: m.classe,
    indicacao: m.indicacao,
    observacao: m.observacao || null,
    urlBulaOficial: urlBulaOficial(m.nome.replace(/\s*\(.*\)\s*$/, '')),
    aviso: AVISO,
    atualizadoEm: ATUALIZADO_EM,
  };
}

/** Procura pelo nome, pelo princípio ativo ou por outros nomes conhecidos. Devolve uma página de resumos. */
function buscar(nome, pagina) {
  const termo = normalizar(nome);
  const numero = Math.max(1, Number.parseInt(pagina, 10) || 1);
  const achados = medicamentos.filter((m) =>
    [m.nome, m.principioAtivo, ...(m.outrosNomes || [])].some((campo) => normalizar(campo).includes(termo))
  );
  const inicio = (numero - 1) * TAMANHO_PAGINA;
  return {
    total: achados.length,
    pagina: numero,
    tamanhoPagina: TAMANHO_PAGINA,
    itens: achados.slice(inicio, inicio + TAMANHO_PAGINA).map(resumo),
  };
}

function obter(id) {
  const alvo = normalizar(id);
  const m = medicamentos.find((x) => x.id === alvo);
  return m ? detalhe(m) : null;
}

module.exports = { buscar, obter, normalizar, medicamentos, AVISO, TAMANHO_PAGINA };
