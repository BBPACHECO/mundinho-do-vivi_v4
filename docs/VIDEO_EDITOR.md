# Meu Miniestúdio — editor de vídeo 0.6.1

Abra **Caretas Divertidas → Meu Miniestúdio**. O editor mantém o processamento no aparelho e cria um novo arquivo; o vídeo escolhido não é sobrescrito.

## Criar uma edição

1. Toque em **Escolher um vídeo** e selecione um arquivo local no seletor do Android. O limite é de 90 segundos e 500 MiB. O app faz uma cópia privada temporária para continuar trabalhando mesmo que o provedor revogue acesso ao arquivo original.
2. Use **Tocar vídeo**, a posição no vídeo e **Desde o começo** para escolher o momento das inserções. Pause no ponto desejado antes de adicionar emojis ou sons.
3. Escolha uma cor: original, quentinho, azulzinho, rosinha ou preto e branco. A prévia da imagem usa os mesmos efeitos de cor da exportação.
4. Adicione sorriso, coração, estrela, risada, arco-íris ou festa. São permitidos até oito emojis. Cada um tem controles de posição horizontal e vertical, tamanho, início, fim e remoção. Os glifos vêm do Android, portanto a aparência pode variar por aparelho.
5. Ouça os sons disponíveis e toque em **Colocar** no momento escolhido: arroto, scratch, bolha eletrônica, twang, gota eletrônica e sino. Há até 12 inserções. Os WAVs ficam no aplicativo e mantêm os créditos CC0 da biblioteca do DJ. Cada inserção pode ser retirada.
6. Escolha uma voz: original, neném, grave ou robô. Os filtros de neném e grave mudam o tom sem acelerar a reprodução. O robô aplica modulação digital. O efeito modifica o áudio original inteiro e a voz da janelinha, incluindo músicas e ruídos; não identifica nem isola a fala. Sons acrescentados conservam seu som original.

A reprodução na edição e durante a reação respeita o volume de efeitos e o Modo Calminho dos responsáveis. O volume original a incorporar no arquivo salvo pode ser ajustado separadamente. Caso o vídeo não tenha áudio, o filtro de voz só será audível se houver uma gravação da câmera com voz.

## Gravar a janelinha da câmera

Toque em **Gravar minha janelinha**, ligue a câmera e autorize seu uso se solicitado. É possível usar câmera frontal ou traseira, quando disponível. Ao iniciar a gravação, o vídeo-base volta ao começo e reproduz enquanto a câmera grava sua reação. Pare manualmente ou deixe terminar junto do vídeo, até o limite de 90 segundos.

**Gravar minha voz** começa desligado. O microfone só é solicitado ao iniciar com essa opção ligada. Se a permissão for negada, a gravação pode continuar silenciosa. O DJ e a exportação digital não dependem de microfone. Usar fones durante a reação ajuda a impedir que o microfone capture também o áudio do vídeo reproduzido pelo alto-falante.

Depois, escolha um dos quatro cantos e ajuste o tamanho da janelinha, de 20% a 45% da largura do vídeo. Também é possível retirá-la ou gravar novamente. Se houver voz, há controle de volume separado para ela. A reação começa em zero; não há deslocamento livre de seu início. Uma reação mais curta desaparece antes do fim, mantendo o restante do vídeo-base.

Sair da captura ou colocar o app em segundo plano desliga a câmera, pausa o vídeo e descarta uma gravação interrompida. Não há remoção de fundo, recorte automático da pessoa ou filtro facial adicional nesse editor.

## Ver e salvar o resultado

Durante a edição, a prévia exibe cores e emojis com o áudio original. Para conferir voz, sons e janelinha juntos, toque em **Ver resultado**. O app decodifica, compõe e codifica um novo MP4 H264/AAC, com resolução limitada a HD — até 1280 × 720 ou 720 × 1280, mantendo a proporção — e até 30 FPS. Vídeos menores não são ampliados para HD.

A exportação mostra progresso e pode ser cancelada. Ao colocar o app em segundo plano, ela é cancelada; a edição continua disponível enquanto a sessão existir. A ausência de codec compatível, arquivo incompatível ou espaço insuficiente deve gerar aviso para tentar um vídeo menor ou outro formato. Alguns arquivos HDR podem não ser aceitos no aparelho.

O resultado pronto pode ser reproduzido antes de salvar. **Salvar vídeo na galeria** pede confirmação e cria uma cópia em **Filmes/MundinhoDoVivi**. Cancelar a confirmação não grava na galeria. Cópias parciais de uma gravação na galeria que falhe ou seja cancelada são removidas.

## Limites e preservação

- A edição existe apenas durante a sessão. Não há salvamento de projeto para continuar depois, corte, união de clipes ou recuperação garantida após perda do processo. Salve o resultado antes de sair da atividade.
- A importação usa até 500 MiB, além do espaço necessário à exportação. O app precisa de espaço livre; arquivos temporários de sessões anteriores são limpos quando é seguro fazê-lo.
- A prévia do resultado completo depende de renderizar o vídeo. Não há promessa de composição em tempo real de voz, efeitos e câmera durante a edição.
- A sincronização exata, os codecs, o espelhamento, a qualidade de áudio e vídeo e o desempenho térmico exigem teste em um Galaxy A57. Testes Robolectric e a compilação dos testes de dispositivo não comprovam esses comportamentos físicos.

## Estrutura técnica

O pacote `video/` separa contrato e limites (`VideoProject`), importação e estado (`VideoEditorViewModel`), interface e prévia, captura (`ReactionRecorder`), efeitos (`VideoRenderEffects` e `VoiceAudioEffects`), composição (`VideoExporter`) e persistência explícita na galeria (`VideoMediaStore`).

Media3 1.7.1 fornece ExoPlayer, composição e codificação. CameraX 1.4.2 fornece a captura. O app mantém as permissões de rede removidas e não envia arquivos para renderização em servidores.

Há testes unitários de cópia limitada, cancelamento, geometria e processamento PCM. Os testes instrumentados usam três clipes pequenos originais para verificar pixels, duração, áudio e mudança de frequência. Consulte [VALIDACAO](VALIDACAO.md) para saber quais foram efetivamente executados.
