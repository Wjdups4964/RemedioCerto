# RemédioCerto

Aplicativo Android de lembrete e controle de medicação para idosos, com acompanhamento por um familiar ou cuidador. Projeto da disciplina de Programação para Dispositivos Móveis (IFPE, Campus Belo Jardim).

O idoso cadastra seus remédios e horários, recebe avisos antes da hora de tomar e confirma cada dose. O familiar, depois de vinculado por um código, acompanha em tempo real, só para leitura, as doses do dia.

**Equipe:** Victor e Pedro. **Última atualização deste documento:** 20/11/2026.

## Sumário

1. [Situação do projeto](#1-situação-do-projeto)
2. [Funcionalidades](#2-funcionalidades)
3. [Arquitetura](#3-arquitetura)
4. [Telas e navegação](#4-telas-e-navegação)
5. [Estrutura de pastas](#5-estrutura-de-pastas)
6. [Dados e segurança](#6-dados-e-segurança)
7. [Integração com o web service (bulário)](#7-integração-com-o-web-service-bulário)
8. [Avisos de dose (notificações)](#8-avisos-de-dose-notificações)
9. [Como executar](#9-como-executar)
10. [Testes](#10-testes)
11. [Como contribuir (Git)](#11-como-contribuir-git)

## 1. Situação do projeto

| Parte | Situação |
|---|---|
| Entrada: aviso médico, cadastro e login com e-mail e senha, confirmação de e-mail, recuperação de senha | Pronto |
| Medicamentos: cadastro, edição, exclusão, horários, dias e duração do tratamento | Pronto |
| Tela Hoje, confirmação de dose (Tomei/Adiar) e Próximos dias | Pronto |
| Histórico com resumo dos últimos 7 dias | Pronto |
| Bula pelo web service (bulario-api na Vercel, com cache offline) | Pronto |
| Notificações 10 min antes, 5 min antes e na hora da dose | Pronto |
| Vínculo idoso e familiar, com acompanhamento em tempo real | Pronto |
| Tema automático, claro e escuro | Pronto |
| Modo de acessibilidade (fonte grande e alto contraste) | Parcial: a chave salva a escolha, mas ainda não muda a tela |
| Alarme em tela cheia com Tomei/Adiar de verdade | Pendente (a tela atual é só de teste) |
| Farmácias próximas | Pendente (tela reservada, sem funcionalidade) |
| Exportar histórico em PDF e leitura em voz alta | Pendente |

Entregas: **24/11** integração com o web service, **30/11** pré-apresentação (alarme real, notificações e acessibilidade), **07/12** apresentações.

## 2. Funcionalidades

**Idoso** (abas Hoje, Remédios, Histórico e Ajustes)

- **Hoje:** lista as doses do dia, atualizada em tempo real. Cada dose passa de "Próxima" para "Hora de tomar" e "Atrasada" sozinha. Os botões Tomei e Adiar só aparecem na hora da dose, e "Tomei" pede uma confirmação ("Sim, tomei") para um toque sem querer não registrar nada. O botão "Ver próximos dias" abre a lista até o fim do último tratamento.
- **Remédios:** lista, cadastro e edição. O nome tem sugestões vindas da bula. Cada remédio tem dose, forma, observações, horários (fixos ou "a cada N horas" a partir da primeira dose), "a cada N dias", duração (uso contínuo ou por N dias, com data de início e de fim) e o tempo de adiamento (5, 10 ou 15 minutos).
- **Detalhes:** dados do remédio, consulta da bula ("para que serve" e link da bula oficial da Anvisa), aviso de que o conteúdo não substitui a orientação médica, editar e apagar.
- **Histórico:** resumo dos últimos 7 dias e as doses registradas agrupadas por dia.
- **Ajustes:** meu perfil (nome, nascimento, sexo e contato de emergência), vínculo com familiar, situação dos avisos, modo de acessibilidade, aparência (automático, claro ou escuro), farmácias próximas e sair.

**Familiar** (abas Acompanhamento e Ajustes)

- **Acompanhamento:** doses de hoje do idoso vinculado, em tempo real e somente leitura. Quem acompanha mais de um idoso troca entre eles na própria tela.
- **Vínculo:** digita o código de 8 dígitos que o idoso gerou.

## 3. Arquitetura

- **MVVM** com `ViewModel` e `StateFlow` (coroutines). As telas só mostram o estado e repassam os toques; as regras ficam nas ViewModels e em funções puras, que têm testes de unidade.
- **Navigation Component**: um único `nav_graph.xml` e dois menus inferiores, um por perfil. O perfil (idoso ou familiar) é escolhido no cadastro da conta.
- **Repositórios atrás de interfaces** (`data/repository`). `Repositorios.kt` é o ponto único de acesso: trocar a fonte de dados (por exemplo, por um repositório de teste) é mudar só esse arquivo.
- **Firebase** (Authentication e Firestore) guarda os dados do usuário. O cache offline do Firestore faz o app funcionar sem internet: o que a pessoa registra fica no celular e sincroniza depois.
- **Retrofit e OkHttp** consultam o web service de bulas.
- **AlarmManager** agenda os avisos de dose, e o `BootReceiver` os agenda de novo depois de reiniciar o celular.
- **ViewBinding** em todas as telas. Os textos ficam em `strings*.xml` e os tamanhos de texto e de botão são grandes, para facilitar a leitura do idoso.

Versões principais: Android Gradle Plugin 9.3.3, `minSdk` 26 (Android 8.0), `compileSdk` e `targetSdk` 37, Material 1.14.0, Navigation 2.9.8, Retrofit 3.0.0, OkHttp 4.12.0 e Firebase BoM 34.17.0.

## 4. Telas e navegação

### Activities

| Activity | Responsabilidade |
|---|---|
| **EntradaActivity** (LAUNCHER) | Aviso médico no primeiro uso, cadastro e login com e-mail e senha (mínimo de 8 caracteres), confirmação do e-mail, recuperação de senha e escolha do perfil. Quem já está logado vai direto para a `MainActivity`. |
| **MainActivity** | Hospeda o `NavHostFragment` e a barra inferior, que muda conforme o perfil. Para o idoso, pede as permissões dos avisos e mantém os avisos atualizados. |
| **AlarmeActivity** | Tela de alarme com os botões grandes Tomei e Adiar. Hoje é só uma tela de teste (botão na tela Hoje); o alarme real em tela cheia é da entrega de 30/11. |

### Fragments

| Fragment | Responsabilidade |
|---|---|
| **HojeFragment** | Doses do dia, Tomei/Adiar e atalho para Próximos dias. |
| **ProximosDiasFragment** | Tratamentos com data para acabar e, dia a dia, os remédios de cada dia, até o fim do último tratamento (no mínimo 7 e no máximo 90 dias). |
| **MedicamentosFragment** | Lista de remédios, com botão para adicionar. |
| **CadastroMedicamentoFragment** | Cadastro e edição de remédio, com validação dos campos e sugestões de nome. |
| **DetalhesMedicamentoFragment** | Dados do remédio, bula, editar e apagar. |
| **HistoricoFragment** | Resumo da semana e doses por dia. |
| **ConfiguracoesFragment** | Seções Minha conta, Avisos dos remédios (só idoso), Leitura e aparência e Mais. Mostra se os avisos vão funcionar e leva às configurações do Android quando falta permissão. |
| **PerfilFragment** | Dados pessoais. Só o nome é obrigatório. |
| **VinculoFragment** | Idoso: gera o código e vê ou encerra vínculos. Familiar: digita o código. |
| **AcompanhamentoFragment** | Doses de hoje do idoso vinculado, somente leitura. |
| **FarmaciasFragment** | Tela reservada para farmácias próximas (opcional). |

### Fluxo

```
EntradaActivity ─► aviso médico ─► cadastro ou login ─► (e-mail confirmado) ─► MainActivity
                                                                                  │
        ┌─────────────────────────────────────────────────────────────────────────┘
        │  Perfil IDOSO: [Hoje] [Remédios] [Histórico] [Ajustes]
        │  Perfil FAMILIAR: [Acompanhamento] [Ajustes]
        ▼
Hoje ─► Próximos dias
Remédios ─► (+) Cadastro ─────────► salva e volta
         └─► item ─► Detalhes ─► Editar ─► Cadastro
Ajustes ─► Meu perfil | Vínculo | Farmácias | Sair
Acompanhamento (sem vínculo) ─► Vínculo

AlarmManager ─► AlarmeReceiver ─► notificação (10 min, 5 min e na hora)
BootReceiver ─► agenda os avisos de novo (reinício, atualização do app, mudança de fuso)
```

## 5. Estrutura de pastas

O pacote do projeto é `com.example.remediocerto`.

```
app/src/main/java/com/example/remediocerto/
├── RemedioCertoApp.kt        Application: tema salvo, canal de notificação e repositórios
├── alarm/                    Lembrete.kt (quais avisos existem), AlarmeScheduler.kt (agenda no AlarmManager),
│                             AgendaLocal.kt (cópia local dos remédios), AlarmeReceiver.kt, BootReceiver.kt
├── notification/             NotificacaoHelper.kt (canal e notificações)
├── data/
│   ├── Repositorios.kt       ponto único de acesso aos repositórios
│   ├── model/                Medicamento, RegistroDose, DoseDoDia, Convite, Vinculo, Bula, DadosPessoais...
│   ├── repository/           interfaces (MedicamentoRepository, DoseRepository, VinculoRepository...)
│   ├── remote/firestore/     implementações com Firestore
│   ├── remote/bulario/       Retrofit: BularioApi, BularioDto, RetrofitProvider, RetrofitBularioRepository
│   └── local/                BulaCache.kt (última bula de cada remédio)
├── ui/                       uma pasta por tela: entrada, main, hoje, medicamentos, historico,
│                             configuracoes, perfil, vinculo, acompanhamento, alarme, farmacias
└── util/                     Datas, Periodo, Tema, PermissoesHelper, StatusVisual, Coletar...

app/src/main/res/             layouts, menus, navigation/nav_graph.xml, values (textos em strings*.xml)
app/src/test/                 testes de unidade (JVM)

bulario-api/                  web service de bulas (Node.js, publicado na Vercel)
firestore.rules               regras de segurança do Firestore
```

## 6. Dados e segurança

### Firestore

```
usuarios/{uid}                       perfil de uso, dados pessoais e configurações
usuarios/{uid}/medicamentos/{id}     remédios do usuário
usuarios/{uid}/registros/{id}        doses registradas (tomada ou adiada)
convites/{codigo}                    código de convite (o código é o id do documento)
vinculos/{idosoUid}_{familiarUid}    vínculo entre um idoso e um familiar
```

### Regras (`firestore.rules`)

- Cada pessoa lê e grava só o que está na própria conta.
- O familiar vinculado **só lê** os medicamentos e os registros do idoso (não vê os dados pessoais) e perde o acesso na hora em que o vínculo é encerrado.
- O código de convite tem 8 dígitos, vale 24 horas (`CONVITE_VALIDADE_HORAS`) e só pode ser usado uma vez. As regras aceitam no máximo 2 dias de validade. Quem decide a validade e o uso único é o servidor, com o relógio dele, e não o celular.
- Ninguém lista convites: só dá para consultar um convite sabendo o código.

As regras precisam ser **publicadas** no Console do Firebase (Firestore Database > Regras: colar o arquivo e tocar em "Publicar").

### Cuidados

- O `google-services.json` e o `local.properties` **não são versionados** (estão no `.gitignore`).
- O app não calcula nem sugere doses. A dose é sempre a da receita.
- O aviso de que o app não substitui a orientação médica aparece no primeiro uso e na tela de Detalhes.

## 7. Integração com o web service (bulário)

O app consulta um web service próprio, o **bulario-api** (pasta `bulario-api/`, publicado na Vercel), para mostrar na tela de Detalhes **para que serve** cada medicamento e o link da **bula oficial da Anvisa**. O serviço também sugere nomes enquanto a pessoa digita no cadastro.

**Por que um servidor próprio:** o bulário da Anvisa passou a exigir autenticação e as APIs abertas de terceiros saíram do ar. Com o servidor do grupo, nenhuma chave fica dentro do app e o serviço continua no ar na apresentação. O detalhamento do servidor está em `bulario-api/README.md`.

### Como as partes se conectam

```
Cadastro (digitando "los")  ──► sugerir()  ─┐
Detalhes ("Consultar bula") ──► consultar() ─┤   BularioRepository (interface)
                                             ▼
                                  RetrofitBularioRepository
                                   │                    │
                          Retrofit + OkHttp          BulaCache
                          (HTTPS, JSON/Gson)     (SharedPreferences)
                                   │                    ▲
                                   ▼                    │ salva a cada sucesso;
                    bulario-api na Vercel               │ usa se a internet falhar
        GET /api/bulas?nome=...   GET /api/bulas/{id}
```

### Rotas do serviço (apenas GET)

| Rota | O que devolve |
|---|---|
| `GET /api/bulas?nome=losartana&pagina=1` | Lista de até 10 medicamentos do catálogo que combinam com o nome, com o princípio ativo ou com outros nomes conhecidos (sem acento e sem diferenciar maiúsculas). |
| `GET /api/bulas/{id}` | Detalhes de um medicamento: nome, princípio ativo, classe, para que serve, observação, link da bula oficial e aviso. |
| `GET /api/saude` | Mostra se o servidor está no ar. |

Erros: `400` (nome com menos de 2 letras), `404` (fora do catálogo), `405` (método diferente de GET). O catálogo atual tem 36 medicamentos (`bulario-api/data/medicamentos.json`). **Não há posologia** no serviço: a dose é sempre a da receita e da bula oficial.

### No aplicativo

| Arquivo | Papel |
|---|---|
| `data/remote/bulario/RetrofitProvider.kt` | Cria o Retrofit com a `BASE_URL` (`https://remedio-certo-eight.vercel.app/`) e tempos limite de 8 s de conexão e leitura. |
| `data/remote/bulario/BularioApi.kt` | Interface Retrofit com as duas rotas (`pesquisar` e `detalhes`), chamadas com `suspend`. |
| `data/remote/bulario/BularioDto.kt` | Formato das respostas. Todos os campos têm valor padrão, então um campo ausente não derruba o app. |
| `data/remote/bulario/RetrofitBularioRepository.kt` | Regras da consulta: monta os termos de busca, escolhe o melhor resultado, busca os detalhes, trata os erros e usa o cache. |
| `data/local/BulaCache.kt` | Guarda no celular a última bula de cada medicamento, para abrir sem internet. |
| `ui/medicamentos/DetalhesMedicamentoViewModel.kt` | Estados da bula na tela: inicial, consultando, pronta ou falha. Ignora toques repetidos durante a consulta. |
| `ui/medicamentos/CadastroMedicamentoViewModel.kt` | Sugestões de nome no cadastro, com espera de 300 ms depois da última tecla (evita uma chamada por letra). |

**Consulta da bula.** O nome cadastrado ("Losartana 50 mg") vira termos de busca: primeiro o nome sem números e unidades ("losartana"), depois só a primeira palavra. Para cada termo o app pesquisa, escolhe o resultado de nome igual (ou o primeiro), busca os detalhes e salva no cache. Todas as chamadas de rede rodam fora da thread principal (`Dispatchers.IO`).

### Quando algo dá errado

| Situação | O que o app faz |
|---|---|
| Sem internet ou servidor fora do ar, com consulta anterior salva | Mostra a última bula salva e informa a data: "Sem internet: mostrando a última consulta, feita em …". |
| Sem internet e sem consulta anterior | Explica que é preciso conectar e deixa tentar de novo. |
| Medicamento fora do catálogo (`404`) | Avisa que não foi encontrado e oferece o botão para pesquisar o nome direto no site da Anvisa. |
| Servidor com erro ou resposta inesperada | Usa o cache, se houver; senão pede para tentar de novo em instantes. |
| Sugestões de nome falham | Lista vazia. O campo continua aceitando qualquer texto digitado. |

### Segurança e limites

- Toda a comunicação usa HTTPS e não há chave de acesso dentro do app.
- A tela sempre mostra o aviso de que o conteúdo **não substitui a orientação médica** nem a bula oficial.
- O catálogo é de apoio e **deve ser revisado por um farmacêutico** antes de uso real. O app não calcula nem sugere doses.

## 8. Avisos de dose (notificações)

Para cada dose, o app avisa **10 minutos antes**, **5 minutos antes** e **na hora** (valores em `AVISOS_ANTES_DA_DOSE`, em `alarm/Lembrete.kt`).

1. Quando o idoso entra no app, a `MainActivity` observa os medicamentos. A cada mudança (remédio novo, horário editado, remédio apagado), o `AlarmeScheduler` recalcula os avisos dos **próximos 2 dias** e agenda cada um no `AlarmManager`. O cálculo respeita os mesmos dias do tratamento da tela Hoje ("a cada N dias", início e fim).
2. O app guarda uma cópia dos medicamentos no próprio celular (`AgendaLocal`, em SharedPreferences). Assim os avisos funcionam sem internet e com o app fechado.
3. Na hora marcada, o `AlarmeReceiver` mostra a notificação e agenda de novo os próximos 2 dias. Os três avisos da mesma dose usam a mesma notificação, então um troca o anterior. Tocar na notificação abre a tela Hoje.
4. O `BootReceiver` agenda tudo de novo depois de reiniciar o celular, de atualizar o app ou de mudar o fuso horário.
5. Ao sair da conta, todos os alarmes são cancelados e a cópia local é apagada.

**Permissões.** No Android 13 ou mais novo, o app pede permissão para mostrar notificações. No Android 12 ou mais novo, ele pergunta uma vez se a pessoa permite "Alarmes e lembretes" (sem isso, o Android pode atrasar os avisos em alguns minutos). Se faltar alguma permissão, a tela Ajustes mostra a situação e um botão que leva à tela certa do Android. A tela Hoje também mostra um aviso se as notificações estiverem desligadas.

**Limites atuais.** O botão Adiar registra a dose como adiada, mas não agenda um novo aviso. Alguns celulares (Xiaomi, Samsung e outros) cancelam alarmes de apps fechados quando a economia de bateria está ativa.

## 9. Como executar

1. Abra a pasta do projeto no Android Studio e aguarde a sincronização do Gradle (Gradle 9.5, AGP 9.3.3 e `minSdk` 26).
2. Configure o Firebase. Sem isso o app compila, mas o login e a sincronização não funcionam:
   - No [console do Firebase](https://console.firebase.google.com), crie um projeto e adicione um app Android com o pacote `com.example.remediocerto`.
   - Baixe o `google-services.json` e coloque na pasta `app/`. O arquivo **não deve ser versionado**.
   - Ative o login por e-mail e senha (Authentication) e crie o banco Firestore.
   - Publique as regras do arquivo `firestore.rules` (seção 6).
3. Rode o app em um aparelho real ou emulador. Para os avisos, permita as notificações e "Alarmes e lembretes" quando o app pedir.
4. A bula usa o servidor da Vercel, que já está no ar. Para rodar o servidor no computador (Node 18 ou mais novo):
   ```
   cd bulario-api
   npm test
   npm start        # http://localhost:3000
   ```
   Para o emulador usar o servidor local, troque a `BASE_URL` em `RetrofitProvider.kt` por `http://10.0.2.2:3000/` (só para esse teste, e libere tráfego sem HTTPS no manifesto).

## 10. Testes

- **App:** testes de unidade (JVM) das regras principais: avisos de dose (`LembreteTest`), próximos dias (`ProximosDiasTest`), agrupamento do histórico (`HistoricoItensTest`), datas do tratamento (`PeriodoTest`), convites (`ConviteTest`), sugestões e termos de busca da bula (`SugestoesTest`, `TermosDeBuscaTest`). No Android Studio: botão direito em `app/src/test` > Run Tests.
- **Servidor:** `cd bulario-api && npm test`.

**Roteiro de teste dos avisos:** cadastre um remédio com horário uns 12 minutos à frente do relógio do celular, feche o app e bloqueie a tela. Os avisos devem chegar 10 min antes, 5 min antes e na hora. Para conferir os alarmes agendados: `adb shell dumpsys alarm | findstr remediocerto`.

## 11. Como contribuir (Git)

- Trabalhe em branches curtas (`feature/...`) e abra um Pull Request para a `main`. Nunca use `git push --force`.
- Antes do `git add`, confira o `git status`: o `google-services.json` e o `local.properties` não podem aparecer.
- A cada merge na `main`, a Vercel publica a nova versão do `bulario-api` sozinha.
