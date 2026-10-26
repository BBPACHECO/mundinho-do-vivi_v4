# Mundinho do Vivi — 0.5.0 · teste em família

Aplicativo Android **nativo em Kotlin e Jetpack Compose**, com cinco ambientes locais. Android 10+ (API29), compilação/targetAPI35. Não usa HTML ou WebView.

Esta versão é para o primeiro teste no Galaxy A57. Há atividades implementadas em todas as áreas; o projeto ainda não representa todos os requisitos originais nem uma versão aprovada para a Google Play. Veja [estado e limitações](docs/ESTADO_DO_PROJETO.md) e [validação](docs/VALIDACAO.md).

## O que foi implementado

- **Meu Ateliê:** 22 desenhos originais/8categorias,48cores,RGB personalizada, preencher regiões, pincel/borracha,3carimbos,10passos de desfazer/refazer, zoom com dois dedos, rascunho automático e exportação explícita à galeria. Importação adulta de PNG/JPG e primeira página de PDF, com limites de tamanho.
- **Meu DJ:** dois decks,16ritmos originais,32pads com arquivos CC0 empacotados, BPM/crossfader/volume, sequenciador de16passos e gravação WAV diretamente do mixer, sem microfone. Ouvir/exportar e apagar gravações na área adulta. A biblioteca atual possui **corvo**, mas ainda não possui os latidos/miados e vários outros animais/objetos pedidos.
- **Descobrir:** alfabeto26letras,260entradas locais com palavras/frases e ilustrações vetoriais procedurais; números0–30 e contagem por toque. Quantidades são preservadas mesmo quando o Modo Calminho divide objetos em grupos. Algumas imagens são exemplos de categorias, explicitamente legendados. Voz opcional pt-BR offline do Android.
- **Meus Joguinhos:** voo por obstáculos, corrida de3pistas, sandbox em grade com salvamento, bolhas, memória, puzzle de peças retangulares, encaixe de formas e contagem. Começar/pausar/recomeçar, controles de toque e ajuste sensorial. São minijogos focados, não um mundo3D equivalente a Roblox.
- **Caretas:** CameraX e detector ML Kit **com modelo incluído**,12filtros (7acessórios/máscaras vetoriais rastreados e5deformações reais de pixels), câmera frontal/traseira, troca por gesto e fotografia filtrada com confirmação para salvar. Rastreamento2D de pontos faciais; não reconstrução3D/oclusão. Qualidade física ainda precisa ser avaliada.
- **Responsáveis:** verificação adulta simples, nome, volumes, narração, animação, velocidade/dificuldade, redução de estímulos, vibração, limite de elementos, Modo Calminho, lembrete de pausa, bibliotecas e créditos locais. Ao ir ao segundo plano, o acesso adulto é bloqueado novamente. O seletor de documentos continua registrado para receber importações.

## Instalar o APK no Galaxy A57

Baixe a entrega APK/ZIP fornecida na conversa. Caso seja ZIP, em **Meus Arquivos → Downloads**, extraia e abra o `.apk`. Se o Android solicitar, autorize a instalação somente pela fonte usada para abrir esse arquivo. Esta versão usa assinatura debug para testes e não deve ser enviada à loja.

O aplicativo só pede **câmera**, depois de tocar em Ligar câmera. Não pede microfone, localização, internet ou acesso amplo a arquivos. Para importar, o responsável escolhe o arquivo no seletor do Android; para salvar criações, a ação é explícita. Fotos/rostos ficam no aparelho. Créditos e licenças dos sons estão na área adulta.

Uma voz pt-BR local deve estar instalada nas configurações de texto para voz do Android para haver fala em modo avião. Sem ela, o app continua visualmente sem narração; o app não baixa nem escolhe uma voz que exija rede. A naturalidade depende do pacote instalado.

Siga o [checklist do Galaxy A57](docs/CHECKLIST_GALAXY_A57.md) e registre atividade, passos, Android/One UI e sintoma antes de publicar. A instalação e os testes físicos não foram feitos por este ambiente cloud.

## Android Studio e compilação

Requisitos: JDK17 completo, Android SDK Platform35/BuildTools35.0.0. Gradle Wrapper8.11.1, AGP8.9.2, Kotlin2.1.20 e ComposeBOM2025.04.01 estão fixados no projeto.

Abra esta pasta no Android Studio e sincronize. Configure `ANDROID_HOME` ou `local.properties` com `sdk.dir=/caminho/do/sdk` (ignorado pelo Git).

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-daemon --max-workers=4
```

APK universal debug: `app/build/outputs/apk/debug/app-debug.apk`. Para gerar um APK menor **arm64**, apropriado ao Galaxy A57:

```sh
./gradlew -PgalaxyApk :app:assembleDebug --no-daemon --max-workers=4
```

Essa propriedade limita somente o build selecionado a arm64. A compilação normal continua incluindo as ABIs das dependências. O debug usa `com.mundodedescobertas.app.debug`, release `com.mundodedescobertas.app`. A versão antiga debug pode ser atualizada preservando preferências se a chave de teste for a mesma; instalação release é separada.

A primeira compilação usa rede para ferramentas/bibliotecas; áudio/modelo do aplicativo já acompanham o código/dependências. Os testes Robolectric usam o Android instrumentado do Maven Central. Com todos os artefatos em cache, builds podem ser repetidos com `--offline`. O app instalado não precisa dessa rede.

## Testes e limites de evidência

`testDebugUnitTest` executa testes de catálogo, pixels/histórico, física, geometria facial, mixer/WAV, DataStore e testes de interação Compose via Robolectric. `assembleDebugAndroidTest` só compila os testes de dispositivo. Com aparelho/emulador configurado: `./gradlew :app:connectedDebugAndroidTest`.

Robolectric não prova latência sonora, qualidade/precisão facial, gesto com dois dedos, temperatura, FPS, aparência final ou concessão de permissões no Galaxy. Estes pontos dependem do teste físico. Resultados executados estão em [VALIDACAO](docs/VALIDACAO.md); não deduzir aprovação de um teste que só foi compilado.

## Organização

`data/`: preferências/DataStore; `painting/`: raster/importação/rascunhos; `learning/`: catálogo/arte/contagem; `dj/`: mixer/ritmos/WAV/arquivos; `games/`: física e apresentação; `camera/`: captura/detecção/malha; `audio/`: vozlocal; `ui/`: tema,início,responsáveis. ViewModel/StateFlow e coroutines isolam estado e I/O das telas.

Consulte: [Ateliê](docs/ATELIE.md), [Descobrir](docs/DESCOBRIR.md), [DJ](docs/DJ.md), [Jogos](docs/JOGOS.md), [Câmera](docs/CAMERA.md) e [créditos dos sons](docs/CREDITOS_AUDIO.md).

## Ambiente cloud

`source scripts/cloud-env.sh` ativa JDK/SDK/caches em `/workspace`. `bash scripts/setup-cloud.sh` prepara pré-requisitos e executa as verificações. TLS e checksums continuam ativos; nenhum segredo é gravado no projeto. Builds e renderizações Compose devem rodar sequencialmente no mesmo checkout. Não há aparelho/emulador/KVM preparado no contêiner, e o ADB local encontra uma pasta padrão somente leitura; isso não é defeito do APK.

## Depois: Google Play

Primeiro realizar o teste familiar e corrigir os resultados. [PUBLICACAO_PLAY](docs/PUBLICACAO_PLAY.md) explica assinatura de produção, AAB, targetSDK vigente, política de privacidade, público infantil e revisão de licenças. A conta, chave e publicação em loja não estão configuradas. O workflow GitHub gera APK e relatórios em execuções manuais e nos envios à branch main. APKs debug deste ambiente e do GitHub podem ter chaves de teste diferentes; mantenha a mesma origem dos instaladores durante o teste para preservar atualizações e dados. A chave de produção será configurada separadamente antes da Play.
