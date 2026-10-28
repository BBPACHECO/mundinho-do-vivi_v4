# Validação — 0.6.0-familia — 27/10/2026

Ambiente Linux, JDK 17, Gradle 8.11.1, AGP 8.9.2, Kotlin 2.1.20 e Android SDK 35/Build Tools 35.0.0. Data em horário de Brasília. O [relatório anterior](HISTORICO_VALIDACAO_0_5.md) preserva a evidência da versão 0.5.

## Verificações executadas

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --offline --no-daemon --max-workers=2
```

O comando passou após a recuperação do ambiente e novamente após ajustar o enquadramento vertical da tela de gravação. A segunda execução terminou em 1 min 22 s. A passagem anterior à interrupção da nuvem também havia executado 57 testes; ela não foi usada para presumir a validade das alterações posteriores.

| Verificação | Resultado observado |
|---|---|
| Compilação debug e APK de testes Android | Sucesso |
| Testes locais | **57 executados, zero falhas, erros ou ignorados** |
| Regras, arquivos, persistência, física, áudio e geometria | 43 testes passaram |
| Interações Compose/Robolectric API 35 | 14 testes passaram |
| Editor: importação | Cópia no limite, rejeição de excesso/arquivo vazio e cancelamento passaram |
| Editor: processamento de áudio | Robô preserva canais e fase entre buffers; ganho e silêncio mantêm as amostras esperadas |
| Editor: geometria | Resolução par limitada a HD, sem ampliar entradas menores; início/fim da reação testados |
| Navegação | Cinco ambientes, entrada no Miniestúdio e retorno exercitados na Activity real |
| Renderização visual | Sete capturas da Activity em configuração simulada de 393 × 852 dp, incluindo o editor sem vídeo escolhido |
| Lint | Zero erros e 12 avisos: compatibilidade ChromeOS, consulta de espaço e sugestões de estilo KTX |

Também foi compilado o APK Família arm64 com `-PfamilyApk -PgalaxyApk :app:assembleRelease`, usando a chave privada de teste local por variáveis de ambiente. Assinatura conferida com `apksigner` e comparada à impressão digital pública em `FAMILIA_CERT_SHA256.txt`; alinhamento ZIP de 16 KB e integridade do arquivo confirmados. As cinco bibliotecas arm64 têm segmentos ELF LOAD alinhados a pelo menos `0x4000`. Isso não substitui execução em um aparelho com páginas de 16 KB.

O manifesto final informa pacote `com.mundodedescobertas.app.familia`, versão 6 / `0.6.0-familia`, Android mínimo 29 e alvo 35. Permissões: câmera, microfone opcional e a permissão interna AndroidX. Não há internet, localização ou acesso amplo a arquivos. Câmera e microfone foram declarados como hardware opcional.

## Tentativa de testes em Android real emulado

Foi preparado um emulador API 35 x86_64 com imagem e ferramenta oficiais verificadas, SwiftShader e execução por software sem KVM. O APK debug universal instalou com `Success`. Durante a instalação do APK instrumentado, o Watchdog do Android encerrou o `system_server` do emulador; o serviço PackageManager retornou `Broken pipe`.

**Nenhum teste instrumentado de exportação foi executado.** O problema ocorreu no sistema do emulador, antes de iniciar os testes; não é evidência de uma falha do aplicativo. O emulador foi encerrado. Não foram desativadas verificações, alteradas asserções ou forçadas propriedades de inicialização para produzir um resultado positivo.

Os dois testes de vídeo foram compilados e podem ser executados em Android compatível. Eles verificam composição por pixels, duração da base com reação curta, emojis e som codificados, e mudança real de frequência de 440 Hz para aproximadamente 704 Hz com a voz de neném. As três pequenas amostras de vídeo são originais, geradas para o teste e incluídas somente no APK de instrumentação.

## Validação ainda necessária no Galaxy A57

Não foi conectado um Galaxy A57. Permanecem pendentes: concessão/recusa de câmera e microfone, gravação da reação e sincronização com a base, codecs e HDR, filtros e áudio no MP4 final, gravação na galeria, seletor de documentos, desempenho, temperatura e conforto sensorial. A aparência da câmera e a pronúncia da narração também dependem do aparelho.

O filtro de voz afeta a faixa original inteira e a voz da reação; não há separação automática de fala e música. A prévia da edição mostra cores e emojis com áudio original. Para ouvir a mixagem e os filtros e ver a reação, é preciso renderizar e assistir ao resultado. A edição fica apenas na sessão; o resultado salvo é um novo arquivo e o vídeo original é preservado.

Consulte [CHECKLIST_GALAXY_A57.md](CHECKLIST_GALAXY_A57.md) e [VIDEO_EDITOR.md](VIDEO_EDITOR.md). Os demais recursos e limites do aplicativo estão em [ESTADO_DO_PROJETO.md](ESTADO_DO_PROJETO.md).

Relatórios locais: `app/build/reports/v0.6/`, `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/lint-results-debug.html` e `app/build/reports/ui/`. A distribuição é uma pré-release familiar assinada para testes, sem publicação na Google Play.
