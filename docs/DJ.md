# Meu DJ — implementação nativa

## Implementado

- Dois decks independentes com seleção entre 16 padrões originais: hip-hop, boom bap, funk instrumental, eletrônica, house, pop, disco, reggae, rock, trap, lo-fi, percussão, dance, ritmo infantil, samba instrumental e batida livre. São sequências musicais originais geradas com bateria amostrada e notas de baixo; não são gravações comerciais. Batida livre deixa a criação para os pads/sequenciador.
- Um único relógio de amostras mantém os decks e o sequenciador sincronizados entre 60–170 BPM. Crossfader de potência constante mistura A/B; alteração de BPM ocorre sem temporizadores de interface.
- Motor PCM16 mono 48 kHz com AudioTrack em modo streaming e pedido de baixa latência. Buffer mínimo do aparelho e blocos de 256 amostras; até 32 vozes sobrepostas, saturação suave e ganhos limitados. Não promete latência medida no Galaxy sem teste físico.
- 32 pads com arquivos CC0 incorporados: bateria, instrumentos, gravações de objetos/animal e sons eletrônicos. Os sons eletrônicos estão identificados como tais. Veja CREDITOS_AUDIO.md e o manifesto embarcado assets/dj/credits.json.
- Sequenciador funcional de 16 passos, com seleção do pad tocado. Este marco oferece **uma pista de pad**; mudar o pad muda o som de todos os passos. Os decks continuam independentes.
- Gravação da saída digital do mixer em WAV, sem microfone ou qualquer permissão de gravação. Arquivo incremental privado limitado a três minutos; a biblioteca tem limite de 100 MB. Ao parar, sair da tela ou perder foco, o cabeçalho WAV é finalizado.
- Biblioteca local, reprodução, parar e exportação explícita via MediaStore para Música/MundinhoDoVivi. Exclusão privada com confirmação disponível em AdultRecordingsScreen, que deve ser exposta somente pela navegação protegida dos responsáveis. Arquivos exportados não são apagados pelo gerenciador privado.
- ON_STOP pausa os decks, sequência e reprodução, finaliza a gravação e libera foco. Sair da composição libera AudioTrack. O app não exige microfone.
- Volume máximo da mesa respeita volume geral × efeitos × atenuação do Modo Calminho. Redução de estímulos desliga o medidor e a posição animada dos discos. O volume físico do Android continua soberano.

## Limitações concretas frente ao pedido completo

- O animal incluído é **corvo**, uma gravação real. Cachorro, gato, pato, vaca, cavalo, leão e macaco **não estão incluídos**. Risada, espirro, soluço, aplausos, panela, colher, água e relógio também não foram obtidos para este marco. Não foram substituídos por imitações sintetizadas nem apresentados como disponíveis.
- A gravação de animais/objetos via Wikimedia permanece bloqueada pela política de rede deste ambiente até revisão e aplicação dos domínios commons.wikimedia.org/upload.wikimedia.org. Os 32 arquivos existentes foram obtidos legitimamente da coleção CC0 do Sonic Pi por raw.githubusercontent.com.
- Os ritmos são arranjos nativos curtos de bateria/baixo; qualidade artística e volume no alto-falante precisam de avaliação familiar no aparelho. Não são uma biblioteca comercial masterizada.
- A biblioteca conserva músicas; o estado de seleção dos decks e a edição do sequenciador ainda não são projetos musicais persistentes.
- A gravação armazena a saída com o volume aplicado. Ao reproduzir dentro do app, o limite de volume é aplicado novamente; isso pode deixar a reprodução interna mais baixa que a cópia ouvida em outro player.
- Multitoque, latência, rota Bluetooth, chamadas/foco de áudio e estabilidade térmica precisam de testes no aparelho real. O simulador/JVM não comprova saída sonora.

## APIs de integração

```kotlin
DjScreen(settings: ChildPreferences)
AdultRecordingsScreen(settings: ChildPreferences = ChildPreferences())
```

Nenhuma dependência Gradle nova é necessária. O único motor de áudio do DJ é nativo. Os arquivos offline estão em app/src/main/assets/dj; a interface não baixa nada.

## Testes

PcmMixerTest cobre relógio musical, polifonia/limites PCM, crossfader/mute, disparos do sequenciador/pausa, WAV finalizado e round-trip, limite de duração/rejeição de corrupção e integridade estrutural do catálogo. A execução e o resultado devem ser confirmados pelo build integrado; não foram presumidos neste documento.
