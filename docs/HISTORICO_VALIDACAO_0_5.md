# Validação — 0.5.0-familia — 26/10/2026

Executado em Linux/JDK17completo, GradleWrapper8.11.1, AGP8.9.2, Kotlin2.1.20, AndroidSDK35/BuildTools35.0.0.

Comando final: `./gradlew -PgalaxyApk :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --no-daemon --max-workers=4`.

| Verificação | Resultado executado |
|---|---|
| Compilação e empacotamento arm64 | Sucesso; versão5 / 0.5.0-familia |
| Testes totais | **49 executados, 0falhas, 0erros, 0ignorados** |
| Regras, persistência, pixels, catálogo, física, áudio/WAV e geometria | 35 testes passaram |
| Interações Compose nativas/Robolectric API35 | 14 testes passaram |
| Navegação | Cinco destinos/voltar, mascote e proteção adulta exercitados |
| Aprendizagem |26letras,10palavrasA/falas solicitadas,31números,zero,contagem5emgrupos3+2,semduplicação |
| Jogos |8jogos abrem/iniciam;bolhas respondem;pausa e sandboxsalvo/restaurado por toque |
| Pintura |PNGmodificado salvo/restaurado;importação válida e rejeição inválida sem lixo |
| Câmera virtual | Desligada e sem fotografar/trocar antes de autorização explícita |
| Renderização visual |6PNGs da Activity real via decorView.draw(Canvas), configuração simulada393×852dp |
| Análise Android/lint |0erros;6avisos de compatibilidade ChromeOS, espaço em disco e estiloKTX |
| APK/assinatura |Verificação apksigner válida e alinhamento ZIP16KB confirmado |
| Bibliotecas arm64 |5ELFs com segmentosLOAD alinhados a0x4000; não substitui execução em dispositivo16KB |
| Permissões finais |CAMERA + permissão interna de assinaturaAndroidX;semInternet,microfone,localização ou arquivos amplos |
| Modelo facial offline | Arquivos de modelo e JNI do MLKit encontrados no artefato empacotado |
| Áudios |32WAVsCC0:hashesSHA256 batem com créditos;PCMmono48kHz/16bit,dadosnão vazios |
| Testes instrumentados Android |Compilados; **não executados em celular** |

As49verificações de software não provam latência sonora, pronúncia/naturalidade TTS, precisão/fotografia de rostos, gesto de2dedos, galeria/seletor reais,60FPS,temperatura ou conforto sensorial no GalaxyA57. Esses testes físicos estão no checklist e ainda não foram executados.

O runner Robolectric inicialmente parou no lock em /home/agent somenteleitura, antes de executar14testes. A JVM de teste passou a usar um diretório próprio sob GRADLE_USER_HOME, mantendo TLS/checksum ativos. Depois, todos49passaram. A captura PixelCopy não é implementada neste runner; a árvore real da Activity foi desenhada diretamente em Bitmap. A revisão dessas imagens identificou sobreposição na câmera, corrigida com dimensões limitadas; o comando final repetiu49testes e gerou as capturas corrigidas.

Relatórios: app/build/reports/tests/testDebugUnitTest/, app/build/reports/lint-results-debug.html e app/build/reports/ui/. O APK é debug para o teste familiar, não uma versão assinada/aprovada para publicação. Os animais/objetos de áudio ainda faltantes e demais limites estão em ESTADO_DO_PROJETO.md.
