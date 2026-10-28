# Mundinho do Vivi — 0.6.1 · teste em família

Aplicativo Android **nativo em Kotlin e Jetpack Compose**, com cinco ambientes locais. Requer Android 10 ou superior (API 29); compilação e target SDK 35. Não usa HTML ou WebView.

A versão 0.6 acrescenta o **Meu Miniestúdio**, dentro de **Caretas Divertidas**, mantendo os cinco acessos da tela inicial. É uma edição de teste familiar; ainda há requisitos do projeto original e testes físicos pendentes. Consulte o [estado e as limitações](docs/ESTADO_DO_PROJETO.md) e os [resultados de validação](docs/VALIDACAO.md).

## Atividades implementadas

- **Meu Ateliê:** 22 desenhos originais em oito categorias, 48 cores, cor RGB personalizada, preenchimento de regiões, pincel e borracha com espessuras, três carimbos, dez passos de desfazer/refazer, zoom, rascunho automático e exportação explícita à galeria. Importação adulta de PNG, JPG e primeira página de PDF.
- **Meu DJ:** dois decks, 16 ritmos originais, 32 pads CC0 locais, BPM, crossfader, volume, sequenciador de 16 passos e gravação WAV diretamente do mixer, sem microfone. Reprodução, exportação e gestão adulta das gravações. A biblioteca possui corvo real, mas ainda faltam latidos, miados e vários outros animais e objetos pedidos.
- **Descobrir:** 26 letras com dez entradas por letra, palavras, frases e ilustrações vetoriais procedurais; números de 0 a 30 e contagem por toque. O Modo Calminho divide objetos em grupos sem mudar a quantidade. Algumas imagens de categoria são explicitamente legendadas. Narração opcional com voz pt-BR offline do Android.
- **Meus Joguinhos:** voo por obstáculos, corrida de três pistas, construção em grade com salvamento, bolhas, memória, quebra-cabeça retangular, formas e contagem. Controles de toque, pausa, reinício e ajustes sensoriais.
- **Caretas Divertidas:** CameraX e ML Kit com modelo incluído, sete acessórios ou máscaras rastreados em 2D e cinco deformações reais de pixels. Troca de câmera, gesto para mudar filtro e fotografia filtrada salva somente após confirmação. O rastreamento não representa reconstrução 3D.
- **Meu Miniestúdio:** escolha de vídeo local, cinco opções de cor, seis emojis com posição, tamanho e intervalo, sons engraçados na linha do tempo e filtros de voz original, neném, grave e robô. Grava uma janelinha da câmera enquanto o vídeo passa, com voz opcional. Renderiza um novo MP4 antes de salvar. Veja [como usar e os limites](docs/VIDEO_EDITOR.md).
- **Responsáveis:** verificação adulta, nome, volumes, narração, animação, velocidade, dificuldade, redução de estímulos, vibração, limite de elementos, Modo Calminho, lembrete de pausa, bibliotecas e créditos. O acesso adulto é bloqueado novamente em segundo plano.

## Instalação e preservação das criações

A nova edição de teste usa o identificador **`com.mundodedescobertas.app.familia`** e aparece no celular como **Mundinho do Vivi • Família**. Ela instala ao lado da versão 0.5, preservando o aplicativo anterior e suas criações. Os dados das duas edições ficam separados; não há migração automática. Mantenha a versão antiga enquanto houver criações que queira guardar.

Downloads da edição Família 0.6.1: [APK para o Galaxy A57](https://github.com/BBPACHECO/mundinho-do-vivi_v4/releases/download/v0.6.1-familia/Mundinho-do-Vivi-0.6.1-Galaxy-A57.apk), [instalador compactado](https://github.com/BBPACHECO/mundinho-do-vivi_v4/releases/download/v0.6.1-familia/Mundinho-do-Vivi-0.6.1-instalar.zip) e [notas da pré-release](https://github.com/BBPACHECO/mundinho-do-vivi_v4/releases/tag/v0.6.1-familia). Os links ficam disponíveis quando o workflow de publicação termina com sucesso.

Abra **Meus Arquivos → Downloads** e toque no APK. Se baixou o ZIP, extraia primeiro. Autorize a instalação somente pela fonte usada para abrir o arquivo, se o Android solicitar. No aplicativo, entre em **Caretas Divertidas → Meu Miniestúdio**.

O [APK histórico 0.5](https://github.com/BBPACHECO/mundinho-do-vivi_v4/releases/tag/v0.5.0-familia) continua disponível e **não contém o mini editor**. Ele usa uma assinatura debug diferente da chave local antiga, o que pode impedir instalação por cima de outro APK com o mesmo identificador. Desinstalar apaga dados privados; exporte criações antes de uma troca desse tipo.

## Privacidade e permissões

O processamento das atividades, fotos, vídeos e voz ocorre no aparelho. O app não pede internet, localização ou acesso amplo a arquivos. A câmera só é solicitada por ação explícita. O microfone é opcional e solicitado somente ao iniciar a gravação da janelinha com **Gravar minha voz** ativado; o DJ continua gravando a mistura digital sem microfone. A recusa do microfone permite gravar uma janelinha silenciosa.

O seletor do Android abre somente o arquivo escolhido. O original do vídeo é preservado; a edição usa cópias privadas temporárias. O vídeo final só vai à galeria após uma ação de salvar. A edição não possui rascunho persistente: salve o resultado antes de sair. Não há anúncios, cadastro ou envio de conteúdo.

Uma voz pt-BR local precisa estar instalada nas configurações de texto para voz do Android para haver narração em modo avião. Sem ela, o app funciona visualmente sem fala. A naturalidade depende do pacote instalado. Os filtros de voz do editor alteram o áudio original inteiro, incluindo música; não isolam fala automaticamente.

## Android Studio e compilação

Requisitos fixados: JDK 17, Android SDK Platform 35, Build Tools 35.0.0, Gradle Wrapper 8.11.1, AGP 8.9.2, Kotlin 2.1.20 e Compose BOM 2025.04.01. O editor usa Media3 1.7.1 e CameraX 1.4.2.

Abra esta pasta no Android Studio e sincronize. Configure `ANDROID_HOME` ou um `local.properties` com `sdk.dir=/caminho/do/sdk`, ignorado pelo Git.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-daemon --max-workers=4
```

Saída debug: `app/build/outputs/apk/debug/app-debug.apk`, pacote `com.mundodedescobertas.app.debug`. Para limitar o APK a arm64, apropriado ao Galaxy A57:

```sh
./gradlew -PgalaxyApk :app:assembleDebug --no-daemon --max-workers=4
```

Para compilar a edição Família, configure de forma privada `VIVI_FAMILY_KEYSTORE_PATH`, `VIVI_FAMILY_STORE_PASSWORD` e `VIVI_FAMILY_KEY_ALIAS`; a senha da chave deve ser a mesma do keystore. Depois:

```sh
./gradlew -PfamilyApk -PgalaxyApk :app:assembleRelease --no-daemon --max-workers=4
```

Saída: `app/build/outputs/apk/release/app-release.apk`. A chave de teste criada para esta edição fica fora do repositório. Somente sua impressão digital pública está em [FAMILIA_CERT_SHA256.txt](docs/FAMILIA_CERT_SHA256.txt). Não versione keystores, senhas ou metadados privados. Preserve uma cópia segura da mesma chave antes de depender de atualizações futuras. A chave de teste não é a chave de produção da Play.

O workflow `gerar-apk.yml` compila e valida debug. Seu job de assinatura Família só roda na `main` confiável quando existem os três secrets `VIVI_FAMILY_KEYSTORE_BASE64`, `VIVI_FAMILY_STORE_PASSWORD` e `VIVI_FAMILY_KEY_ALIAS`. `publicar-familia.yml` prepara uma pré-release manual após as verificações. A configuração desses secrets ainda está pendente; o fluxo de assinatura automática não está pronto somente porque o YAML existe. O workflow histórico `entregar-apk-teste.yml` é específico para a entrega 0.5.

Para esta entrega, `publicar-apk-preassinado.yml` recebe o instalador já assinado em uma branch separada de distribuição. Confere que o código coincide com o commit de origem, valida hashes, certificado e alinhamento, executa as verificações e publica o download. A chave privada permanece fora do Git e do instalador; nenhum binário de distribuição é adicionado à `main`.

A primeira compilação requer rede para ferramentas e bibliotecas. Áudios e modelo facial acompanham o app. Os testes Robolectric obtêm o Android instrumentado do Maven Central; após preencher os caches, o build pode usar `--offline`. O app instalado funciona offline.

## Testes e limites da evidência

`testDebugUnitTest` inclui testes de dados, pintura, física, geometria facial, áudio, importação de vídeo e interações Compose em Robolectric. `assembleDebugAndroidTest` apenas compila os testes de dispositivo. Para executá-los com aparelho ou emulador configurado:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Os testes de vídeo em dispositivo incluem composição de pixels, duração, áudio e mudança real de frequência. Sua existência ou compilação não comprova que foram executados. O resultado efetivamente obtido fica em [VALIDACAO](docs/VALIDACAO.md).

Robolectric não comprova codecs, sincronização da câmera com o vídeo, qualidade final dos filtros, microfone, latência, zoom, FPS ou temperatura no Galaxy. Execute o [checklist do Galaxy A57](docs/CHECKLIST_GALAXY_A57.md) e registre versão Android/One UI, versão do app e passos de qualquer falha.

## Organização e ambiente cloud

`data/` guarda preferências; `painting/`, pintura e rascunhos; `learning/`, catálogo e contagem; `dj/`, mixer e WAV; `games/`, física e apresentação; `camera/`, detecção e deformações; `video/`, importação, captura, efeitos e exportação; `audio/`, narração; `ui/`, tema, início e responsáveis. ViewModel, StateFlow e coroutines separam estado e operações de entrada e saída.

Consulte também [Ateliê](docs/ATELIE.md), [Descobrir](docs/DESCOBRIR.md), [DJ](docs/DJ.md), [Jogos](docs/JOGOS.md), [Câmera](docs/CAMERA.md) e [créditos dos sons](docs/CREDITOS_AUDIO.md).

No ambiente cloud, `source scripts/cloud-env.sh` ativa as ferramentas e `bash scripts/setup-cloud.sh` prepara pré-requisitos. Mantenha TLS e checksums ativos. Rode builds e renderizações sequencialmente no mesmo checkout. A disponibilidade do ambiente, do ADB e de um emulador precisa ser conferida; ela não equivale à validação em celular físico.

## Depois: Google Play

Primeiro faça o teste familiar e corrija os resultados. [PUBLICACAO_PLAY](docs/PUBLICACAO_PLAY.md) descreve assinatura de produção, AAB, target SDK vigente, política pública, público infantil e licenças. Conta, chave de produção e publicação em loja não estão configuradas. Nenhuma aprovação da Play é presumida nesta entrega.
