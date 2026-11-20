'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('node:http');
const { rotear } = require('../server');
const { medicamentos } = require('../lib/catalogo');

function subir() {
  return new Promise((resolve) => {
    const servidor = http.createServer(rotear).listen(0, '127.0.0.1', () => resolve(servidor));
  });
}

async function pedir(servidor, caminho, metodo = 'GET') {
  const { port } = servidor.address();
  const resposta = await fetch(`http://127.0.0.1:${port}${caminho}`, { method: metodo });
  return { status: resposta.status, corpo: await resposta.json(), tipo: resposta.headers.get('content-type') };
}

test('catálogo: ids únicos, campos obrigatórios e sem posologia', () => {
  const ids = new Set();
  for (const m of medicamentos) {
    assert.ok(m.id && m.nome && m.principioAtivo && m.classe && m.indicacao, `campos faltando em ${m.id}`);
    assert.match(m.id, /^[a-z0-9-]+$/, `id inválido: ${m.id}`);
    assert.ok(!ids.has(m.id), `id repetido: ${m.id}`);
    ids.add(m.id);
    assert.ok(!('posologia' in m) && !('dose' in m), `não guardamos posologia (${m.id})`);
  }
});

test('API de bulas', async (t) => {
  const servidor = await subir();
  t.after(() => servidor.close());

  await t.test('saúde', async () => {
    const r = await pedir(servidor, '/api/saude');
    assert.equal(r.status, 200);
    assert.equal(r.corpo.status, 'ok');
    assert.equal(r.corpo.medicamentos, medicamentos.length);
    assert.match(r.tipo, /application\/json/);
  });

  await t.test('pesquisa pelo nome', async () => {
    const r = await pedir(servidor, '/api/bulas?nome=losartana');
    assert.equal(r.status, 200);
    assert.equal(r.corpo.total, 1);
    assert.equal(r.corpo.itens[0].id, 'losartana');
  });

  await t.test('pesquisa ignora acento e maiúsculas', async () => {
    const r = await pedir(servidor, '/api/bulas?nome=ACIDO');
    assert.equal(r.status, 200);
    assert.ok(r.corpo.itens.some((i) => i.id === 'acido-acetilsalicilico'));
  });

  await t.test('pesquisa por outro nome (AAS) e por princípio ativo', async () => {
    const aas = await pedir(servidor, '/api/bulas?nome=aas');
    assert.ok(aas.corpo.itens.some((i) => i.id === 'acido-acetilsalicilico'));
    const metamizol = await pedir(servidor, '/api/bulas?nome=metamizol');
    assert.ok(metamizol.corpo.itens.some((i) => i.id === 'dipirona'));
  });

  await t.test('pesquisa sem resultado devolve lista vazia, não erro', async () => {
    const r = await pedir(servidor, '/api/bulas?nome=xyzxyz');
    assert.equal(r.status, 200);
    assert.deepEqual(r.corpo.itens, []);
    assert.equal(r.corpo.total, 0);
  });

  await t.test('nome curto ou ausente dá 400', async () => {
    assert.equal((await pedir(servidor, '/api/bulas')).status, 400);
    assert.equal((await pedir(servidor, '/api/bulas?nome=a')).status, 400);
  });

  await t.test('paginação', async () => {
    const p1 = await pedir(servidor, '/api/bulas?nome=ina&pagina=1');
    const p2 = await pedir(servidor, '/api/bulas?nome=ina&pagina=2');
    assert.ok(p1.corpo.itens.length <= 10);
    if (p1.corpo.total > 10) assert.ok(p2.corpo.itens.length > 0);
    const ids1 = p1.corpo.itens.map((i) => i.id);
    assert.ok(p2.corpo.itens.every((i) => !ids1.includes(i.id)));
  });

  await t.test('detalhes de um medicamento', async () => {
    const r = await pedir(servidor, '/api/bulas/dipirona');
    assert.equal(r.status, 200);
    assert.equal(r.corpo.id, 'dipirona');
    assert.ok(r.corpo.indicacao);
    assert.match(r.corpo.urlBulaOficial, /^https:\/\/consultas\.anvisa\.gov\.br\/#\/bulario\/q\/\?nomeProduto=Dipirona$/);
    assert.match(r.corpo.aviso, /não substitui/i);
    assert.ok(r.corpo.atualizadoEm);
  });

  await t.test('nome com parênteses sai limpo no link da bula', async () => {
    const r = await pedir(servidor, '/api/bulas/acido-acetilsalicilico');
    assert.ok(r.corpo.urlBulaOficial.endsWith('nomeProduto=%C3%81cido%20acetilsalic%C3%ADlico'));
  });

  await t.test('id inexistente dá 404 com mensagem', async () => {
    const r = await pedir(servidor, '/api/bulas/nao-existe');
    assert.equal(r.status, 404);
    assert.ok(r.corpo.erro);
  });

  await t.test('rota desconhecida dá 404 e POST dá 405', async () => {
    assert.equal((await pedir(servidor, '/api/outra')).status, 404);
    assert.equal((await pedir(servidor, '/api/bulas?nome=losartana', 'POST')).status, 405);
  });
});
