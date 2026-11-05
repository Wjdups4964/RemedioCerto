# RemedioCerto

Aplicativo Android para lembrete e controle de medicação de idosos, com acompanhamento por familiar. Projeto da disciplina de Programação para Dispositivos Móveis (IFPE Campus Belo Jardim). Os requisitos citados abaixo (RF, RN, RNF) estão no documento de requisitos do projeto.

## 1. Arquitetura de telas

O app tem 3 Activities e 9 Fragments, com dois perfis de uso: **idoso** e **familiar**.

### Activities

| Activity | Responsabilidade | Requisitos |
|---|---|---|
| **EntradaActivity** (LAUNCHER) | Aviso médico no primeiro uso, orientação de permissões (notificações, alarmes exatos, bateria), login, cadastro e a opção "Continuar sem conta". Quando o onboarding já foi concluído, vai direto para a MainActivity. | RN02, RF24, RF25, RF37, RNF21 |
| **MainActivity** | Hospeda o `NavHostFragment` e a navegação inferior. Escolhe o menu conforme o perfil (idoso ou familiar). | Seção 8 dos requisitos |
| **AlarmeActivity** | Abre sobre a tela bloqueada. Mostra nome e dose com os botões grandes "Tomei" e "Adiar" e lê o lembrete em voz alta se a opção estiver ativa. Confirmar a dose exige no máximo 3 toques. | RF08–RF13, RF35, RNF03, RNF04, RNF07 |

### Fragments

| Fragment | Responsabilidade | Requisitos |
|---|---|---|
| **HojeFragment** | Lista do dia, com doses tomadas e próximas. É a tela inicial do idoso. | RF15, RF14 |
| **MedicamentosFragment** | Lista de medicamentos, com botão para adicionar. | RF05 |
| **CadastroMedicamentoFragment** | Cadastro e edição de medicamento e seus horários (dias da semana, período, data final ou contínuo). Valida horários duplicados. | RF01–RF03, RN05, RN07 |
| **DetalhesMedicamentoFragment** | Dados cadastrados, consulta e bula (PDF) pela bulário-api, aviso "não substitui orientação médica", opção de desativar ou excluir. | RF04, RF19–RF23, RN02, RN03 |
| **HistoricoFragment** | Doses por período e por medicamento, resumo de adesão e exportação em PDF. | RF16, RF17, RF18 |
| **ConfiguracoesFragment** | Modo de acessibilidade, intervalo de adiamento, som, conta (sair, excluir dados da nuvem) e consentimento de sincronização. | RF34, RF36, RF25, RN09, RN11, RNF18 |
| **VinculoFragment** | Idoso: gera código de convite e vê ou revoga vínculos. Familiar: digita o código. | RF28–RF30, RN10, RN11 |
| **AcompanhamentoFragment** | Visão somente leitura do familiar: lista do dia e histórico do idoso, em tempo real. | RF31, RF33, RN08 |
| **FarmaciasFragment** (opcional) | Farmácias próximas e rota no app de mapas. | RF38, RF39 |

### Fluxo de navegação

```
EntradaActivity ──(primeiro uso)──► Aviso médico ► Permissões ► Login / Cadastro / "Continuar sem conta"
       │                                                            │
       └──(onboarding já feito)─────────────────────────────────────┘
                                    ▼
                              MainActivity
   ┌────────────────────────────────┴─────────────────────────────────┐
   │ Perfil IDOSO                        Perfil FAMILIAR              │
   │ [Hoje][Medicamentos][Histórico]     [Acompanhamento]             │
   │ [Configurações]                     [Configurações]              │
   └──────────────────────────────────────────────────────────────────┘

Medicamentos ─► (+) CadastroMedicamento (novo)
             └─► item ─► DetalhesMedicamento ─► Editar ─► CadastroMedicamento (edição)
                                             └─► Farmácias próximas (opcional)
Configurações ─► Vínculo (gerar/usar código, ver/revogar)
Acompanhamento (sem vínculo) ─► botão "Inserir código" ─► Vínculo

AlarmManager ─► AlarmeReceiver ─► notificação + AlarmeActivity (tela bloqueada)
AlarmeActivity ─► "Tomei" (registra e fecha) | "Adiar" (reagenda e fecha)
               └─ sem resposta ─► repete até o limite ─► dose "não confirmada"
BootReceiver ─► reagenda todos os alarmes
```

### Decisões de projeto

- O idoso tem 4 abas e o Vínculo fica dentro de Configurações. O documento de requisitos só define as abas do familiar.
- O familiar digita o código de convite (RF29) na `VinculoFragment`, acessada por Configurações ou pelo botão da tela de Acompanhamento vazia. A mesma Fragment serve aos dois perfis, mudando só o conteúdo.
- O perfil (idoso ou familiar) é escolhido na `EntradaActivity`, no cadastro da conta. Quem usa sem conta é sempre idoso (RF24).
- A `HojeFragment` permite confirmar uma dose pendente ou atrasada (RN04: dose só é "tomada" quando o usuário confirma).
- Navegação com o Navigation Component: um único `nav_graph` e dois menus inferiores. Os IDs dos itens do menu devem ser iguais aos IDs dos destinos.
- A `EntradaActivity` é uma Activity só, com os passos trocando dentro dela.
- **A definir:** tempo de expiração do código de convite (RN10).

## 2. Estrutura de pastas e arquivos

O pacote do projeto é `com.example.remediocerto`.

```
app/
├── google-services.json                  (NÃO versionar — RNF17)
├── build.gradle.kts                      (Room, Navigation, Retrofit, Firebase BoM, WorkManager)
└── src/main/
    ├── AndroidManifest.xml               (permissões, Activities, receivers)
    ├── java/com/example/remediocerto/
    │   ├── RemedioCertoApp.kt            (Application: canais de notificação, Firebase)
    │   │
    │   ├── ui/
    │   │   ├── entrada/        EntradaActivity.kt, EntradaViewModel.kt
    │   │   ├── main/           MainActivity.kt, MainViewModel.kt
    │   │   ├── alarme/         AlarmeActivity.kt, AlarmeViewModel.kt
    │   │   ├── hoje/           HojeFragment.kt, HojeViewModel.kt, DoseDiaAdapter.kt
    │   │   ├── medicamentos/   MedicamentosFragment.kt, MedicamentosViewModel.kt, MedicamentoAdapter.kt,
    │   │   │                   CadastroMedicamentoFragment.kt, CadastroMedicamentoViewModel.kt, HorarioAdapter.kt,
    │   │   │                   DetalhesMedicamentoFragment.kt, DetalhesMedicamentoViewModel.kt
    │   │   ├── historico/      HistoricoFragment.kt, HistoricoViewModel.kt, HistoricoAdapter.kt
    │   │   ├── configuracoes/  ConfiguracoesFragment.kt, ConfiguracoesViewModel.kt
    │   │   ├── vinculo/        VinculoFragment.kt, VinculoViewModel.kt, VinculoAdapter.kt
    │   │   ├── acompanhamento/ AcompanhamentoFragment.kt, AcompanhamentoViewModel.kt
    │   │   └── farmacias/      FarmaciasFragment.kt, FarmaciasViewModel.kt   (opcional)
    │   │
    │   ├── data/
    │   │   ├── local/          AppDatabase.kt
    │   │   │   ├── entity/     MedicamentoEntity.kt, HorarioEntity.kt, RegistroDoseEntity.kt,
    │   │   │   │               ConfiguracaoEntity.kt, CacheBulaEntity.kt
    │   │   │   └── dao/        MedicamentoDao.kt, HorarioDao.kt, RegistroDoseDao.kt,
    │   │   │                   ConfiguracaoDao.kt, CacheBulaDao.kt
    │   │   ├── remote/
    │   │   │   ├── firestore/  FirestoreDataSource.kt
    │   │   │   └── bulario/    BularioApi.kt, BularioDto.kt, RetrofitProvider.kt
    │   │   ├── model/          Usuario.kt, Vinculo.kt, Convite.kt, StatusDose.kt, Perfil.kt
    │   │   └── repository/     MedicamentoRepository.kt, DoseRepository.kt, ConfiguracaoRepository.kt,
    │   │                       BularioRepository.kt, AuthRepository.kt, VinculoRepository.kt,
    │   │                       FarmaciaRepository.kt (opcional)
    │   │
    │   ├── alarm/              AlarmeScheduler.kt, AlarmeReceiver.kt, BootReceiver.kt
    │   ├── notification/       NotificacaoHelper.kt
    │   ├── sync/               SincronizacaoWorker.kt
    │   └── util/               ValidadorHorario.kt (RN05/RN06/RN07), PermissoesHelper.kt (RF37),
    │                           VozHelper.kt (TTS, RF35), PdfExporter.kt (RF18), AcessibilidadeHelper.kt
    │
    └── res/
        ├── layout/             activity_entrada.xml, activity_main.xml, activity_alarme.xml,
        │                       fragment_hoje.xml, fragment_medicamentos.xml,
        │                       fragment_cadastro_medicamento.xml, fragment_detalhes_medicamento.xml,
        │                       fragment_historico.xml, fragment_configuracoes.xml,
        │                       fragment_vinculo.xml, fragment_acompanhamento.xml, fragment_farmacias.xml,
        │                       item_dose_dia.xml, item_medicamento.xml, item_horario.xml,
        │                       item_historico.xml, item_vinculo.xml
        ├── navigation/         nav_graph.xml
        ├── menu/               menu_bottom_idoso.xml, menu_bottom_familiar.xml
        ├── values/             strings.xml, colors.xml, dimens.xml, themes.xml
        ├── drawable/           ícones (sempre acompanhados de texto — RNF05)
        └── xml/                file_paths.xml (FileProvider para o PDF do histórico)

raiz do projeto/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── gradle/libs.versions.toml             (catálogo de versões das dependências)
├── .gitignore                            (inclui google-services.json — RNF17)
└── README.md                             (este arquivo)
```

### Observações

- O `AndroidManifest.xml` precisa de `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE` e `INTERNET`. A `AlarmeActivity` usa `showWhenLocked` e `turnScreenOn`.
- O `themes.xml` terá o tema padrão e um tema de alto contraste com fonte maior para o modo de acessibilidade (RF34): 18 sp no padrão e 24 sp no modo acessível (RNF01, valores a validar com testes).

## 3. Como executar

1. Abra a pasta do projeto no Android Studio e aguarde a sincronização do Gradle. O projeto usa AGP 9.3, Gradle 9.5 e `minSdk` 26 (Android 8.0).
2. Sem configurar o Firebase o app compila, mas o login e a sincronização não funcionam. Para configurá-lo:
   - No [console do Firebase](https://console.firebase.google.com), crie um projeto e adicione um app Android com o nome de pacote `com.example.remediocerto`.
   - Baixe o `google-services.json` e coloque-o na pasta `app/`. O arquivo está no `.gitignore` e **não deve ser versionado** (RNF17).
   - Ative o login por e-mail e senha (Authentication) e crie o banco Firestore.
   - Publique as regras de segurança do Firestore (RNF14): só o dono grava seus dados e só familiares vinculados leem.
3. Para testar o alarme, use um aparelho real e conceda as permissões de notificação e de alarmes exatos quando o app pedir (RF37).
