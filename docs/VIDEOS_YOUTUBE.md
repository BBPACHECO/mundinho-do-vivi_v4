# Meus Vídeos — YouTube dentro do aplicativo

Meus Vídeos é uma biblioteca de até 100 vídeos escolhidos pelos responsáveis. Ela fica separada do Miniestúdio, que continua editando arquivos locais. A biblioteca salva somente os IDs e títulos no aparelho; não baixa os vídeos.

## Escolher os vídeos

1. Abra **Responsáveis → Escolher e gerenciar vídeos**, ou peça ajuda aos responsáveis na tela **Meus Vídeos**. A verificação adulta protege a gestão.
2. Pesquise no YouTube pela área de busca. Os resultados são apresentados pelo site oficial dentro de um WebView, usando internet.
3. Abra o vídeo desejado e use a opção de adicioná-lo à biblioteca, conferindo seu título. Também é possível colar um link do YouTube e informar um título.
4. Remova da biblioteca os vídeos que não devem mais aparecer na lista infantil.

A pesquisa online é feita pelos responsáveis. O campo de pesquisa da tela infantil filtra os títulos já aprovados, sem fazer uma busca livre no YouTube. Sem uma lista cadastrada, a tela explica como pedir ajuda para escolher os primeiros vídeos.

## Reprodução e dados

A criança escolhe um item aprovado para abrir o player oficial incorporado. Pesquisa online e reprodução exigem internet e um componente Android System WebView funcional e atualizado. A nova permissão `INTERNET` atende essa seção; pintura, DJ, aprendizagem, jogos, câmera e edição local continuam disponíveis sem rede.

A integração usa o YouTube comum, não um player do YouTube Kids. Ela não herda os filtros ou a seleção de conteúdo do Kids. O serviço pode mostrar anúncios, recomendações, links e avisos de cookies e aplicar restrições de região, idade ou incorporação. A lista aprovada organiza a escolha inicial, mas não transforma o player em uma experiência totalmente fechada. Acompanhe o uso da criança.

Ao abrir a pesquisa ou o player, o YouTube recebe a pesquisa e os dados necessários às páginas e à reprodução, conforme suas políticas. O aplicativo não envia ao YouTube as fotos, pinturas, gravações ou vídeos locais. Não há extração de streams, download de vídeos, backend próprio nem chave da YouTube Data API. O acesso à gestão é bloqueado novamente quando o app vai para segundo plano.

## Verificações pendentes no aparelho

Os testes locais de parsing de links, armazenamento, filtro e navegação não executam a pesquisa real nem comprovam áudio/vídeo no WebView. Antes do uso familiar, confira em Android com internet:

- Busca, abertura de resultado, aprovação, cadastro por link e remoção.
- Persistência da lista depois de fechar e reabrir o app e bloqueio da gestão após voltar do segundo plano.
- Reprodução de um vídeo público que permita incorporação, pausa ao sair ou minimizar e ausência de áudio depois de fechar o player.
- Comportamento sem rede e diante de vídeo removido, privado, bloqueado ou sem permissão de incorporação.
- Anúncios, recomendações, links e avisos que o serviço apresentar, sem presumir filtros do YouTube Kids.

Referências do serviço: [player incorporado](https://developers.google.com/youtube/iframe_api_reference), [parâmetros do player](https://developers.google.com/youtube/player_parameters), [privacidade do Google](https://policies.google.com/privacy) e [termos do YouTube](https://www.youtube.com/t/terms).
