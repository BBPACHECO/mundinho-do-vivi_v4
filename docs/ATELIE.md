# Meu Ateliê — implementação nativa

A entrada pública é `PaintingScreen(settings: ChildPreferences)`; a biblioteca adulta é `AdultArtLibraryScreen()` e deve ser exposta exclusivamente após a verificação dos responsáveis.

- 22 desenhos originais em oito categorias, traçados com Canvas Android; não há imagens baixadas ou personagens licenciados de terceiros.
- 48 cores na paleta superior, sempre fora do zoom; seletor RGB para cor personalizada.
- Preenchimento de região por flood fill, com máscara das linhas originais e tolerância de cor. Não atravessa linhas originais escuras fechadas. Desenhos importados com contornos abertos podem permitir vazamento; devem ter regiões fechadas para colorir com balde.
- Pincel livre com espessura, borracha que restaura o desenho original, três carimbos vetoriais, desfazer/refazer limitados a dez quadros (cerca de 16 MB para quadros raster de 640×640).
- Zoom de dois dedos de 1× a 4× e movimento com dois dedos; ação para voltar ao enquadramento. Um dedo desenha.
- Limpar exige confirmação e pode ser desfeito.
- Rascunhos privados por desenho, salvos após cada gesto, e recuperação da última seleção ao abrir. As criações salvas são guardadas localmente e exportadas explicitamente por MediaStore para `Pictures/MundinhoDoVivi`. Não exige acesso amplo ao armazenamento.
- Área adulta importa PNG/JPG ou a primeira página de PDF sem senha via seletor Android. Limite de 20 MB, recusa imagens acima de 100 MP, decodifica com amostragem e normaliza para 640×640. Originais externos não são alterados. Bibliotecas permitem excluir a cópia do app, preservando imagens já exportadas e arquivos originais.
- A atividade não tem flashes, movimentos automáticos ou áudio próprio; usa o tema compartilhado e funciona offline.

Cinco testes JVM de `PaintPixelsTest`: catálogo de 20+ desenhos/oito categorias/IDs únicos, regiões fechadas, toque em contorno/fora dos limites, recoloração de região e histórico limitado/ramificação de refazer. Testes reais de toque, pinch zoom, importação PDF e MediaStore ainda exigem aparelho/emulador; não foram declarados executados.
