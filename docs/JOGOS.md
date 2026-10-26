# Jogos nativos implementados

API de integração: `GamesScreen(settings: ChildPreferences, onSpeak: (String) -> Unit, onStopSpeech: () -> Unit = {})`. Ligar `onStopSpeech` ao `LocalNarrator.stop` compartilhado para interromper falas ao pausar, recomeçar ou voltar à biblioteca.

Os oito itens da biblioteca abrem mecanismos jogáveis em Canvas. Cada um tem início explícito, instrução, pausa/retomada, retorno à biblioteca e reinício. O back Android dentro de um jogo retorna à biblioteca; o voltar global continua disponível. Sair para segundo plano pausa a simulação e os sons. O loop limita o delta temporal a 40 ms para não saltar a física após interrupções.

- **Passarinho Aventureiro:** toque/botão para bater asas, gravidade, colisão contra limites e árvores, obstáculos gerados com três larguras de passagem, pontuação por obstáculo e reinício imediato após contato. O cenário e o personagem são originais.
- **Corrida das Cores:** cenário com perspectiva de três pistas, gestos laterais/para cima e botões equivalentes, salto com física, caixas para evitar e estrelas para coletar. A velocidade cresce gradualmente e é limitada. O modo livre evita pressão; contatos são gentis e preservam estrelas.
- **Mundinho de Blocos:** mapa de 10 × 8 células, personagem com movimento nas quatro direções e salto, posicionamento de casas, árvores, flores, bichinhos e blocos, ferramenta de apagar. O mundo é salvo em arquivo privado com AtomicFile, periodicamente e ao sair/pausar. Retomar não limpa a construção.
- **Bolhas Mágicas:** bolhas em movimento com tamanhos/velocidades variadas, toque para estourar, partículas discretas e reposição contínua; não há cronômetro. Quantidade e velocidade respeitam as preferências sensoriais.
- **Memória:** 8, 12 ou 16 cartas com imagens vetoriais, animação de virada, comparação real de pares e tempo para observar duas cartas diferentes. Não há penalidade por tentativa.
- **Quebra-cabeça:** uma ilustração original de vila, com 4, 9 ou 16 peças. Arraste uma peça para seu lugar: ela encaixa dentro do raio magnético, troca com a peça deslocada e permanece marcada. O botão Ver desenho mostra a imagem completa por dois segundos. As peças são retangulares, sem recorte ornamental de jigsaw.
- **Formas e Cores:** círculo, quadrado, triângulo e estrela para arrastar aos contornos correspondentes; também permite tocar na forma e depois no contorno. Há confirmação do encaixe e nova distribuição.
- **Aventura dos Números:** desafios de coletar de 1 a 15 estrelas, conforme dificuldade, com contagem narrada por toque e novo desafio após completar. Os objetos chegam em grupos limitados pela preferência sensorial, sem alterar a quantidade solicitada. Flores são distrações sem punição.

Os sons são pequenos sinos sintetizados originais em WAV, reproduzidos por SoundPool, com envelope suave e até quatro vozes. Não representam gravações de animais ou objetos. O volume efetivo dos efeitos e o volume do Android são respeitados; a narração usa o callback compartilhado de voz offline. Não há microfone, rede, anúncios, chat nem multiplayer.

## Verificação e limites

Foram adicionados seis testes JVM para: início/impulso do voo, limite temporal, colisão e pontuação, pistas/salto/coleta, recuperação do mundo e rejeição de arquivo inválido, e raio magnético de encaixe. O agente integrador executa a compilação e os testes do conjunto.

É necessária validação em aparelho para responsividade de gestos, telas com fontes grandes, áudio, conforto sensorial, aquecimento e FPS. Não há medição de 60 FPS no Galaxy A57. Os alvos desenhados no Canvas ainda não têm nós individuais de navegação TalkBack; há descrição global do estado e botões nativos equivalentes para voo, corrida e movimento. O sandbox é uma área pequena em grade com profundidade ilustrada, não um motor 3D nem um equivalente à complexidade do Roblox. O progresso de rodadas dos jogos não é persistido; o mundo de construção é persistido.
