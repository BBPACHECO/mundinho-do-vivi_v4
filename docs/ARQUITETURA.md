# Arquitetura nativa — 0.5.0

A Application mantém uma instância do DataStore. O ViewModel expõe preferências normalizadas por StateFlow e transações atômicas; as telas coletam de acordo com o ciclo de vida. Preferências individuais permanecem separadas dos valores efetivos do Modo Calminho.

NavigationCompose organiza início/cincoáreas/responsáveis/bibliotecas/créditos. A área adulta usa uma verificação simples, sem pretensão de autenticação forte. O acesso é revogado em ON_STOP e na recriação. Uma janela modal bloqueia as telas adultas enquanto seus launchers do seletor de documentos permanecem registrados para receber resultados.

Pintura: catálogo vetorial original rasterizado, algoritmo de preenchimento puro, histórico limitado, ViewModel, PNGs privados com troca de arquivo temporário, fila de gravação serial independente da saída da tela. Importações passam por limites de bytes/resolução e normalização. Exportação explícita usa MediaStore.

Aprendizagem: catálogo local tipado de260palavras, ilustrações procedurais e estados de seleção/contagem. A quantidade matemática é independente da paginação sensorial; o ordinal falado segue a ordem dos toques, sem duplicar.

DJ: mixer PCM48kHz em thread de áudio, fila de comandos descartada ao encerrar e polifonia limitada, StateFlow de status, dois decks sincronizados pelo mesmo relógio musical e sequenciador deuma pista. WAV escreve diretamente a saída digital, semmicrofone. Foco deáudio/ON_STOP interrompem e finalizam gravações; falhas fecham o motor e desabilitam controles. Os32samplesCC0 e metadados acompanham oAPK.

Jogos: simulação separada do Canvas, delta temporal limitado, pausa por ciclo de vida, cenários e objetos originais. Sandbox serializa em AtomicFile. Configurações influenciam ritmo/quantidade; as atividades de contar mantêm alvos corretos.

Câmera: CameraXImageAnalysis e MLKitbundled. QuadrosYUVstride-aware limitadosa640px, detecção local de landmarks, rotação/espelhamento/suavização, acessórios no referencial do rosto e malhaBitmapMesh para5deformações. Há um executor e uma entrega pendente de quadro; o pipeline é descartado ao sair/minimizar. Fotos salvam o mesmo bitmap filtrado após confirmação.

Narração: somente vozespt-BR que não exijam rede; para ao sair/parar/navegar e encerra o motor com aActivity. Sem vozlocal o app não escolhe um fallback online.

Não há permissão deInternet,microfone,localização ou arquivos amplos. Backups e transferência automática de dados são excluídos. Nenhum dado facial é persistido como identificação biométrica. A ausência de rede foi verificada no manifesto empacotado; estabilidade/desempenho e experiências sensoriais exigem o teste físico.
