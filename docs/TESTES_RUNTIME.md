# Testes nativos de interface em JVM

Testes em `app/src/test/java/com/mundodedescobertas/app/integration/` usam Robolectric 4.14.1, Android API 35 e renderização nativa (`GraphicsMode.NATIVE`). Executam código Compose Android sem navegador, emulador ou telefone. Os arquivos de teste por si só **não comprovam execução**: conferir o relatório do build atual.

## Cobertura preparada

- Os cinco botões ilustrados respondem com o destino correto; nome personalizado aparece.
- Acesso dos responsáveis e cumprimento do mascote não acionam os destinos infantis.
- A verificação adulta rejeita resposta errada e libera a correta usando a interface real.
- A Activity de produção, o ViewModel e DataStore iniciam; as cinco rotas abrem e o botão Voltar retorna ao início.
- As 26 letras e os 31 números estão disponíveis; tocar em uma palavra pede a narração correta e permite repeti-la.
- No modo reduzido, cinco objetos continuam sendo cinco ao paginar e tocar novamente no mesmo objeto não duplica a contagem. Zero não inventa objetos.
- Bolhas respondem a toques de verdade; pausar, continuar e voltar funcionam sem trocar o motor por um mock.
- Os oito jogos abrem Canvas, botão de começar e retornam à biblioteca.
- Construir um objeto por toque altera o mundo, pausar bloqueia novos toques e reabrir restaura o arquivo salvo.
- A câmera permanece desligada e sem fotografar/trocar câmera antes de uma ação explícita de autorização.
- Uma pintura modificada é salva em PNG privado e restaurada em nova instância do repositório.
- Um PNG importado é normalizado sem perder a proporção e entra na biblioteca; entrada inválida não deixa um desenho quebrado nem arquivo temporário.

## Executar

```sh
source scripts/cloud-env.sh
./gradlew :app:testDebugUnitTest --no-daemon --max-workers=4
```

Requisitos de configuração: `unitTests.includeAndroidResources = true`; dependências de teste `org.robolectric:robolectric:4.14.1`, `androidx.compose.ui:ui-test-junit4` usando o BOM principal e `androidx.test:core-ktx:1.6.1`. O resolver do Robolectric precisa baixar o Android instrumentado de Maven Central na primeira execução; usar `https://repo.maven.apache.org/maven2`, proxy injetado e truststore público do sistema. Não desativar TLS.

## Limites

O teste de rota da câmera valida apenas abrir o destino e voltar com permissão não concedida. Robolectric não comprova que CameraX detecta um rosto, que filtros acompanham movimento ou que o Samsung reproduz áudio com baixa latência. Testes de toque de uma tela não equivalem a estabilidade, FPS ou conforto sensorial no Galaxy A57. A instalação, permissões reais, sistema TTS, câmera, galeria e desempenho devem ser verificados em aparelho físico.

O primeiro conjunto integrado completou 49 testes, incluindo estes 14 testes runtime, sem falhas, erros ou ignorados. O agente integrador mantém o relatório final do build entregue.

`ActivityNavigationRuntimeTest` também pode gravar seis capturas PNG de produção em `app/build/reports/ui/` (home, atelier, dj, discover, games, camera), numa configuração Android simulada de 393 × 852 dp. A captura desenha a árvore nativa real de `Activity.window.decorView` em um Bitmap no thread de UI após idle; o handshake PixelCopy/forceRedraw não é implementado pelo Robolectric usado aqui. Não substitui conteúdo por mocks nem pula falhas de desenho. São renderizações de teste nativo, não capturas de um Galaxy físico. A gravação das seis capturas foi confirmada pela execução final com49testes aprovados. A revisão visual levou à correção da sobreposição da prévia da câmera e as capturas finais foram regeneradas. Não substituir testes que falharem por asserts triviais para declarar sucesso.
