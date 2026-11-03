# Arquitetura nativa — 0.5.0

A Application mantém uma instância do DataStore. O ViewModel expõe preferências normalizadas por StateFlow e transações atômicas; as telas coletam de acordo com o ciclo de vida. Preferências individuais permanecem separadas dos valores efetivos do Modo Calminho.

NavigationCompose organiza início/seis áreas/responsáveis/bibliotecas/créditos. A área adulta usa uma verificação simples, sem pretensão de autenticação forte. O acesso é revogado em ON_STOP e na recriação. Uma janela modal bloqueia as telas adultas enquanto seus launchers do seletor de documentos permanecem registrados para receber resultados. A gestão dos vídeos online só é composta depois da liberação adulta, para não carregar o navegador por trás desse bloqueio.

Pintura: catálogo vetorial original rasterizado, algoritmo de preenchimento puro, histórico limitado, ViewModel, PNGs privados com troca de arquivo temporário, fila de gravação serial independente da saída da tela. Importações passam por limites de bytes/resolução e normalização. Exportação explícita usa MediaStore.

Aprendizagem: catálogo local tipado de260palavras, ilustrações procedurais e estados de seleção/contagem. A quantidade matemática é independente da paginação sensorial; o ordinal falado segue a ordem dos toques, sem duplicar.

DJ: mixer PCM48kHz em thread de áudio, fila de comandos descartada ao encerrar e polifonia limitada, StateFlow de status, dois decks sincronizados pelo mesmo relógio musical e sequenciador deuma pista. WAV escreve diretamente a saída digital, semmicrofone. Foco deáudio/ON_STOP interrompem e finalizam gravações; falhas fecham o motor e desabilitam controles. Os32samplesCC0 e metadados acompanham oAPK.

Jogos: simulação separada do Canvas, delta temporal limitado, pausa por ciclo de vida, cenários e objetos originais. Sandbox serializa em AtomicFile. Configurações influenciam ritmo/quantidade; as atividades de contar mantêm alvos corretos.

Câmera: CameraXImageAnalysis e MLKitbundled. QuadrosYUVstride-aware limitadosa640px, detecção local de landmarks, rotação/espelhamento/suavização, acessórios no referencial do rosto e malhaBitmapMesh para5deformações. Há um executor e uma entrega pendente de quadro; o pipeline é descartado ao sair/minimizar. Fotos salvam o mesmo bitmap filtrado após confirmação.

Narração: somente vozespt-BR que não exijam rede; para ao sair/parar/navegar e encerra o motor com aActivity. Sem vozlocal o app não escolhe um fallback online.

Meus Vídeos: biblioteca local de IDs e títulos aprovados, com pesquisa nativa nessa lista. A gestão adulta pesquisa no site oficial do YouTube em WebView e cadastra o vídeo escolhido; também aceita um link. O player incorporado oficial reproduz o vídeo online, sem extrair streams nem usar Media3 para baixar conteúdo do YouTube. Não há backend nem chave da YouTube Data API. A busca no site e o player dependem do serviço externo e não oferecem os filtros do YouTube Kids. Veja [VIDEOS_YOUTUBE](VIDEOS_YOUTUBE.md).

O manifesto permite internet para a seção YouTube e microfone para a voz opcional da câmera do Miniestúdio. Não há localização ou acesso amplo a arquivos. As demais atividades continuam locais; fotos, pinturas, gravações e vídeos locais não são enviados ao YouTube pelo app. Backups e transferência automática de dados são excluídos. Nenhum dado facial é persistido como identificação biométrica. O YouTube recebe os dados de suas páginas e player e pode exibir anúncios, recomendações e links. Estabilidade, reprodução online, desempenho e experiências sensoriais exigem teste no aparelho.
