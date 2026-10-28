# Depois do teste no Galaxy A57

O objetivo atual é a instalação familiar da versão 0.6.0, com o mini editor de vídeo. Não existe publicação na Google Play nesta entrega. A disponibilidade de um APK assinado deve ser conferida nas notas da versão; código ou workflow preparado não equivalem a uma entrega publicada.

## Caminho para distribuição

1. Testar as cinco atividades e o Miniestúdio no Galaxy A57 seguindo o checklist, incluindo modo avião, importações, saída de áudio, câmera, voz opcional, codecs, sincronização e exportação de vídeo. Registrar falhas com versão Android/One UI e os passos exatos.
2. Corrigir resultados reais, completar as gravações naturais pedidas e revisar a correspondência das ilustrações educativas. Repetir testes e confirmar conforto sensorial com o responsável.
3. Revisar o inventário de licenças, as condições do modelo/detector ML Kit e os avisos obrigatórios de todas as dependências. Validar suporte real de bibliotecas nativas a aparelhos com páginas de 16 KB; alinhamento de ZIP sozinho não prova isso.
4. Preparar a versão release com assinatura própria do proprietário, guardada fora do repositório. Não usar a chave debug nem a chave de teste Família na loja. Configurar Play App Signing e proteger a chave de upload, senhas e credenciais do Play Console em armazenamento seguro.
5. Conferir no Play Console o target SDK exigido na data da publicação (esta compilação usa API 35) e atualizar SDK/AGP quando necessário. Confirmar requisitos de teste da conta; variam conforme tipo/data de criação.
6. Criar política de privacidade pública, listagem, ícone, capturas reais, classificação indicativa, público infantil e formulários de Segurança dos dados/permissões de câmera e microfone. Declarar fielmente o comportamento do aplicativo e das dependências. Fotos, vídeos e voz são opcionais e processados no dispositivo; o microfone só é solicitado na gravação da própria voz. A galeria recebe uma cópia somente por ação explícita.
7. Gerar o AAB assinado, enviar primeiro à faixa de teste apropriada, validar a instalação pela loja e só depois solicitar produção com aprovação do proprietário.

O projeto debug usa `com.mundodedescobertas.app.debug`; a edição release padrão usa `com.mundodedescobertas.app`. A opção `-PfamilyApk` cria a edição de teste separada `com.mundodedescobertas.app.familia`, que pode conviver com a anterior. Dados desses pacotes não migram automaticamente para a versão da loja. Criar um caminho de exportação/importação para criações a preservar antes dessa troca. O editor não salva projetos para retomar depois; confirme a exportação dos vídeos na galeria antes de sair.

A chave local de teste Família fica fora do repositório, e somente seu fingerprint público é versionado. Os secrets de assinatura automática no GitHub ainda precisam ser configurados. Uma impressão digital pública ou um YAML não substituem o backup seguro da chave privada. Essa preparação de teste não configura credenciais de publicação na Play.

A conta Play Console, a assinatura de produção e os dados públicos da listagem não foram fornecidos. Não se deve inventar credenciais, publicar automaticamente ou afirmar que a loja aprovou o aplicativo.
