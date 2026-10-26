# Estado — versão 0.5.0 para teste familiar

As cinco rotas agora abrem atividades nativas reais; o placeholder da etapa1 foi removido. Identidade visual, configurações e persistência foram mantidas e integradas.

## Implementado

Ateliê:22originais,48cores, preencher/pincel/borracha, espessuras, RGB,3carimbos, histórico10,zoom,rascunhos,galeria,importação adulta. Descobrir:26×10entradas e0–30, vozlocal, contagem por ordem de toque e agrupamento sensorial. DJ:2decks,16ritmos,32sons CC0, mixer, sequenciador16passos deuma pista, gravação digital/reprodução/exportação/gestão. Jogos:8minijogos cominteração,pausa e sandbox persistente. Câmera:12filtros,7acessórios2D rastreados+5deformações de imagem,modelooffline, permissão explícita, foto e MediaStore.

## O que ainda não cumpre integralmente o pedido original

- A biblioteca de animais/objetos do DJ está incompleta: existe corvo real; cachorro, gato, pato, vaca, cavalo, leão, passarinho e macaco, risada/panela e vários objetos ainda precisam de arquivos licenciados adequados. Não foram substituídos por síntese. Fontes Wikimedia foram bloqueadas pela rede; os domínios estão no rascunho aguardando aplicação nas configurações.
- Os16ritmos são padrões originais curtos gerados pelo motor musical, não faixas de estúdio produzidas por músicos. O sequenciador trabalha com um pad por vez. Verificar timbres, sincronismo e resposta no aparelho.
- As260entradas educativas têm arte vetorial procedural por tipo de objeto e algumas representações de categoria/nomes com legenda, não260ilustrações individuais feitas à mão. Revisar adequação do vocabulário e pronúncia com o responsável. A voz natural gravada humana não foi produzida; usa TTSpt-BRoffline como alternativa permitida.
- O sandbox é pequeno/emgrade2D e o puzzle usa peças retangulares com encaixe magnético. Não há reconstrução de um mundo3D complexo, peças irregulares nem suporte TalkBack individual para cada objeto dos Canvas de jogos.
- A câmera acompanha pontos2D/escala/inclinação, sem garantir oclusão3D, expressões ou estabilidade em todos os ângulos. Deformações são reais de pixels; precisam de avaliação visual no Galaxy. Quadros limitados a640px para conter custo e memória.
- Acessibilidade visual/gestual completa, desempenho60FPS/latência/temperatura, permissões e fluxos MediaStore/SAF devem ser testados no Galaxy A57. Testes de software não equivalem a esses testes físicos.
- Falta validar e assinar uma versão de produção, revisar termos/licenças transitivas e preparar a listagem/política pública da Play. A publicação não foi realizada.

## Próxima ação

Gerar/instalar a versão de teste, executar o checklist físico e registrar falhas. Completar os áudios faltantes quando o acesso às fontes licenciadas estiver disponível. Corrigir resultados e só então preparar GooglePlay. Resultados reais do ambiente estão em VALIDACAO.md.
