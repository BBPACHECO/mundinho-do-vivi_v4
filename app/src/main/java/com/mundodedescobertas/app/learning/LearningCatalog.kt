package com.mundodedescobertas.app.learning

import java.text.Normalizer
import java.util.Locale

/** Conteúdo autoral e local; nomes próprios são indicados para as letras menos comuns. */
data class LearningWord(val letter: Char, val word: String, val sentence: String, val art: String, val properName: Boolean = false) {
    val narration: String get() = "${LearningCatalog.letterNames.getValue(letter)} de $word. $sentence"
}

object LearningCatalog {
    val letterNames = ('A'..'Z').zip(listOf("A", "Bê", "Cê", "Dê", "É", "Efe", "Gê", "Agá", "I", "Jota", "Cá", "Ele", "Eme", "Ene", "Ó", "Pê", "Quê", "Erre", "Esse", "Tê", "U", "Vê", "Dáblio", "Xis", "Ípsilon", "Zê")).toMap()
    val numberNames = listOf("zero", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez", "onze", "doze", "treze", "catorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove", "vinte", "vinte e um", "vinte e dois", "vinte e três", "vinte e quatro", "vinte e cinco", "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove", "trinta")
    private fun group(letter: Char, rows: String) = rows.trimIndent().lineSequence().filter(String::isNotBlank).map { row ->
        val p = row.split('|')
        LearningWord(letter, p[0], p[1], p[2], p.getOrNull(3) == "nome")
    }.toList()
    val words: List<LearningWord> = listOf(
        group('A', """
            Abelha|A abelha visita as flores.|bee
            Abacaxi|O abacaxi tem uma coroa de folhas.|pineapple
            Abóbora|A abóbora cresce na horta.|pumpkin
            Avião|O avião voa pelo céu.|plane
            Árvore|A árvore tem folhas e oferece sombra.|tree
            Anel|O anel cabe no dedo.|ring
            Aranha|A aranha tem oito patas.|spider
            Abacate|O abacate tem um caroço grande.|avocado
            Arco-íris|O arco-íris tem muitas cores.|rainbow
            Aquário|O peixe nada no aquário.|aquarium
        """),
        group('B', """
            Baleia|A baleia vive no mar.|whale
            Banana|A banana tem uma casca amarela.|banana
            Bola|A bola pode rolar.|ball
            Borboleta|A borboleta abre suas asas.|butterfly
            Bicicleta|A bicicleta tem duas rodas.|bicycle
            Barco|O barco navega na água.|boat
            Bolo|O bolo está pronto para a festa.|cake
            Bota|A bota protege o pé.|boot
            Balão|O balão está preso por uma cordinha.|balloon
            Brócolis|O brócolis parece uma pequena árvore.|broccoli
        """),
        group('C', """
            Cachorro|O cachorro tem um focinho e uma cauda.|dog
            Casa|A casa tem porta e janelas.|house
            Cavalo|O cavalo tem uma linda crina.|horse
            Coelho|O coelho tem orelhas compridas.|rabbit
            Cenoura|A cenoura cresce debaixo da terra.|carrot
            Coração|Este coração é um desenho de carinho.|heart
            Caracol|O caracol carrega sua concha.|snail
            Castelo|O castelo tem torres altas.|castle
            Caminhão|O caminhão leva uma carga.|truck
            Coruja|A coruja tem olhos grandes.|owl
        """),
        group('D', """
            Dado|O dado tem pontinhos em suas faces.|dice
            Dinossauro|O dinossauro viveu há muito tempo.|dinosaur
            Dente|A escova ajuda a cuidar do dente.|tooth
            Diamante|O diamante é uma pedra brilhante.|diamond
            Desenho|Este desenho tem muitas cores.|drawing
            Dromedário|O dromedário tem uma corcova.|camel
            Dominó|Vamos combinar as peças de dominó.|domino
            Doce|Este doce pode fazer parte de uma festa.|candy
            Disco|O disco guarda músicas.|disc
            Despertador|O despertador ajuda a lembrar a hora.|clock
        """),
        group('E', """
            Elefante|O elefante tem uma tromba comprida.|elephant
            Estrela|A estrela aparece no céu à noite.|star
            Escova|A escova ajuda a limpar os dentes.|toothbrush
            Espelho|Podemos ver nosso rosto no espelho.|mirror
            Escada|A escada tem vários degraus.|ladder
            Estojo|O estojo guarda lápis e canetas.|pencilcase
            Envelope|Uma carta cabe no envelope.|envelope
            Esquilo|O esquilo tem uma cauda fofinha.|squirrel
            Espiga|A espiga tem muitos grãos de milho.|corn
            Ervilha|A ervilha fica dentro de uma vagem.|pea
        """),
        group('F', """
            Flor|A flor tem pétalas delicadas.|flower
            Foca|A foca nada e descansa perto da água.|seal
            Foguete|O foguete viaja pelo espaço.|rocket
            Formiga|A formiga tem seis patas.|ant
            Fada|A fada é uma personagem de histórias.|fairy
            Farol|A luz do farol orienta os barcos.|lighthouse
            Flauta|A flauta produz música com o sopro.|flute
            Folha|A folha faz parte da planta.|leaf
            Framboesa|A framboesa é uma frutinha vermelha.|raspberry
            Fivela|A fivela ajuda a prender o cinto.|buckle
        """),
        group('G', """
            Gato|O gato tem bigodes e patinhas.|cat
            Girafa|A girafa tem um pescoço comprido.|giraffe
            Galinha|A galinha tem penas e um bico.|hen
            Galo|O galo tem uma crista vermelha.|rooster
            Golfinho|O golfinho vive na água e respira ar.|dolphin
            Guitarra|A guitarra tem cordas para tocar.|guitar
            Gota|Uma pequena gota pode cair da nuvem.|drop
            Guarda-chuva|O guarda-chuva ajuda a proteger da chuva.|umbrella
            Goiaba|A goiaba tem muitas sementinhas.|guava
            Girassol|O girassol tem pétalas amarelas.|sunflower
        """),
        group('H', """
            Helicóptero|A hélice ajuda o helicóptero a voar.|helicopter
            Hipopótamo|O hipopótamo é um animal grande.|hippo
            Hambúrguer|O hambúrguer cabe dentro do pão.|burger
            Harpa|A harpa tem cordas de vários tamanhos.|harp
            Hortelã|A hortelã é uma planta cheirosa.|mint
            Horta|Na horta crescem verduras e legumes.|garden
            Hospital|No hospital as pessoas recebem cuidados.|hospital
            Hélice|A hélice gira para movimentar o ar.|propeller
            Hidrante|O hidrante fornece água para os bombeiros.|hydrant
            Hamster|O hamster é um pequeno roedor.|hamster
        """),
        group('I', """
            Ilha|A ilha é cercada de água.|island
            Iglu|O iglu pode ser construído com blocos de neve.|igloo
            Ioiô|O ioiô sobe e desce pela cordinha.|yoyo
            Inhame|O inhame é um alimento que cresce na terra.|yam
            Inseto|O inseto tem seis patas.|insect
            Ímã|O ímã pode atrair alguns metais.|magnet
            Iguana|A iguana é um réptil com uma longa cauda.|iguana
            Ipê|O ipê pode ficar cheio de flores.|floweringtree
            Igreja|A igreja é um lugar de encontro e oração.|church
            Instrumento|Este triângulo é um instrumento musical.|triangle
        """),
        group('J', """
            Jacaré|O jacaré tem uma cauda comprida.|alligator
            Joaninha|A joaninha tem pintinhas nas asas.|ladybug
            Janela|Pela janela podemos olhar o lado de fora.|window
            Jarra|A jarra pode guardar água.|jug
            Jaca|A jaca é uma fruta grande de casca áspera.|jackfruit
            Jipe|O jipe tem rodas para caminhos diferentes.|jeep
            Jujuba|A jujuba é um doce pequeno e colorido.|jellybean
            Jardim|No jardim podemos cuidar das flores.|garden
            Jangada|A jangada flutua sobre a água.|raft
            Jabuti|O jabuti tem um casco e vive em terra.|tortoise
        """),
        group('K', """
            Kiwi|O kiwi tem a casca marrom e a polpa verde.|kiwi
            Ketchup|O ketchup é um molho feito com tomate.|ketchup
            Kart|O kart é um carrinho usado em uma pista.|kart
            Karatê|O karatê é uma prática esportiva com movimentos do corpo.|karate
            Karaokê|No karaokê podemos cantar uma música.|microphone
            Kit|Este kit reúne materiais para desenhar.|artkit
            Kitesurfe|No kitesurfe uma pipa ajuda a deslizar pela água.|kitesurf
            Kátia|Kátia é o nome desta personagem.|person|nome
            Kaique|Kaique é o nome deste personagem.|person|nome
            Kevin|Kevin é o nome deste personagem.|person|nome
        """),
        group('L', """
            Leão|O leão pode ter uma juba bem grande.|lion
            Lua|A Lua acompanha a Terra no espaço.|moon
            Lápis|O lápis faz traços no papel.|pencil
            Livro|O livro guarda histórias e descobertas.|book
            Laranja|A laranja tem gominhos por dentro.|orange
            Lagarta|A lagarta pode se transformar em borboleta.|caterpillar
            Lhama|A lhama tem um pescoço comprido e pelos fofos.|llama
            Lanterna|A lanterna ilumina o caminho.|flashlight
            Laço|Este laço tem duas voltinhas.|bow
            Limão|O limão é uma fruta de sabor azedo.|lemon
        """),
        group('M', """
            Macaco|O macaco pode se mover pelos galhos.|monkey
            Maçã|A maçã tem uma casca fina.|apple
            Morango|O morango tem pequenas sementes por fora.|strawberry
            Melancia|A melancia tem bastante água.|watermelon
            Mochila|A mochila pode carregar nossos materiais.|backpack
            Martelo|O martelo é uma ferramenta usada por adultos.|hammer
            Milho|Os grãos de milho ficam juntos na espiga.|corn
            Montanha|A montanha é uma grande elevação do terreno.|mountain
            Moto|A moto tem duas rodas e um motor.|motorcycle
            Mão|A mão ajuda a tocar e segurar objetos.|hand
        """),
        group('N', """
            Navio|O navio navega levando pessoas ou cargas.|ship
            Nuvem|A nuvem pode trazer chuva.|cloud
            Ninho|Muitos pássaros cuidam dos filhotes no ninho.|nest
            Nariz|O nariz ajuda a sentir os cheiros.|nose
            Noz|A noz fica protegida por uma casca.|walnut
            Nabo|O nabo é um alimento que cresce na terra.|turnip
            Nota|Esta nota faz parte da música.|musicnote
            Neve|A neve é formada por pequenos cristais de gelo.|snowflake
            Novelo|O novelo enrola um fio comprido.|yarn
            Número|Os números ajudam a contar.|number
        """),
        group('O', """
            Ovelha|A ovelha tem lã macia.|sheep
            Ovo|O ovo tem uma casca que o protege.|egg
            Olho|O olho ajuda a enxergar.|eye
            Ouriço|O ouriço tem espinhos nas costas.|hedgehog
            Ônibus|O ônibus transporta várias pessoas.|bus
            Óculos|Alguns óculos ajudam a enxergar melhor.|glasses
            Orelha|A orelha faz parte do nosso corpo.|ear
            Ostra|A ostra vive dentro de duas conchas.|oyster
            Onça|A onça tem manchas na pelagem.|jaguar
            Orquídea|A orquídea é uma flor de muitas formas.|orchid
        """),
        group('P', """
            Pato|O pato tem pés que ajudam a nadar.|duck
            Peixe|O peixe usa as nadadeiras para nadar.|fish
            Pinguim|O pinguim é uma ave que nada muito bem.|penguin
            Porco|O porco tem um focinho arredondado.|pig
            Pera|A pera tem a base mais larga.|pear
            Pipa|A pipa pode voar com o vento em um lugar seguro.|kite
            Panela|Na panela os adultos preparam alimentos.|pot
            Pão|O pão pode fazer parte do lanche.|bread
            Pipoca|O milho pode se transformar em pipoca.|popcorn
            Piano|O piano tem teclas claras e escuras.|piano
        """),
        group('Q', """
            Queijo|O queijo pode ser cortado em pedaços.|cheese
            Quadrado|O quadrado tem quatro lados iguais.|square
            Quati|O quati tem uma cauda longa e listrada.|coati
            Quebra-cabeça|As peças do quebra-cabeça formam uma figura.|puzzle
            Quadro|O quadro pode enfeitar a parede.|frame
            Quarto|O quarto pode ser um cantinho de descanso.|bed
            Quiabo|O quiabo é um alimento verde e comprido.|okra
            Quindim|O quindim é um doce amarelo.|quindim
            Quatro|Aqui temos quatro bolinhas para contar.|four
            Quintal|No quintal podemos cuidar das plantas.|garden
        """),
        group('R', """
            Raposa|A raposa tem uma cauda fofinha.|fox
            Rato|O rato tem orelhas e bigodes.|mouse
            Robô|O robô é uma máquina criada por pessoas.|robot
            Relógio|O relógio mostra as horas.|clock
            Rosa|A rosa é uma flor com muitas pétalas.|rose
            Rabanete|O rabanete tem uma raiz arredondada.|radish
            Rádio|O rádio pode tocar músicas.|radio
            Raquete|A raquete ajuda a rebater a bola.|racket
            Rio|A água do rio segue por um caminho.|river
            Roda|A roda gira para ajudar a mover objetos.|wheel
        """),
        group('S', """
            Sapo|O sapo pode dar pequenos saltos.|frog
            Sol|O Sol ilumina e aquece a Terra.|sun
            Sorvete|O sorvete é um alimento gelado.|icecream
            Semente|Uma semente pode dar origem a uma planta.|seed
            Sino|O sino pode produzir um som.|bell
            Sapato|O sapato protege o pé.|shoe
            Sanduíche|O sanduíche tem um recheio entre os pães.|sandwich
            Serpente|A serpente se move sem patas.|snake
            Sereia|A sereia é uma personagem de histórias.|mermaid
            Saxofone|O saxofone é um instrumento de sopro.|saxophone
        """),
        group('T', """
            Tartaruga|A tartaruga tem o corpo protegido pelo casco.|turtle
            Tigre|O tigre tem listras na pelagem.|tiger
            Trem|O trem viaja sobre trilhos.|train
            Tambor|O tambor produz som quando é tocado.|drum
            Tomate|O tomate pode ser usado em saladas.|tomato
            Tesoura|A tesoura deve ser usada com cuidado e ajuda.|scissors
            Tubarão|O tubarão é um peixe que vive na água.|shark
            Tênis|O tênis protege o pé nas brincadeiras.|sneaker
            Tucano|O tucano tem um bico grande e colorido.|toucan
            Trator|O trator ajuda no trabalho do campo.|tractor
        """),
        group('U', """
            Uva|As uvas crescem juntas em um cacho.|grapes
            Urso|O urso tem patas grandes e pelos.|bear
            Unicórnio|O unicórnio é um animal das histórias.|unicorn
            Urubu|O urubu ajuda a manter a natureza limpa.|vulture
            Utensílio|Esta colher é um utensílio de cozinha.|spoon
            Uniforme|O uniforme é uma roupa usada por um grupo.|shirt
            Urna|A urna é uma caixa para guardar papéis.|ballotbox
            Ukulele|O ukulele é um pequeno instrumento de cordas.|ukulele
            Universo|O universo tem estrelas e planetas.|space
            Umbu|O umbu é uma pequena fruta do umbuzeiro.|umbu
        """),
        group('V', """
            Vaca|A vaca se alimenta de plantas.|cow
            Vela|A vela pode iluminar e deve ficar com um adulto.|candle
            Violão|O violão tem cordas para tocar músicas.|acousticguitar
            Vaso|O vaso pode receber uma plantinha.|vase
            Vestido|O vestido é uma peça de roupa.|dress
            Vassoura|A vassoura ajuda a limpar o chão.|broom
            Vulcão|O vulcão pode liberar lava do interior da Terra.|volcano
            Ventilador|O ventilador movimenta o ar.|fan
            Vagalume|O vagalume pode produzir uma pequena luz.|firefly
            Vitrola|A vitrola toca música de discos.|recordplayer
        """),
        group('W', """
            Waffle|Waffle é o nome de uma massa assada com quadradinhos.|waffle
            Windsurfe|No windsurfe a vela ajuda a prancha a andar na água.|windsurf
            Wagner|Wagner é o nome deste personagem.|person|nome
            Wanda|Wanda é o nome desta personagem.|person|nome
            Walter|Walter é o nome deste personagem.|person|nome
            William|William é o nome deste personagem.|person|nome
            Wilson|Wilson é o nome deste personagem.|person|nome
            Wesley|Wesley é o nome deste personagem.|person|nome
            Wellington|Wellington é o nome deste personagem.|person|nome
            Wilma|Wilma é o nome desta personagem.|person|nome
        """),
        group('X', """
            Xícara|A xícara tem uma alça para segurar.|cup
            Xilofone|O xilofone tem peças que produzem notas.|xylophone
            Xadrez|O xadrez é um jogo de peças em um tabuleiro.|chess
            Xale|O xale pode aquecer os ombros.|shawl
            Xerife|Este xerife é um personagem de histórias.|sheriff
            Xampu|O xampu ajuda a lavar os cabelos.|shampoo
            Xaréu|O xaréu é um peixe que vive no mar.|fish
            Xaxim|O xaxim é uma planta que devemos proteger.|fern
            Xique-xique|O xique-xique é um cacto brasileiro.|cactus
            Xaxado|O xaxado é uma dança brasileira.|dancer
        """),
        group('Y', """
            Yara|Yara é o nome desta personagem.|person|nome
            Yasmin|Yasmin é o nome desta personagem.|person|nome
            Yuri|Yuri é o nome deste personagem.|person|nome
            Yago|Yago é o nome deste personagem.|person|nome
            Yolanda|Yolanda é o nome desta personagem.|person|nome
            Yohana|Yohana é o nome desta personagem.|person|nome
            Yumi|Yumi é o nome desta personagem.|person|nome
            Yves|Yves é o nome deste personagem.|person|nome
            Yasmina|Yasmina é o nome desta personagem.|person|nome
            Yakisoba|Yakisoba é o nome de um prato com macarrão e vegetais.|noodles
        """),
        group('Z', """
            Zebra|A zebra tem listras pretas e brancas.|zebra
            Zíper|O zíper ajuda a abrir e fechar uma roupa.|zipper
            Zangão|O zangão é uma abelha macho.|bee
            Zebu|O zebu é um tipo de bovino.|zebu
            Zepelim|O zepelim é um dirigível que pode voar.|airship
            Zabumba|A zabumba é um tambor usado na música brasileira.|drum
            Zigue-zague|O zigue-zague muda de direção várias vezes.|zigzag
            Zero|O zero representa nenhuma unidade nesta contagem.|zero
            Zoológico|No zoológico podemos conhecer diferentes animais.|zoo
            Zircão|O zircão é um mineral encontrado na natureza.|gem
        """)
    ).flatten()
    fun forLetter(letter: Char) = words.filter { it.letter == letter }
    fun initialOf(text: String): Char = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("""\p{M}+"""), "").uppercase(Locale.ROOT).first()
}

/** Quantidade verdadeira nunca é reduzida pelo modo sensorial. */
data class CountingRound(val target: Int, val selected: Set<Int> = emptySet()) {
    init { require(target in 0..30); require(selected.all { it in 0 until target }) }
    fun toggle(index: Int): CountingRound = if(index !in 0 until target) this else copy(selected = if(index in selected) selected - index else selected + index)
    val complete get() = selected.size == target
}
