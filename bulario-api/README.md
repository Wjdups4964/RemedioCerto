# bulario-api

Web service de bulas do aplicativo **RemédioCerto** (IFPE Belo Jardim). O app consulta este serviço pela rede (Retrofit) para mostrar, na tela de Detalhes, para que serve cada medicamento e o link da bula oficial da Anvisa.

Por que um servidor próprio: o bulário da Anvisa passou a exigir autenticação, e as APIs abertas de terceiros saíram do ar. Assim, nenhuma chave fica dentro do app e o serviço continua funcionando na apresentação.

## Como funciona

Duas rotas de consulta (método GET) e uma de verificação:

| Rota | O que faz |
|---|---|
| `GET /api/bulas?nome=losartana&pagina=1` | Procura pelo nome, pelo princípio ativo ou por outros nomes conhecidos. Ignora acento e maiúsculas. Até 10 resultados por página. |
| `GET /api/bulas/{id}` | Detalhes de um medicamento (ex.: `/api/bulas/losartana`). |
| `GET /api/saude` | Mostra se o servidor está no ar. |

Exemplo de resposta de `/api/bulas/dipirona`:

```json
{
  "id": "dipirona",
  "nome": "Dipirona",
  "principioAtivo": "Dipirona sódica (metamizol)",
  "classe": "Analgésico e antitérmico",
  "indicacao": "Dor e febre.",
  "observacao": null,
  "urlBulaOficial": "https://consultas.anvisa.gov.br/#/bulario/q/?nomeProduto=Dipirona",
  "aviso": "Conteúdo de apoio. Não substitui a orientação médica nem a bula oficial. Siga sempre a receita do seu médico.",
  "atualizadoEm": "2026-11-20"
}
```

Erros: `400` (nome com menos de 2 letras), `404` (medicamento fora do catálogo), `405` (método diferente de GET). A resposta de erro é `{ "erro": "mensagem" }`.

O catálogo fica em `data/medicamentos.json`. De propósito, **não há posologia (quanto tomar)**: só nome, tipo, para que serve e, em poucos casos, uma observação. A dose é sempre a da receita e da bula oficial.

> Atenção: o conteúdo do catálogo precisa ser revisado pelo grupo (e, se possível, por um farmacêutico) antes da apresentação.

## Testar no computador

Precisa do Node 18 ou mais novo.

```
cd bulario-api
npm test          # roda os testes
npm start         # sobe em http://localhost:3000
```

Depois abra `http://localhost:3000/api/bulas?nome=dipirona` no navegador.

## Colocar no ar (Vercel, grátis)

1. Envie a pasta `bulario-api` para o GitHub (ela já faz parte do repositório do app).
2. Entre em <https://vercel.com> com a conta do GitHub e clique em **Add New > Project**.
3. Escolha o repositório `RemedioCerto` e, em **Root Directory**, selecione `bulario-api`. Deixe o resto como está (Framework: Other) e clique em **Deploy**.
4. Quando terminar, a Vercel mostra o endereço, algo como `https://remediocerto-bulario.vercel.app`. Abra `https://SEU-ENDERECO/api/saude` e confira se aparece `{"status":"ok", ...}`.
5. No app, abra `RetrofitProvider.kt` e troque o valor de `BASE_URL` pelo endereço (com `/` no final).

A cada `git push` na branch principal a Vercel publica a nova versão sozinha. Para incluir ou corrigir medicamentos, edite o `data/medicamentos.json`.

## Estrutura

```
bulario-api/
├── api/              as rotas (a Vercel transforma cada arquivo em uma rota)
│   ├── bulas/index.js     GET /api/bulas
│   ├── bulas/[id].js      GET /api/bulas/{id}
│   └── saude.js           GET /api/saude
├── lib/              busca no catálogo e respostas em JSON
├── data/             medicamentos.json (o catálogo)
├── test/             testes automáticos
└── server.js         servidor local, só para desenvolvimento
```
