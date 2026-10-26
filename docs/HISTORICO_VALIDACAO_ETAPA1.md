# Validação da etapa 1 — 26/10/2026

Executado neste ambiente Linux, com JDK completo Temurin 17.0.17+10, Gradle Wrapper 8.11.1, AGP 8.9.2, Kotlin 2.1.20, Android Platform 35 e Build Tools 35.0.0.

| Verificação | Resultado |
|---|---|
| `bash scripts/setup-cloud.sh` | Executado com sucesso, incluindo repetição com ferramentas/dependências instaladas |
| `:app:assembleDebug` | APK gerado |
| `--offline :app:assembleDebug` | Build repetido sem rede com os artefatos em cache; não equivale a teste offline no telefone |
| `:app:testDebugUnitTest` | 6 testes executados; 0 falhas, 0 erros, 0 ignorados |
| Regras sensoriais e verificação adulta | 4 testes passaram |
| DataStore: reabrir arquivo e alterações simultâneas | 2 testes passaram |
| `:app:lintDebug` | 0 erros, 0 avisos |
| `:app:assembleDebugAndroidTest` | APK de testes compilado; execução não realizada |
| `apksigner verify` | Assinatura debug válida, esquema v2 |
| `zipalign -c -P 16 4` | Alinhamento do ZIP validado; não é teste de execução em aparelho com páginas de 16 KB |
| Manifesto do APK via `aapt` | Sem permissões Internet, câmera, microfone, localização ou arquivos; apenas permissão interna de assinatura do AndroidX |
| ABIs do APK | arm64-v8a, armeabi-v7a, x86 e x86_64 |
| Renderização de `HomePreview` via Android CLI | Tela Compose renderizada e inspecionada sem dispositivo; não valida interação |
| Instalação, navegação e TalkBack no Galaxy A57 | Não executados; seguir checklist |
| Narração offline e conforto sensorial no telefone | Não executados; dependem da voz local/aparelho e avaliação com o responsável |
| Pintura, DJ, jogos, câmera | Ainda não implementados nem testados nesta etapa |

Relatórios gerados: `app/build/reports/tests/testDebugUnitTest/` e `app/build/reports/lint-results-debug.html`. Não confundir compilar os testes de interface com executá-los.

O ambiente não tem celular, emulador ou `/dev/kvm` preparados. A tentativa de consultar ADB neste contêiner esbarrou no diretório padrão `/home/agent/.android`, somente leitura; não foi instalada nem iniciada uma aplicação Android aqui. Essa limitação não invalida a compilação nem os testes JVM executados.

O script foi ajustado para usar o proxy injetado e o truststore público do sistema com TLS verificado. SDK/JDK foram obtidos de fontes oficiais com verificação de checksum; a verificação de rede nunca foi desativada. A primeira tentativa de renderizar Compose simultaneamente ao build causou conflito de caches Kotlin; a renderização foi interrompida, os caches incrementais gerados foram recriados e as verificações finais foram executadas sequencialmente com sucesso.

As instruções `install_script` e `start_skill` foram salvas no rascunho do ambiente. Isso não publica o ambiente nem envia o projeto ao GitHub. A configuração e o snapshot devem ser revisados/salvos e publicados pelo usuário nas configurações do ambiente.
