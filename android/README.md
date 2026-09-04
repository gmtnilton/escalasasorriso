# Meus Jogos de Arbitragem

Aplicativo Android nativo para controle pessoal de jogos de futebol apitados: cadastro rápido, controle financeiro (a receber / recebido), histórico, filtros, resumo mensal/anual, estatísticas com gráficos simples, backup/restauração e exportação em CSV. **Funciona 100% offline** — todos os dados ficam salvos localmente no aparelho (Room/SQLite).

---

## 1. Estrutura do projeto

Projeto Gradle multi-módulo:

```
android/
├── app/    → aplicativo Android (Kotlin + Jetpack Compose + Room + Navigation, MVVM)
└── core/   → módulo Kotlin puro (sem dependência do Android) com o modelo de
              domínio e TODAS as regras de negócio financeiras (cálculo de
              totais, duplicação, filtros, resumos) — testado com JUnit
```

Separar a lógica financeira em `:core` permite testar as regras de cálculo (a
parte mais crítica do app) com testes unitários rápidos, sem precisar de
emulador Android.

### Principais tecnologias

- Kotlin 1.9.24
- Jetpack Compose + Material 3 (UI declarativa)
- Room (banco de dados local/SQLite)
- Navigation Compose (navegação entre telas)
- Arquitetura MVVM (ViewModel + StateFlow)
- Coroutines/Flow para reatividade
- minSdk 26 (Android 8.0+) · targetSdk 34 · compileSdk 34

Não há dependência de internet, notificações ou serviços externos — o app
não pede nenhuma permissão especial.

---

## 2. Como abrir no Android Studio

1. Instale o **Android Studio** (versão Koala/2024.1 ou mais recente é
   recomendada, mas qualquer versão compatível com AGP 8.5 funciona).
2. Abra o Android Studio → **File → Open...** → selecione a pasta `android/`
   deste repositório (a pasta que contém `settings.gradle.kts`, **não** a
   raiz do repositório).
3. Aguarde o **Gradle Sync** automático (a primeira vez baixa as
   dependências e pode demorar alguns minutos, dependendo da internet).
4. Se o Android Studio pedir para instalar algum componente do SDK (Android
   SDK Platform 34, Build-Tools, etc.), aceite — ele resolve isso
   automaticamente pelo **SDK Manager**.

Não é necessário nenhum arquivo `local.properties` manual — o Android
Studio gera esse arquivo sozinho apontando para o SDK instalado.

---

## 3. Como compilar

### Pelo Android Studio (mais simples)

- **Build → Make Project** (ou o martelo 🔨 na barra de ferramentas)
  compila o projeto inteiro.
- **Run ▶** com um emulador ou aparelho físico conectado instala e abre o
  app diretamente.

### Pela linha de comando (usando o Gradle Wrapper incluso)

Dentro da pasta `android/`:

```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

O wrapper baixa a versão correta do Gradle (8.7) automaticamente na
primeira execução — não é preciso ter o Gradle instalado manualmente.

---

## 4. Como gerar o APK

### APK de debug (para testar rapidamente)

```bash
./gradlew assembleDebug
```

O APK gerado fica em:

```
app/build/outputs/apk/debug/app-debug.apk
```

### APK de release (assinado, para instalar "de verdade" no seu celular)

1. Gere uma keystore (só precisa fazer isso uma vez), se ainda não tiver
   uma:

   ```bash
   keytool -genkeypair -v -keystore meus-jogos-arbitragem.jks \
     -alias meusjogos -keyalg RSA -keysize 2048 -validity 10000
   ```

2. No Android Studio: **Build → Generate Signed Bundle / APK...** → escolha
   **APK** → selecione a keystore criada → siga o assistente → escolha a
   variante **release**.

   Ou pela linha de comando, configurando a assinatura em
   `app/build.gradle.kts` (bloco `signingConfigs`) e rodando:

   ```bash
   ./gradlew assembleRelease
   ```

   O APK assinado fica em `app/build/outputs/apk/release/app-release.apk`.

O app **não requer nenhuma configuração de assinatura especial** além da
padrão do Android — qualquer keystore serve, inclusive uma pessoal criada
localmente.

---

## 5. Como instalar no Android

### Direto do Android Studio

Conecte o celular via USB com a **Depuração USB** ativada (Configurações →
Opções do desenvolvedor) e clique em **Run ▶**.

### Instalando o APK manualmente

1. Copie o arquivo `.apk` gerado (debug ou release) para o celular (cabo
   USB, e-mail, Google Drive, etc.).
2. No celular, abra o arquivo `.apk` pelo gerenciador de arquivos.
3. Se for a primeira vez instalando um app fora da Play Store, o Android
   vai pedir para permitir "instalar apps de fontes desconhecidas" para o
   app usado para abrir o arquivo (Configurações → o próprio Android
   guia esse passo).
4. Toque em **Instalar**.

### Via ADB (linha de comando)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. Testes

### Testes de lógica financeira (`:core`) — os mais importantes

Reproduzem exatamente os cenários de integridade descritos no briefing do
app (TESTE 1 a TESTE 7: cadastrar, receber, cadastrar outro, excluir,
duplicar, alterar valor, desfazer recebimento — sempre validando que
`TOTAL GERAL = TOTAL RECEBIDO + TOTAL A RECEBER`).

```bash
./gradlew :core:test
```

O relatório em HTML fica em `core/build/reports/tests/test/index.html`.

### Testes instrumentados do Room (`:app`) — TESTE 8 (persistência)

Precisam de um emulador ou aparelho conectado:

```bash
./gradlew :app:connectedAndroidTest
```

Comprovam que os jogos permanecem salvos mesmo fechando e reabrindo o app
(o banco de dados é gravado em disco via Room/SQLite, não em memória).

---

## 7. Visão geral das funcionalidades

- **Cadastro ultrarrápido**: só DATA e VALOR são obrigatórios. Todos os
  demais campos (horário, competição, categoria, equipes, local, função,
  observações) são opcionais e podem ser preenchidos depois, editando o
  jogo.
- **Dashboard**: total a receber, total recebido, total geral (sempre
  recalculado = recebido + a receber), contagem de jogos, e resumo do mês
  atual.
- **Meus Jogos**: lista ordenada do mais recente para o mais antigo, com
  pesquisa (equipe, competição, categoria, local, função, observações) e
  filtros (status, período, competição, função).
- **Detalhes do jogo**: editar, marcar como recebido (com confirmação e
  data ajustável), desfazer recebimento, duplicar (a cópia nasce sempre "A
  RECEBER", sem herdar pagamento nem data de recebimento) e excluir.
- **Resumo**: resumo mensal e anual (com média por jogo), estatísticas
  gerais (maior valor, quantidade recebida/pendente) e gráficos simples de
  barras (jogos por mês; recebido x a receber por mês).
- **Configurações**: backup completo em `.json`, restauração de backup, e
  exportação para `.csv` (abre em Excel/Planilhas Google) — tudo via o
  seletor de arquivos do próprio Android (SAF), sem precisar de permissões
  de armazenamento.
- **Sem alertas/notificações**: por design, o app não implementa nenhum
  tipo de lembrete ou notificação.

---

## 8. Estrutura de dados (Room)

Tabela `jogos`, com os campos sugeridos no briefing: `id`, `data`,
`horario`, `competicao`, `categoria`, `equipe_mandante`,
`equipe_visitante`, `local`, `funcao`, `valor_centavos`,
`status_pagamento`, `data_recebimento`, `observacoes`, `data_criacao`,
`data_atualizacao`. Todos os campos opcionais aceitam `NULL`. Valores
monetários são guardados em **centavos** (inteiro) para garantir que somas
e totais nunca percam precisão.
