# Estado — versão 0.6.1 para teste familiar

As cinco áreas abrem atividades Android nativas. O novo **Meu Miniestúdio** fica dentro de **Caretas Divertidas**, mantendo os cinco acessos da tela inicial. O código da versão 0.6 inclui o editor; a publicação de um APK assinado e os resultados finais devem ser conferidos nas notas da entrega e em [VALIDACAO](VALIDACAO.md).

## Implementado

- **Ateliê:** 22 desenhos originais, 48 cores, preenchimento, pincel, borracha, espessuras, RGB, três carimbos, dez passos de histórico, zoom, rascunhos, galeria e importação adulta.
- **Descobrir:** 26 letras com dez entradas por letra, números de 0 a 30, voz local, contagem por toque e agrupamento sensorial que preserva o total.
- **DJ:** dois decks, 16 ritmos, 32 sons CC0, mixer, sequenciador de 16 passos de uma pista, gravação digital, reprodução, exportação e gestão adulta.
- **Jogos:** oito minijogos com interação, pausa, ajustes e construção em grade com salvamento.
- **Câmera:** sete acessórios ou máscaras rastreados em 2D e cinco deformações reais de imagem, modelo incluído, permissão explícita e foto salva após confirmação.
- **Vídeo:** importação de vídeo local de até 90 segundos e 500 MiB, cinco opções de cor, seis tipos de emoji com até oito inserções, seis sons locais com até 12 inserções, posição e intervalo dos emojis, gravação de câmera enquanto o vídeo passa e voz opcional. Filtros de voz original, neném, grave e robô. Renderização MP4 H264/AAC até HD e 30 FPS, reprodução do resultado, cancelamento e confirmação de salvamento na galeria. A edição vale para a sessão; o original é preservado. Veja [VIDEO_EDITOR](VIDEO_EDITOR.md).

## Instalação da edição Família

O pacote de teste `com.mundodedescobertas.app.familia`, exibido como **Mundinho do Vivi • Família**, permite manter a versão anterior instalada. Os dados não migram automaticamente entre pacotes. Uma chave de teste local foi criada fora do Git; apenas sua impressão digital pública está versionada. O backup seguro da chave e a configuração de secrets no GitHub ainda precisam ser concluídos antes de prometer atualizações assinadas automaticamente. Não há chave de produção da Play configurada.

## Limitações do editor

- A prévia de edição mostra cores e emojis com o áudio original. Para ver a janelinha e ouvir os efeitos de voz e sons misturados, é preciso usar **Ver resultado** e reproduzir o MP4 renderizado.
- O filtro de voz modifica a faixa inteira, incluindo música, e a voz opcional da câmera. Não separa fala, música ou ruídos. Os efeitos sonoros adicionados mantêm seu som original.
- A câmera grava a partir do começo do vídeo. A janelinha pode terminar antes dele; posição em um dos quatro cantos e tamanho são ajustados depois. Não há recorte de pessoa ou remoção de fundo.
- A edição não possui projeto persistente nem linha do tempo de corte ou união de vídeos. Sair da atividade ou perder o processo pode descartar mudanças não salvas. O MP4 confirmado na galeria permanece.
- Formatos, HDR, codecs, qualidade, sincronização, espelhamento, gravação de voz e custo térmico precisam de teste real. Um formato incompatível deve produzir aviso; não há garantia de aceitar todo arquivo de vídeo do Android.
- Não existe teste físico aprovado para câmera, microfone, sincronização ou exportação no Galaxy A57 somente por compilar o projeto.

## Lacunas do pedido original

- Faltam gravações naturais de cachorro, gato, pato, vaca, cavalo, leão, passarinho e macaco, além de risada, panela e vários objetos. Há corvo real. Os sons ausentes não foram substituídos por imitações sintetizadas. O acesso às fontes licenciadas depende da política de rede efetivamente aplicada.
- Os 16 ritmos são padrões originais curtos do motor musical. O sequenciador usa um pad por vez. Timbres, sincronismo e resposta precisam de avaliação no aparelho.
- As 260 entradas educativas usam arte vetorial procedural por tipo de objeto e algumas representações de categoria ou nome com legenda. Não são 260 ilustrações individuais feitas à mão. A narração usa TTS pt-BR offline do dispositivo; não foram produzidas gravações humanas próprias.
- O sandbox é pequeno e em grade 2D. O quebra-cabeça usa peças retangulares. O suporte TalkBack individual a todos os objetos desenhados nos Canvas dos jogos permanece incompleto.
- A detecção facial acompanha pontos, escala e inclinação em 2D. Não garante oclusão 3D, expressões ou estabilidade em qualquer ângulo. As deformações alteram pixels; a qualidade ainda precisa de avaliação. A câmera de filtros limita quadros a 640 px para conter custo e memória.
- Acessibilidade gestual, desempenho, latência, temperatura, permissões e operações MediaStore/SAF devem ser testados no Galaxy A57.
- Faltam assinatura de produção, revisão das licenças e condições transitivas, listagem e política pública da Play. Não houve publicação na loja.

## Próximas verificações

As verificações locais e a assinatura da versão 0.6 estão registradas em [VALIDACAO](VALIDACAO.md). Confira a conclusão do workflow de publicação e o download da pré-release. No Galaxy A57, execute o checklist e registre falhas. Complete os áudios quando houver fontes licenciadas acessíveis, corrija os resultados e só então prepare a Google Play.
