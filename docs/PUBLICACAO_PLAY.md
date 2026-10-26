# Depois do teste no Galaxy A57

O objetivo atual é a instalação familiar do APK 0.5.0 de teste. Não existe publicação na Google Play nesta entrega.

## Caminho para distribuição

1. Testar as cinco atividades no Galaxy A57 seguindo o checklist, incluindo modo avião, importações, saída de áudio e câmera. Registrar falhas com versão Android/One UI e os passos exatos.
2. Corrigir resultados reais, completar as gravações naturais pedidas e revisar a correspondência das ilustrações educativas. Repetir testes e confirmar conforto sensorial com o responsável.
3. Revisar o inventário de licenças, as condições do modelo/detector ML Kit e os avisos obrigatórios de todas as dependências. Validar suporte real de bibliotecas nativas a aparelhos com páginas de 16 KB; alinhamento de ZIP sozinho não prova isso.
4. Preparar a versão release com assinatura própria do proprietário, guardada fora do repositório. Não usar a chave debug na loja. Configurar Play App Signing e proteger a chave de upload, senhas e credenciais do Play Console em armazenamento seguro.
5. Conferir no Play Console o target SDK exigido na data da publicação (esta compilação usa API 35) e atualizar SDK/AGP quando necessário. Confirmar requisitos de teste da conta; variam conforme tipo/data de criação.
6. Criar política de privacidade pública, listagem, ícone, capturas reais, classificação indicativa, público infantil e formulários de Segurança dos dados/permissão de câmera. Declarar fielmente o comportamento do aplicativo e das dependências. As fotos são opcionais e processadas/salvas no dispositivo.
7. Gerar o AAB assinado, enviar primeiro à faixa de teste apropriada, validar a instalação pela loja e só depois solicitar produção com aprovação do proprietário.

O projeto debug usa `com.mundodedescobertas.app.debug`, e o release usa `com.mundodedescobertas.app`. Dados da versão debug não migram automaticamente para a versão da loja. Criar um caminho de exportação/importação para criações a preservar antes dessa troca.

A conta Play Console, a assinatura de produção e os dados públicos da listagem não foram fornecidos. Não se deve inventar credenciais, publicar automaticamente ou afirmar que a loja aprovou o aplicativo.
