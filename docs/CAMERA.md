# Caretas Divertidas — implementação nativa

## Implementado

- CameraX `ImageAnalysis`, câmera frontal/traseira, autorização solicitada somente ao tocar **Ligar câmera**, tratamento de recusa/câmera ausente/erro e botão de desligar.
- Detector facial **bundled** `com.google.mlkit:face-detection:16.1.7`. O modelo vem no APK; não é o artefato não empacotado do Google Play Services. Não há download de modelo em uso nem envio de fotos pelo aplicativo.
- Conversão YUV respeitando row stride/pixel stride, rotação do sensor, espelhamento frontal e transformação equivalente dos pontos faciais. Um único executor, uma única entrega pendente à UI (quadros adicionais são descartados se a interface estiver ocupada), timeout de 3 segundos para a detecção com liberação segura do bitmap somente após a tarefa encerrar, resolução solicitada de 480 × 640 (conversão limitada a 640 pixels no lado maior mesmo se o driver devolver uma resolução superior), `KEEP_ONLY_LATEST` e limitação aproximada de 12 quadros/s (8 no Modo Calminho).
- Detecção da face maior, olhos, nariz e boca. Orelhas e contorno são ancorados no retângulo facial; focinhos e óculos/olhos das máscaras seguem os pontos de nariz e olhos detectados, transformados para o referencial local da face. Todos seguem centro/escala e inclinação dos olhos. Suavização temporal usa o menor caminho para ângulos; uma mudança grande reinicia a pose.
- **12 filtros:** cachorrinho, gatinho, coelhinho, nariz gigante, olhos gigantes, bochechas, cabeça grandona, rosto comprido, arco-íris, óculos, desenho animado e monstrinho fofo.
- Cinco filtros alteram realmente os pixels da câmera: nariz, olhos, bochechas, cabeça e alongamento. Uma malha de 36 × 48 células usa deformações locais suaves em `Canvas.drawBitmapMesh`. Não são adesivos apresentados como deformação.
- Os outros sete filtros são acessórios/máscaras **2D** vetoriais originais. Eles não são apresentados como modelos 3D. O desenho animado é uma máscara de traços/cores sobre a face, não uma transformação neural fotorrealista.
- Troca por deslize ou chips grandes. Aviso amigável quando não há rosto. Fotografar mostra uma confirmação antes de salvar JPEG em **Imagens/MundinhoDoVivi** via MediaStore; não usa permissões amplas de arquivos. A imagem salva é uma cópia do mesmo bitmap filtrado mostrado na prévia.
- O fluxo CameraX é vinculado ao ciclo de vida; saída da tela libera o use case, detector e executor. Segundo plano remove o controlador e a prévia. Nenhum áudio/vídeo é gravado.

## Por que ML Kit nesta versão

Face Landmarker do MediaPipe exige distribuir e validar um modelo adicional e integrar sua malha tridimensional. Para esta implementação 2D, o detector facial nativo bundled do ML Kit é uma alternativa tecnicamente suficiente para localizar olhos/nariz, medir escala/inclinação e aplicar deformação de imagem baseada nesses pontos. Não se promete reconstrução facial 3D, oclusão correta em grandes giros, nem rastreamento de expressões/blendshapes. O código de geometria e renderização é independente do detector para permitir migração futura.

## Limites e validação pendente

O código foi escrito para processamento local; os resultados reais de câmera, permissão, rotação/espelhamento, foco, precisão das deformações, consistência dos acessórios, frames por segundo, temperatura e exportação ainda precisam ser testados no Galaxy A57. A resolução é uma solicitação: o hardware pode escolher outra compatível. Não há garantia de 60 FPS; o processamento da câmera é limitado propositalmente.

A malha usa CPU e deforma também pequenas regiões de fundo perto do contorno. Óculos/orelhas seguem pose 2D, sem oclusão 3D. Não há reação garantida a abrir a boca/piscar. Poses de perfil, pouca luz, rosto muito pequeno e múltiplas pessoas reduzem a confiabilidade. Quando a detecção falha, o aplicativo remove o filtro e informa ausência de rosto.

Os testes JVM de geometria cobrem limites da deformação, preservação de pixels fora da face, diferença entre acessórios/deformações, eixo local do alongamento e suavização de ângulos. Eles não comprovam comportamento de câmera ou qualidade visual em um telefone.

## Checklist físico obrigatório

- Conceder/negar permissão, negar permanentemente e habilitar novamente pelo Android.
- Alternar câmeras em retrato/paisagem; comparar lado esquerdo/direito com movimentos reais e foto salva.
- Testar os 12 efeitos com face parada, inclinada, próxima, distante e em perfil.
- Confirmar nariz/olhos reais ampliados nos cinco filtros de proporção, sem inversão de textura.
- Confirmar ausência de filtro quando não há rosto e nenhuma máscara fixa na tela.
- Salvar/descartar fotografia e verificar galeria, orientação e bitmap idêntico à prévia.
- Sair/voltar/minimizar/bloquear tela: câmera deve desligar/liberar hardware, sem captura em segundo plano.
- Modo avião desde antes de abrir; detector deve funcionar sem baixar recursos.
- Avaliar taxa de quadros, memória e calor por sessão de 10 minutos.
