# Descobrir — implementação nativa local

## Disponível

- `DiscoverScreen(settings, onSpeak)` usa apenas Jetpack Compose e Canvas. Sem HTML/WebView, imagens remotas ou dados enviados.
- Alfabeto com 26 letras, nome falado em português brasileiro e dez entradas por letra: **260 palavras/nomes diferentes**, cada uma com frase curta autoral. A associação considera a inicial sem remover os acentos do texto visível: Árvore → A, Ímã → I, Ônibus → O.
- Cada entrada tem um desenho vetorial procedural, abre uma ficha e permite repetir a narração por ação explícita. As figuras são desenhadas para o projeto a partir de formas originais e famílias reutilizáveis; não são fotos nem 260 arquivos desenhados manualmente.
- K/W/Y usam palavras de empréstimo e nomes de personagens fictícios, indicados na interface. Nomes não são apresentados como vocabulário comum nem como retratos de pessoas reais.
- Números de 0 a 30 com nomes escritos, fala acionada pelo toque, representação por estrelas, marcação de objetos contados e recomeço sem punição.
- Quantidades grandes são divididas em grupos. O total e o número do grupo ficam explícitos; Modo Calminho altera o tamanho dos grupos, **nunca o valor matemático**.
- Brincadeira guiada de associação entre contagem e três alternativas. Sem cronômetro, perda de pontos ou limite de tentativas. Dificuldade limita os números sorteados a 0–5, 0–10 ou 0–30.
- As preferências do app controlam a suavidade do feedback e a narração via callback do mecanismo TTS local. Não há áudio automático ao abrir telas.

## Validação

`LearningCatalogTest` verifica 26 × 10 entradas distintas, iniciais com diacríticos, nomes 0–30 e contagem sem duplicação/índices inválidos. A execução e integração são coordenadas pelo agente principal, que mantém o relatório geral do projeto.

Tags de interface para testes: `discover_screen`, `letter_A` até `letter_Z`, `word_Abelha` (e demais palavras literais), `number_0` até `number_30`, `count_object_0` etc., e `count_progress`.

## Limites que precisam de revisão no aparelho

A naturalidade e pronúncia dependem da voz pt-BR offline instalada no Android. Não foram incluídas gravações humanas e a tela não promete voz natural uniforme. O responsável deve revisar especialmente nomes e empréstimos de K/W/Y.

As ilustrações têm estética simples e compartilham famílias. São associações visuais estilizadas, não um guia de identificação biológica. Exemplos de conceitos amplos (instrumento/triângulo, utensílio/colher, número/sete, kit/material de pintura, uniformes, canteiros e nomes próprios) recebem legenda explicando o desenho específico. O catálogo e a distinção visual de animais/plantas menos familiares precisam de revisão editorial e teste com a criança antes de alegar acabamento educacional final.

A atividade de contagem oferece ajuda e indica o total; ela é uma associação guiada, sem esconder a resposta para testar ou punir a criança. Não é um instrumento de avaliação pedagógica ou clínica.
