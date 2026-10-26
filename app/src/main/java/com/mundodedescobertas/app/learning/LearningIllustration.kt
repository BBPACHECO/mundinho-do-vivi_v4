package com.mundodedescobertas.app.learning

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.*

/** Desenhos vetoriais autorais: formas, acessórios e cores são montados localmente. */
@Composable
fun LearningIllustration(word: LearningWord, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = "Ilustração de ${word.word}" }) {
        val unit = min(size.width, size.height) / 120f
        translate((size.width-120f*unit)/2,(size.height-120f*unit)/2) {
            scale(unit,unit,Offset.Zero) { drawLearningArt(word.art,word.word.hashCode()) }
        }
    }
}

private val ink=Color(0xFF39374F)
private val purple=Color(0xFF927AC5)
private val mint=Color(0xFF8FCDB2)
private val peach=Color(0xFFFFAF96)
private val yellow=Color(0xFFFFD77D)
private val blue=Color(0xFF90C4E7)
private val rose=Color(0xFFE58BAA)
private val brown=Color(0xFFB18866)
private val green=Color(0xFF76AC76)
private fun DrawScope.line(x:Float,y:Float,x2:Float,y2:Float,c:Color=ink,w:Float=3f)=drawLine(c,Offset(x,y),Offset(x2,y2),w,StrokeCap.Round)
private fun DrawScope.oval(c:Color,x:Float,y:Float,w:Float,h:Float)=drawOval(c,Offset(x,y),Size(w,h))
private fun DrawScope.box(c:Color,x:Float,y:Float,w:Float,h:Float,r:Float=8f)=drawRoundRect(c,Offset(x,y),Size(w,h),CornerRadius(r))
private fun DrawScope.dot(c:Color,x:Float,y:Float,r:Float)=drawCircle(c,r,Offset(x,y))
private fun DrawScope.path(c:Color,vararg points:Float) {
    val p=Path(); points.asList().chunked(2).forEachIndexed { i,xy -> if(i==0)p.moveTo(xy[0],xy[1])else p.lineTo(xy[0],xy[1]) };p.close();drawPath(p,c)
}
private fun DrawScope.smile(x:Float=60f,y:Float=62f,gap:Float=10f) {
    dot(ink,x-gap,y,2.3f);dot(ink,x+gap,y,2.3f)
    drawArc(ink,10f,160f,false,Offset(x-5,y+3),Size(10f,7f),style=Stroke(2.2f,cap=StrokeCap.Round))
}
private fun DrawScope.star(x:Float,y:Float,r:Float,c:Color=yellow) {
    val p=Path()
    repeat(10){i->val a=i*PI/5-PI/2;val radius=if(i%2==0)r else r*.46f;val px=x+(cos(a)*radius).toFloat();val py=y+(sin(a)*radius).toFloat();if(i==0)p.moveTo(px,py)else p.lineTo(px,py)}
    p.close();drawPath(p,c)
}

private fun DrawScope.drawLearningArt(a:String,seed:Int) {
    val tint=listOf(peach,purple,mint,blue,yellow,rose)[(seed and Int.MAX_VALUE)%6]
    when(a) {
        "dog","cat","rabbit","horse","giraffe","llama","unicorn","lion","tiger","jaguar","fox","mouse","bear","hamster","monkey","squirrel","coati","sheep","cow","zebra","zebu","hippo","pig","elephant","hedgehog","camel" -> animal(a,tint)
        "duck","hen","rooster","owl","penguin","toucan","vulture" -> bird(a)
        "fish","dolphin","whale","shark","seal" -> fish(a)
        "bee","butterfly","spider","ant","ladybug","insect","firefly" -> insect(a)
        "turtle","tortoise","alligator","iguana","dinosaur","frog","snake","caterpillar","snail" -> reptile(a)
        "apple","orange","guava","lemon","pear","umbu","walnut","strawberry","raspberry","pineapple","banana","avocado","watermelon","grapes","kiwi","jackfruit" -> fruit(a)
        "pumpkin","carrot","broccoli","corn","pea","yam","turnip","radish","tomato","okra","seed" -> vegetable(a)
        "tree","floweringtree","flower","sunflower","orchid","rose","leaf","mint","fern","cactus","garden" -> plant(a)
        "car","truck","jeep","kart","bus","tractor","motorcycle","bicycle","train" -> vehicle(a)
        "boat","ship","raft","windsurf","kitesurf" -> waterVehicle(a)
        "plane","helicopter","rocket","airship","propeller" -> flying(a)
        "house","castle","hospital","church","igloo","window","lighthouse","bed","frame","drawing" -> building(a)
        "sun","moon","star","cloud","rainbow","drop","mountain","volcano","river","island","snowflake","space" -> nature(a)
        "guitar","acousticguitar","ukulele","harp","piano","flute","saxophone","drum","triangle","musicnote","xylophone","microphone","disc","recordplayer","radio" -> music(a)
        "person","karate","sheriff","dancer","fairy","mermaid" -> person(a,seed)
        "cake","candy","jellybean","burger","sandwich","bread","popcorn","cheese","egg","quindim","icecream","waffle","noodles","oyster","ketchup" -> food(a)
        "aquarium" -> {
            box(blue,7f,23f,106f,84f,6f);box(Color(0xFFDDF2FC),12f,28f,96f,72f,3f)
            box(blue.copy(alpha=.5f),12f,56f,96f,44f,3f);box(yellow,13f,93f,94f,7f,1f)
            translate(24f,44f){scale(.55f,.55f,Offset.Zero){fish("fish")}}
            dot(Color.White,86f,48f,3f);dot(Color.White,92f,37f,4f)
        }
        "mirror" -> {
            oval(purple,23f,5f,75f,87f);oval(Color(0xFFDDF2FC),29f,11f,63f,75f)
            line(62f,93f,62f,111f,purple,7f);line(37f,112f,87f,112f,purple,8f)
            line(41f,41f,65f,20f,Color.White,5f);line(48f,57f,79f,30f,Color.White,4f)
        }
        "heart" -> {val p=Path().apply{moveTo(60f,104f);cubicTo(0f,65f,8f,14f,43f,22f);quadraticTo(55f,24f,60f,38f);cubicTo(80f,-4f,137f,35f,60f,104f);close()};drawPath(p,rose);smile(60f,58f)}
        "diamond","gem" -> {path(blue,16f,43f,36f,19f,84f,19f,105f,43f,60f,106f);line(17f,44f,104f,44f,Color.White);line(36f,20f,60f,105f,Color.White);line(84f,20f,60f,105f,Color.White)}
        "ring" -> {drawCircle(yellow,30f,Offset(60f,71f),style=Stroke(10f));path(blue,42f,35f,50f,19f,70f,19f,78f,35f,60f,50f)}
        "ball" -> {dot(peach,60f,62f,42f);drawCircle(ink,42f,Offset(60f,62f),style=Stroke(2f));drawArc(ink,-70f,140f,false,Offset(15f,31f),Size(39f,70f),style=Stroke(2f));line(26f,38f,94f,87f,ink,2f)}
        "balloon" -> {oval(tint,28f,8f,64f,80f);path(tint,54f,84f,66f,84f,60f,94f);val p=Path().apply{moveTo(60f,94f);cubicTo(35f,100f,80f,110f,51f,117f)};drawPath(p,ink,style=Stroke(2f));smile(60f,44f)}
        "kite" -> {path(purple,60f,8f,101f,50f,60f,94f,20f,50f);path(peach,60f,8f,60f,94f,20f,50f);line(60f,8f,60f,94f,ink,2f);line(20f,50f,101f,50f,ink,2f);line(60f,94f,73f,116f);path(mint,65f,106f,80f,98f,79f,113f)}
        "book" -> {box(blue,10f,24f,50f,76f);box(purple,60f,24f,50f,76f);path(Color(0xFFFFFCF3),17f,18f,56f,27f,56f,92f,17f,83f);path(Color.White,64f,27f,103f,18f,103f,83f,64f,92f);line(60f,28f,60f,103f,purple,4f)}
        "pencil","brush","toothbrush","artkit","pencilcase","backpack","hammer","broom","flashlight","scissors","racket","ladder","umbrella","magnet","yoyo","bow","buckle","zipper","glasses","envelope","ballotbox","shirt","dress","shawl","boot","shoe","sneaker","cup","pot","jug","vase","spoon","clock","fan","hydrant","bell","shampoo","tooth","eye","nose","ear","hand","yarn","nest","robot","dice","domino","puzzle","chess","square","four","zero","number","zigzag","wheel","zoo","candle" -> objectArt(a,tint)
        else -> {box(tint,20f,20f,80f,80f);star(60f,60f,29f,Color.White)}
    }
}

private fun DrawScope.animal(a:String,c:Color) {
    if(a=="coati") {
        oval(brown,27f,57f,62f,44f);dot(brown,76f,41f,24f)
        path(brown,84f,35f,116f,59f,79f,57f);dot(ink,111f,57f,4f);dot(ink,83f,34f,3f)
        val tail=Path().apply{moveTo(33f,77f);cubicTo(0f,83f,12f,39f,15f,12f)};drawPath(tail,brown,style=Stroke(15f,cap=StrokeCap.Round))
        repeat(4){i->line(10f,20f+i*11,21f,23f+i*11,ink,5f)}
        box(brown,42f,91f,11f,24f);box(brown,74f,90f,11f,25f);return
    }
    if(a=="giraffe") {
        box(yellow,43f,49f,31f,66f);oval(yellow,28f,17f,65f,48f);oval(peach,37f,44f,59f,24f)
        line(43f,23f,39f,5f,brown,5f);line(73f,22f,77f,4f,brown,5f)
        repeat(4){i->dot(brown,51f+(i%2)*17,72f+(i/2)*22,6f)}
        dot(ink,47f,34f,3f);dot(ink,73f,34f,3f);return
    }
    val body=when(a){"zebra","sheep"->Color(0xFFFFFAED);"tiger","lion","giraffe"->yellow;"fox"->peach;"pig"->rose;"elephant","hippo"->blue;"cow","zebu"->Color(0xFFE5D8C9);else->c}
    if(a=="lion")dot(brown,60f,61f,47f)
    if(a=="rabbit"||a=="llama"||a=="horse"||a=="unicorn"||a=="zebra") {
        oval(body,27f,1f,17f,55f);oval(body,76f,1f,17f,55f)
        oval(rose.copy(alpha=.5f),32f,9f,7f,31f);oval(rose.copy(alpha=.5f),81f,9f,7f,31f)
    } else if(a=="cat"||a=="fox") {
        path(body,18f,45f,20f,9f,47f,34f);path(body,73f,34f,100f,9f,102f,45f)
    } else {dot(body,30f,33f,16f);dot(body,90f,33f,16f)}
    if(a=="squirrel") {oval(brown,1f,34f,41f,79f);oval(peach,9f,43f,23f,55f)}
    oval(body,17f,28f,86f,78f)
    when(a) {
        "horse","llama"->{path(brown,44f,30f,52f,13f,60f,25f,69f,12f,79f,31f)}
        "dog"->{oval(brown,13f,29f,24f,51f);oval(brown,84f,29f,24f,51f)}
        "elephant"->{oval(body,2f,36f,30f,55f);oval(body,88f,36f,30f,55f);box(body,50f,67f,21f,50f,12f)}
        "tiger","zebra"->{for(i in 0..2){path(ink,29f+i*24f,31f,38f+i*24f,31f,33f+i*24f,57f)}}
        "jaguar","cow","zebu"->{for(i in 0..4)dot(brown,26f+i*16f,42f+(i%2)*8f,5f)}
        "sheep"->{for(i in 0..5)dot(Color.White,23f+i*15f,31f+(i%2)*3f,13f)}
        "unicorn"->path(yellow,51f,29f,60f,1f,69f,29f)
        "monkey","mouse","hamster"->{oval(peach,31f,46f,58f,53f)}
        "hedgehog"->{for(i in 0..5)path(brown,17f+i*14f,39f,24f+i*14f,11f,31f+i*14f,39f)}
    }
    smile(60f,62f,15f)
    if(a=="pig"||a=="hippo"||a=="horse"||a=="zebu"||a=="cow"||a=="zebra") {oval(peach,37f,77f,46f,22f);dot(ink,50f,88f,3f);dot(ink,70f,88f,3f)}
    else if(a!="elephant") {path(ink,55f,72f,65f,72f,60f,78f)}
    if(a=="cat"||a=="mouse"||a=="hamster") {line(21f,74f,42f,77f);line(20f,86f,42f,82f);line(78f,77f,99f,74f);line(78f,82f,100f,86f)}
}

private fun DrawScope.bird(a:String) {
    val color=when(a){"penguin","toucan","vulture"->ink;"duck"->yellow;"hen","rooster"->Color(0xFFFFF5DA);else->brown}
    if(a=="rooster") {
        val tail=Path().apply{moveTo(36f,70f);cubicTo(-3f,65f,3f,17f,24f,24f)};drawPath(tail,purple,style=Stroke(9f,cap=StrokeCap.Round))
        val second=Path().apply{moveTo(35f,74f);cubicTo(-7f,76f,3f,35f,15f,38f)};drawPath(second,mint,style=Stroke(7f,cap=StrokeCap.Round))
    }
    oval(color,25f,31f,70f,75f);dot(color,58f,34f,24f)
    if(a=="penguin")oval(Color.White,39f,47f,43f,54f)
    if(a=="owl"){dot(Color(0xFFFFEAC8),45f,51f,16f);dot(Color(0xFFFFEAC8),76f,51f,16f);dot(ink,45f,51f,5f);dot(ink,76f,51f,5f)} else {dot(Color.White,67f,29f,7f);dot(ink,68f,29f,3f)}
    if(a=="toucan") {box(yellow,71f,36f,43f,19f,9f);box(peach,94f,36f,20f,19f,8f)} else path(peach,74f,39f,102f,47f,75f,55f)
    if(a=="hen"||a=="rooster") {dot(rose,45f,10f,9f);dot(rose,56f,8f,9f);dot(rose,66f,13f,8f)}
    oval(color.copy(alpha=.6f),16f,60f,30f,26f);line(45f,101f,44f,112f,peach,6f);line(76f,101f,77f,112f,peach,6f)
}
private fun DrawScope.fish(a:String) {
    if(a=="seal") {
        oval(blue,18f,48f,84f,51f);dot(blue,74f,37f,26f)
        oval(peach,61f,41f,36f,22f);dot(ink,65f,30f,3f);dot(ink,84f,30f,3f);dot(ink,76f,44f,4f)
        line(58f,46f,45f,42f,ink,2f);line(57f,53f,43f,57f,ink,2f)
        line(94f,46f,108f,42f,ink,2f);line(94f,53f,111f,57f,ink,2f)
        oval(blue,35f,84f,27f,27f);path(blue,24f,84f,6f,74f,5f,105f,31f,94f);return
    }
    val c=if(a=="shark")blue else if(a=="seal")brown else if(a=="whale")purple else peach
    path(c,23f,59f,3f,37f,3f,85f);oval(c,17f,30f,94f,64f)
    if(a=="dolphin"||a=="shark") {path(c,57f,33f,72f,9f,82f,40f);path(c,102f,45f,119f,56f,104f,65f)}
    if(a=="whale") {line(78f,25f,80f,7f,blue,4f);line(80f,13f,71f,7f,blue,3f);line(80f,13f,90f,7f,blue,3f)}
    oval(c.copy(alpha=.5f),56f,71f,24f,31f);dot(ink,89f,48f,3f)
    drawArc(ink,0f,120f,false,Offset(83f,53f),Size(17f,13f),style=Stroke(2f))
    if(a=="fish")drawArc(ink,-70f,140f,false,Offset(58f,33f),Size(15f,55f),style=Stroke(2f))
}
private fun DrawScope.insect(a:String) {
    val n=if(a=="spider")4 else 3
    repeat(n){i->line(44f,45f+i*12,18f,30f+i*21,ink,3f);line(76f,45f+i*12,102f,30f+i*21,ink,3f)}
    if(a=="bee"||a=="butterfly"||a=="firefly"){oval(blue.copy(alpha=.65f),13f,18f,45f,47f);oval(rose.copy(alpha=.65f),62f,18f,45f,47f)}
    oval(if(a=="bee")yellow else if(a=="ladybug")rose else purple,35f,28f,50f,70f)
    if(a=="bee")repeat(3){i->line(40f,50f+i*15,80f,50f+i*15,ink,6f)}
    if(a=="ladybug"){line(60f,37f,60f,94f);repeat(3){i->dot(ink,48f,48f+i*17,4f);dot(ink,72f,48f+i*17,4f)}}
    dot(ink,60f,28f,17f);dot(Color.White,54f,26f,3f);dot(Color.White,66f,26f,3f)
    if(a=="firefly")dot(yellow,60f,90f,13f)
    line(50f,17f,42f,6f,ink,2f);line(70f,17f,78f,6f,ink,2f)
}
private fun DrawScope.reptile(a:String) {
    when(a) {
        "caterpillar"->{repeat(5){i->dot(if(i%2==0)mint else green,22f+i*18,69f-i*3,16f)};smile(95f,48f,6f)}
        "snake"->{val p=Path().apply{moveTo(20f,91f);cubicTo(105f,105f,17f,39f,79f,46f)};drawPath(p,mint,style=Stroke(21f,cap=StrokeCap.Round));oval(mint,71f,26f,39f,31f);dot(ink,95f,36f,3f);line(108f,46f,117f,44f,rose,2f)}
        "snail"->{box(mint,9f,79f,99f,23f,12f);dot(brown,56f,65f,32f);drawArc(yellow,0f,600f,false,Offset(39f,49f),Size(34f,34f),style=Stroke(5f));line(96f,84f,99f,62f);line(105f,84f,112f,64f);dot(ink,99f,61f,3f);dot(ink,113f,63f,3f)}
        "frog"->{oval(mint,14f,39f,93f,62f);dot(mint,37f,33f,18f);dot(mint,82f,33f,18f);dot(Color.White,37f,32f,11f);dot(Color.White,82f,32f,11f);dot(ink,37f,32f,4f);dot(ink,82f,32f,4f);drawArc(ink,5f,170f,false,Offset(32f,57f),Size(54f,23f),style=Stroke(3f))}
        "turtle","tortoise"->{oval(mint,5f,62f,22f,26f);oval(mint,91f,57f,25f,28f);repeat(2){i->oval(mint,26f+i*48,86f,20f,19f)};oval(green,20f,33f,78f,62f);drawArc(yellow,190f,160f,false,Offset(35f,43f),Size(44f,42f),style=Stroke(4f));line(59f,36f,59f,93f,yellow,3f);dot(ink,109f,64f,2.5f)}
        else->{path(mint,20f,87f,1f,108f,40f,96f);oval(mint,18f,48f,68f,52f);oval(mint,63f,23f,38f,43f);box(mint,77f,41f,39f,21f);dot(ink,87f,32f,3f);box(mint,30f,88f,16f,25f);box(mint,64f,88f,16f,25f);repeat(4){i->path(green,22f+i*12,53f,27f+i*12,36f,34f+i*12,53f)}}
    }
}
private fun DrawScope.fruit(a:String) {
    when(a) {
        "banana"->{val p=Path().apply{moveTo(21f,25f);cubicTo(12f,105f,100f,123f,109f,39f);cubicTo(82f,89f,33f,73f,21f,25f);close()};drawPath(p,yellow);line(19f,24f,22f,14f,brown,7f);line(106f,41f,110f,32f,brown,5f)}
        "grapes"->{repeat(4){row->repeat(4-row){col->dot(purple,31f+row*10+col*20,40f+row*19,14f)}};line(61f,29f,67f,9f,brown,5f);oval(green,61f,10f,29f,14f)}
        "pineapple","jackfruit"->{oval(if(a=="pineapple")yellow else green,24f,29f,72f,80f);repeat(5){i->line(32f+i*12,41f,29f+i*12,96f,brown.copy(alpha=.5f),2f)};repeat(4){i->line(30f,43f+i*16,91f,44f+i*16,brown.copy(alpha=.5f),2f)};if(a=="pineapple")path(green,37f,34f,26f,11f,49f,24f,60f,1f,70f,25f,95f,12f,82f,36f)else line(60f,31f,61f,12f,brown,6f)}
        "watermelon"->{drawArc(green,0f,180f,true,Offset(8f,6f),Size(104f,104f));drawArc(rose,0f,180f,true,Offset(16f,14f),Size(88f,88f));repeat(5){i->oval(ink,29f+i*13,64f+(i%2)*12,3f,6f)}}
        "avocado","pear"->{val p=Path().apply{moveTo(61f,14f);cubicTo(29f,13f,46f,39f,24f,65f);cubicTo(-3f,120f,122f,120f,99f,69f);cubicTo(82f,39f,88f,16f,61f,14f);close()};drawPath(p,green);if(a=="avocado"){oval(yellow,27f,53f,66f,49f);dot(brown,60f,77f,18f)};line(61f,16f,65f,5f,brown,5f)}
        "kiwi"->{dot(brown,60f,62f,47f);dot(green,60f,62f,40f);dot(Color(0xFFEEE9BB),60f,62f,13f);repeat(12){i->val t=i*PI/6;dot(ink,60f+(cos(t)*25).toFloat(),62f+(sin(t)*25).toFloat(),2f)}}
        "strawberry","raspberry"->{if(a=="strawberry"){path(rose,17f,40f,31f,24f,87f,24f,105f,41f,61f,110f);repeat(4){r->repeat(4-r){c->oval(yellow,34f+r*6+c*15,43f+r*15,3f,5f)}}}else{repeat(4){r->repeat(4-r){c->dot(rose,31f+r*10+c*19,34f+r*19,13f)}}};path(green,31f,28f,40f,11f,54f,24f,63f,8f,72f,24f,90f,18f,86f,32f)}
        "walnut"->{oval(brown,21f,22f,78f,88f);val p=Path().apply{moveTo(62f,27f);cubicTo(29f,47f,87f,60f,58f,105f)};drawPath(p,yellow,style=Stroke(5f));drawArc(yellow,170f,150f,false,Offset(29f,42f),Size(30f,43f),style=Stroke(3f))}
        else->{val color=when(a){"lemon"->yellow;"orange"->peach;"guava","umbu"->mint;else->rose};oval(color,20f,30f,81f,78f);line(60f,31f,64f,12f,brown,5f);oval(green,64f,11f,28f,14f);if(a=="guava"){oval(rose,39f,45f,45f,48f);repeat(6){i->dot(yellow,51f+(i%2)*17,55f+(i/2)*12,2f)}};if(a=="lemon"){path(yellow,20f,61f,10f,68f,22f,76f);path(yellow,99f,61f,111f,67f,99f,75f)}}
    }
}
private fun DrawScope.vegetable(a:String) {
    when(a) {
        "carrot"->{path(peach,28f,36f,96f,36f,62f,111f);repeat(3){i->line(42f+i*6,48f+i*15,63f+i*4,48f+i*15,brown,2f)};path(green,43f,36f,28f,7f,53f,24f,63f,2f,71f,26f,96f,9f,81f,36f)}
        "broccoli"->{box(green,48f,51f,25f,58f);dot(mint,34f,45f,26f);dot(mint,62f,30f,27f);dot(mint,89f,47f,25f)}
        "corn"->{oval(yellow,31f,13f,58f,95f);repeat(5){r->repeat(3){c->dot(peach,45f+c*15,29f+r*16,5f)}};path(green,37f,111f,9f,49f,47f,67f,63f,112f);path(mint,63f,112f,76f,53f,109f,39f,88f,111f)}
        "pea"->{oval(green,9f,38f,105f,43f);repeat(4){i->dot(mint,27f+i*23,58f,14f)}}
        "seed"->{oval(brown,39f,75f,44f,30f);line(60f,77f,60f,30f,green,5f);oval(mint,59f,25f,36f,20f);oval(green,25f,39f,36f,21f)}
        "pumpkin"->{oval(peach,13f,33f,93f,70f);oval(yellow,33f,32f,54f,72f);line(60f,32f,63f,15f,green,9f);drawArc(brown,75f,220f,false,Offset(68f,14f),Size(25f,20f),style=Stroke(3f))}
        "tomato"->{dot(rose,60f,67f,39f);path(green,60f,42f,33f,27f,54f,28f,60f,12f,67f,28f,91f,27f,72f,44f)}
        "okra"->{oval(green,46f,19f,29f,95f);line(59f,24f,59f,104f,mint,4f);path(green,47f,30f,39f,13f,80f,13f,75f,30f)}
        "turnip","radish"->{oval(if(a=="radish")rose else Color(0xFFF6E5F1),28f,43f,65f,58f);path(green,48f,48f,31f,9f,57f,30f,68f,4f,76f,32f,99f,16f,79f,50f);line(61f,99f,52f,113f,brown,3f)}
        else->{oval(brown,25f,36f,76f,62f);line(45f,46f,39f,66f,yellow,2f);line(66f,72f,76f,89f,yellow,2f)}
    }
}
private fun DrawScope.plant(a:String) {
    when(a) {
        "tree","floweringtree"->{box(brown,49f,53f,23f,61f);dot(if(a=="floweringtree")purple else mint,36f,44f,28f);dot(if(a=="floweringtree")rose else green,67f,29f,30f);dot(if(a=="floweringtree")purple else mint,87f,49f,25f)}
        "garden"->{box(brown,9f,83f,103f,26f);repeat(3){i->line(27f+i*33,87f,27f+i*33,44f,green,4f);repeat(5){j->val t=j*PI*2/5;dot(if(i%2==0)rose else yellow,27f+i*33+(cos(t)*12).toFloat(),35f+(sin(t)*12).toFloat(),9f)};dot(yellow,27f+i*33,35f,6f)}}
        "leaf","mint","fern"->{line(60f,109f,61f,16f,green,4f);repeat(if(a=="leaf")1 else 4){i->val y=25f+i*20;oval(mint,24f,y,36f,19f);oval(green,60f,y-8f,36f,19f)}}
        "cactus"->{box(green,48f,10f,25f,101f,13f);box(green,20f,38f,25f,44f,12f);box(green,78f,26f,24f,45f,12f);line(40f,70f,54f,70f,green,13f);line(70f,58f,88f,58f,green,13f);repeat(5){i->line(55f,25f+i*16,65f,29f+i*16,yellow,2f)}}
        else->{line(60f,111f,60f,62f,green,5f);oval(mint,59f,78f,33f,17f);repeat(if(a=="sunflower")12 else 6){i->val t=i*PI*2/(if(a=="sunflower")12 else 6);val c=if(a=="sunflower")yellow else if(a=="rose")rose else purple;dot(c,60f+(cos(t)*24).toFloat(),43f+(sin(t)*24).toFloat(),16f)};dot(if(a=="sunflower")brown else yellow,60f,43f,17f)}
    }
}
private fun DrawScope.vehicle(a:String) {
    if(a=="bicycle"||a=="motorcycle") {
        drawCircle(ink,23f,Offset(27f,85f),style=Stroke(5f));drawCircle(ink,23f,Offset(94f,85f),style=Stroke(5f))
        line(27f,85f,49f,48f,purple,5f);line(49f,48f,72f,85f,purple,5f);line(72f,85f,27f,85f,purple,5f);line(72f,85f,85f,43f,purple,5f);line(85f,43f,94f,85f,purple,5f);line(81f,43f,101f,37f,ink,5f);line(40f,45f,56f,45f,ink,6f)
        if(a=="motorcycle")oval(peach,45f,49f,34f,27f)
        return
    }
    val c=if(a=="bus")yellow else if(a=="tractor")mint else blue
    box(c,10f,45f,101f,50f,12f)
    if(a=="truck"||a=="tractor"){box(c,71f,23f,37f,61f);box(Color(0xFFE5F4FF),77f,29f,24f,24f,3f)}
    else if(a=="train"){box(c,28f,15f,65f,73f);box(Color(0xFFE5F4FF),38f,25f,45f,32f,5f);line(21f,108f,102f,108f,brown,4f)}
    else {box(c,29f,24f,63f,48f,13f);repeat(if(a=="bus")3 else 2){i->box(Color(0xFFE5F4FF),36f+i*21,31f,17f,22f,3f)}}
    dot(ink,31f,95f,13f);dot(ink,92f,95f,13f);dot(Color.White,31f,95f,5f);dot(Color.White,92f,95f,5f)
}
private fun DrawScope.waterVehicle(a:String) {
    line(7f,108f,112f,108f,blue,5f)
    if(a=="kitesurf"){path(purple,21f,8f,80f,9f,109f,35f,12f,35f);line(29f,35f,65f,82f,ink,1.5f);line(93f,35f,65f,82f,ink,1.5f);dot(peach,65f,70f,8f);line(65f,79f,65f,99f,purple,6f);line(45f,103f,86f,103f,yellow,5f);return}
    path(if(a=="ship")blue else brown,9f,76f,112f,76f,93f,103f,29f,103f)
    if(a=="raft"){repeat(4){i->line(22f,85f+i*4,102f,85f+i*4,yellow,3f)}}
    else if(a=="ship"){box(Color.White,32f,43f,57f,34f);repeat(3){i->dot(blue,42f+i*18,61f,5f)};box(rose,56f,19f,14f,24f,3f)}
    else{line(60f,15f,60f,80f,ink,3f);path(a.let{if(it=="windsurf")purple else peach},56f,18f,19f,68f,56f,68f);path(yellow,65f,29f,96f,68f,65f,68f)}
}
private fun DrawScope.flying(a:String) {
    when(a) {
        "helicopter","propeller"->{if(a=="helicopter"){oval(mint,24f,36f,76f,56f);box(blue,65f,46f,26f,25f);path(mint,29f,55f,4f,34f,7f,70f,35f,75f);line(34f,103f,96f,103f,ink,4f);line(47f,89f,45f,102f,ink,3f)};line(60f,21f,60f,36f,ink,4f);line(18f,20f,104f,20f,ink,6f)}
        "rocket"->{path(purple,28f,71f,12f,106f,40f,94f);path(purple,92f,71f,108f,106f,80f,94f);val p=Path().apply{moveTo(39f,93f);lineTo(39f,48f);quadraticTo(40f,22f,60f,4f);quadraticTo(81f,23f,81f,48f);lineTo(81f,93f);close()};drawPath(p,peach);dot(blue,60f,49f,12f);path(yellow,47f,94f,60f,118f,73f,94f)}
        "airship"->{oval(purple,5f,20f,110f,61f);path(peach,18f,51f,2f,33f,2f,79f);box(yellow,46f,79f,35f,20f);line(42f,73f,49f,82f);line(89f,73f,78f,82f)}
        else->{path(blue,6f,66f,49f,46f,56f,8f,68f,8f,71f,47f,112f,66f,112f,76f,72f,68f,72f,98f,88f,109f,87f,116f,62f,108f,37f,116f,36f,109f,52f,98f,52f,68f,6f,76f)}
    }
}
private fun DrawScope.building(a:String) {
    when(a) {
        "window"->{box(brown,17f,13f,86f,97f);box(blue,24f,20f,72f,83f,2f);line(60f,19f,60f,104f,Color.White,5f);line(24f,61f,96f,61f,Color.White,5f)}
        "drawing","frame"->{box(brown,7f,13f,107f,97f,4f);box(Color.White,14f,20f,93f,83f,2f);dot(yellow,87f,41f,12f);path(mint,18f,98f,48f,49f,75f,98f);path(purple,56f,98f,82f,65f,105f,98f)}
        "bed"->{box(brown,8f,30f,10f,80f,2f);box(brown,104f,56f,9f,54f,2f);box(blue,17f,60f,89f,32f,3f);box(Color.White,18f,47f,32f,19f,7f);line(12f,94f,110f,94f,brown,7f)}
        "igloo"->{drawArc(blue,180f,180f,true,Offset(5f,12f),Size(110f,172f));drawArc(ink,180f,180f,true,Offset(45f,66f),Size(37f,60f));line(14f,67f,105f,67f,Color.White,3f);line(27f,43f,95f,43f,Color.White,3f);line(60f,22f,60f,63f,Color.White,3f)}
        "lighthouse"->{path(Color.White,38f,98f,45f,32f,77f,32f,84f,98f);box(rose,40f,49f,41f,17f,1f);box(rose,37f,81f,47f,17f,1f);box(blue,40f,15f,42f,23f,3f);path(rose,35f,15f,61f,2f,88f,15f);line(11f,26f,34f,26f,yellow,4f);line(89f,26f,113f,26f,yellow,4f)}
        else->{box(if(a=="hospital")blue else peach,19f,40f,82f,70f,3f);path(if(a=="castle")purple else brown,10f,43f,60f,5f,110f,43f);box(purple,49f,71f,23f,39f,4f);box(Color.White,29f,53f,14f,18f,3f);box(Color.White,79f,53f,14f,18f,3f);if(a=="castle"){box(purple,7f,19f,22f,87f,2f);box(purple,91f,19f,22f,87f,2f);repeat(3){i->box(purple,4f+i*10,10f,6f,15f,0f);box(purple,88f+i*10,10f,6f,15f,0f)}};if(a=="hospital"){line(52f,52f,69f,52f,rose,6f);line(60f,44f,60f,60f,rose,6f)};if(a=="church"){line(60f,1f,60f,25f,brown,4f);line(51f,11f,69f,11f,brown,4f)}}
    }
}
private fun DrawScope.nature(a:String) {
    when(a) {
        "sun"->{repeat(12){i->val t=i*PI/6;line(60f+(cos(t)*41).toFloat(),60f+(sin(t)*41).toFloat(),60f+(cos(t)*53).toFloat(),60f+(sin(t)*53).toFloat(),yellow,5f)};dot(yellow,60f,60f,32f);smile(60f,56f)}
        "moon"->{dot(yellow,60f,60f,45f);dot(Color(0xFFFFF9F2),79f,39f,39f)}
        "star"->star(60f,60f,49f)
        "cloud","snowflake"->{if(a=="cloud"){oval(blue,11f,46f,99f,47f);dot(blue,40f,43f,23f);dot(blue,72f,38f,29f);smile(60f,65f)}else{repeat(6){i->val t=i*PI/3;val x=60f+(cos(t)*46).toFloat();val y=60f+(sin(t)*46).toFloat();line(60f,60f,x,y,blue,4f);line(x,y,x-9f,y-8f,blue,3f);line(x,y,x+8f,y-9f,blue,3f)}}}
        "rainbow"->{listOf(rose,peach,yellow,mint,blue,purple).forEachIndexed{i,c->drawArc(c,180f,180f,false,Offset(5f+i*7,12f+i*7),Size(110f-i*14,150f-i*14),style=Stroke(8f))}}
        "drop"->{val p=Path().apply{moveTo(60f,8f);cubicTo(8f,66f,19f,107f,60f,110f);cubicTo(103f,106f,113f,67f,60f,8f);close()};drawPath(p,blue);smile(60f,72f)}
        "river"->{box(mint,4f,15f,112f,96f,16f);val p=Path().apply{moveTo(34f,15f);cubicTo(106f,30f,8f,92f,76f,111f)};drawPath(p,blue,style=Stroke(30f))}
        "island"->{oval(blue,6f,63f,108f,49f);oval(yellow,28f,72f,70f,27f);line(63f,80f,66f,30f,brown,6f);repeat(5){i->val t=PI+i*PI/4;line(66f,31f,66f+(cos(t)*35).toFloat(),31f+(sin(t)*22).toFloat(),green,9f)}}
        "space"->{box(Color(0xFF494667),4f,7f,112f,105f,20f);star(30f,31f,10f);star(92f,78f,8f);dot(purple,65f,65f,25f);drawOval(peach,Offset(25f,54f),Size(80f,25f),style=Stroke(6f))}
        else->{path(if(a=="volcano")brown else purple,5f,108f,58f,16f,116f,108f);path(if(a=="volcano")peach else Color.White,41f,45f,58f,16f,76f,46f,62f,39f,55f,47f);if(a=="volcano"){oval(Color(0xFFD0C6D4),38f,2f,46f,22f);path(peach,56f,35f,66f,34f,75f,83f,61f,62f)}}
    }
}
private fun DrawScope.music(a:String) {
    when(a) {
        "guitar","acousticguitar","ukulele"->{if(a=="guitar") {path(peach,24f,109f,10f,81f,23f,54f,34f,61f,39f,40f,47f,56f,62f,53f,64f,40f,81f,58f,72f,93f,52f,112f)}else{oval(brown,18f,51f,58f,61f);oval(brown,37f,37f,39f,42f)};rotate(33f,Offset(62f,54f)){box(brown,55f,7f,15f,65f,3f);box(purple,51f,2f,23f,21f,3f);line(59f,8f,59f,95f,Color.White,1.5f);line(65f,8f,65f,95f,Color.White,1.5f)};if(a=="guitar"){line(36f,79f,59f,87f,ink,5f);dot(yellow,26f,87f,4f)}else dot(ink,49f,78f,10f)}
        "piano","xylophone"->{if(a=="piano"){box(purple,5f,26f,110f,76f);repeat(7){i->box(Color.White,10f+i*14.5f,41f,13f,54f,1f)};repeat(6){i->if(i!=2)box(ink,20f+i*14.5f,40f,7f,30f,1f)}}else{repeat(7){i->box(listOf(rose,peach,yellow,mint,blue,purple,rose)[i],10f+i*14f,29f+i*4,12f,74f-i*8,3f)};line(29f,12f,79f,98f,brown,4f);dot(yellow,29f,12f,8f)}}
        "harp"->{path(yellow,16f,16f,99f,16f,92f,107f,28f,107f);path(Color(0xFFFFF9F2),29f,28f,86f,28f,82f,94f,38f,94f);repeat(6){i->line(37f+i*8,29f,40f+i*7,93f,ink,1.5f)}}
        "drum"->{box(rose,15f,35f,91f,64f,3f);oval(peach,15f,21f,91f,30f);oval(yellow,15f,85f,91f,22f);repeat(4){i->line(22f+i*22,47f,34f+i*22,86f,Color.White,3f)};line(23f,10f,79f,42f,brown,4f);line(101f,5f,53f,42f,brown,4f)}
        "flute"->{rotate(38f,Offset(60f,60f)){box(yellow,49f,6f,22f,109f,8f);repeat(6){i->dot(ink,60f,27f+i*13,3f)}}}
        "saxophone"->{val p=Path().apply{moveTo(37f,19f);lineTo(51f,22f);lineTo(49f,83f);quadraticTo(64f,111f,82f,80f);lineTo(85f,53f)};drawPath(p,yellow,style=Stroke(20f,cap=StrokeCap.Round));path(yellow,70f,60f,111f,60f,103f,40f,80f,40f);repeat(4){i->dot(ink,43f,36f+i*13,2.5f)}}
        "triangle"->{line(58f,16f,16f,102f,purple,6f);line(16f,102f,103f,102f,purple,6f);line(103f,102f,64f,29f,purple,6f);line(82f,33f,111f,72f,brown,4f)}
        "musicnote"->{line(50f,87f,50f,17f,purple,7f);line(50f,17f,96f,9f,purple,8f);line(96f,9f,96f,79f,purple,7f);oval(purple,23f,76f,29f,22f);oval(purple,69f,68f,29f,22f)}
        "microphone"->{rotate(24f,Offset(60f,59f)){box(purple,48f,48f,24f,62f,7f);dot(blue,60f,33f,25f);repeat(4){i->line(42f,21f+i*8,78f,21f+i*8,ink,1.5f)}}}
        "disc"->{dot(ink,60f,60f,47f);drawCircle(purple,35f,Offset(60f,60f),style=Stroke(2f));drawCircle(purple,41f,Offset(60f,60f),style=Stroke(1f));dot(peach,60f,60f,16f);dot(Color.White,60f,60f,4f)}
        else->{box(if(a=="radio")mint else purple,8f,27f,105f,78f,13f);dot(ink,46f,66f,27f);dot(peach,46f,66f,10f);if(a=="radio"){line(88f,28f,103f,6f,ink,3f);box(yellow,80f,42f,24f,14f,2f);dot(ink,92f,82f,7f)}else{line(97f,42f,91f,78f,yellow,5f);line(91f,78f,70f,81f,yellow,5f)}}
    }
}
private fun DrawScope.person(a:String,seed:Int) {
    val palette=listOf(Color(0xFFF5C6A7),Color(0xFFCF9874),Color(0xFF9E6D52),Color(0xFFE9B78D))
    val skin=palette[(seed and Int.MAX_VALUE)%4]
    if(a=="fairy"){oval(blue.copy(alpha=.8f),5f,47f,47f,40f);oval(purple.copy(alpha=.7f),69f,47f,47f,40f);star(104f,37f,9f)}
    dot(brown,60f,33f,29f);dot(skin,60f,39f,25f)
    path(if(a=="karate")Color.White else purple,42f,66f,78f,66f,92f,107f,28f,107f)
    line(42f,76f,20f,93f,skin,9f);line(78f,76f,100f,91f,skin,9f)
    if(a=="karate") {line(36f,86f,84f,86f,ink,6f);line(60f,86f,63f,106f,ink,4f)}
    if(a=="sheriff"||a=="dancer"){box(brown,24f,13f,74f,10f,3f);box(brown,38f,1f,44f,17f,5f);star(60f,82f,10f,yellow)}
    if(a=="mermaid"){path(mint,40f,87f,80f,87f,71f,109f,99f,104f,83f,119f,61f,114f,40f,107f)}
    smile(60f,38f,10f)
}
private fun DrawScope.food(a:String) {
    when(a) {
        "egg"->{val p=Path().apply{moveTo(60f,10f);cubicTo(21f,11f,-6f,109f,60f,111f);cubicTo(126f,109f,100f,10f,60f,10f);close()};drawPath(p,Color(0xFFF7EAD4))}
        "cake"->{box(peach,14f,49f,92f,58f,8f);box(Color(0xFFFFF4DB),14f,46f,92f,21f,8f);line(60f,46f,60f,18f,purple,6f);oval(yellow,55f,4f,10f,16f);repeat(5){i->dot(rose,24f+i*18,77f,4f)}}
        "icecream"->{path(brown,28f,56f,94f,56f,60f,116f);dot(rose,41f,43f,25f);dot(mint,79f,43f,25f);dot(peach,60f,22f,22f)}
        "popcorn"->{path(rose,23f,45f,97f,45f,86f,111f,35f,111f);repeat(3){i->line(39f+i*20,51f,43f+i*17,106f,Color.White,6f)};repeat(7){i->dot(yellow,27f+i*11,39f-(i%2)*15,13f)}}
        "cheese"->{path(yellow,15f,48f,92f,20f,106f,94f,15f,106f);dot(peach,38f,70f,10f);dot(peach,74f,49f,8f);dot(peach,83f,89f,8f)}
        "waffle"->{box(yellow,13f,16f,95f,94f,12f);repeat(4){i->repeat(4){j->box(brown.copy(alpha=.45f),23f+i*20,26f+j*20,13f,13f,2f)}}}
        "noodles"->{drawArc(blue,0f,180f,true,Offset(9f,25f),Size(104f,140f));repeat(5){i->val p=Path().apply{moveTo(24f,57f+i*5);cubicTo(50f,37f+i*5,75f,80f+i*3,100f,53f+i*4)};drawPath(p,yellow,style=Stroke(3f))};line(68f,52f,89f,4f,brown,3f);line(79f,53f,105f,9f,brown,3f)}
        "ketchup"->{box(rose,30f,33f,61f,79f,12f);box(Color.White,37f,58f,47f,31f,3f);box(yellow,43f,13f,35f,24f,3f);dot(rose,60f,73f,10f)}
        "candy","jellybean"->{if(a=="jellybean")oval(rose,19f,39f,82f,43f)else{path(purple,33f,41f,6f,27f,6f,88f,33f,76f);path(purple,87f,41f,114f,27f,114f,88f,87f,76f);box(peach,26f,32f,69f,60f,22f)}}
        "oyster"->{val p=Path().apply{moveTo(60f,109f);cubicTo(10f,87f,1f,45f,20f,36f);lineTo(36f,43f);quadraticTo(45f,3f,60f,25f);quadraticTo(76f,1f,88f,42f);quadraticTo(124f,24f,112f,72f);close()};drawPath(p,purple);repeat(4){i->line(60f,99f,29f+i*21,44f,rose,3f)};dot(Color.White,60f,76f,11f)}
        "quindim"->{oval(yellow,22f,48f,78f,55f);box(yellow,22f,40f,78f,38f,12f);oval(peach,22f,29f,78f,28f)}
        else->{box(brown,14f,38f,94f,61f,20f);box(green,12f,61f,98f,11f,4f);box(rose,18f,76f,88f,8f,2f);oval(yellow,14f,24f,94f,45f);if(a=="bread"){box(yellow,14f,38f,94f,56f,8f);repeat(3){i->line(36f+i*22,39f,28f+i*22,64f,brown,3f)}}}
    }
}
private fun DrawScope.objectArt(a:String,c:Color) {
    when(a) {
        "pencil","brush","hammer","broom","flashlight"->{rotate(28f,Offset(60f,60f)){box(if(a=="hammer"||a=="broom")brown else purple,52f,21f,16f,86f,5f);when(a){"pencil"->path(yellow,51f,21f,60f,3f,69f,21f);"brush"->{box(blue,50f,19f,20f,22f,2f);oval(peach,50f,2f,20f,32f)};"hammer"->box(blue,25f,5f,68f,29f,4f);"broom"->path(yellow,45f,15f,75f,15f,91f,48f,29f,48f);else->{box(blue,43f,8f,35f,34f,4f);path(yellow.copy(alpha=.5f),44f,8f,29f,0f,91f,0f,77f,8f)}}}}
        "toothbrush"->{rotate(22f,Offset(60f,60f)){box(blue,49f,24f,16f,91f,6f);box(blue,49f,13f,39f,23f,5f);repeat(6){i->line(60f+i*5,11f,60f+i*5,25f,Color.White,3f)}}}
        "pencilcase"->{box(purple,8f,35f,105f,54f,14f);line(18f,47f,103f,47f,yellow,4f);dot(yellow,28f,48f,6f);line(24f,54f,27f,65f,yellow,3f)}
        "artkit","backpack"->{box(c,20f,31f,80f,78f,15f);drawArc(ink,180f,180f,false,Offset(39f,13f),Size(42f,39f),style=Stroke(5f));if(a=="artkit"){repeat(4){i->box(listOf(rose,yellow,mint,blue)[i],32f+i*16,47f,10f,45f,3f)}}else{box(blue,29f,67f,62f,28f,5f);line(30f,75f,90f,75f,ink,2f)}}
        "scissors"->{drawCircle(purple,16f,Offset(30f,83f),style=Stroke(7f));drawCircle(purple,16f,Offset(85f,83f),style=Stroke(7f));line(40f,73f,86f,14f,blue,8f);line(75f,73f,31f,14f,blue,8f);dot(ink,58f,52f,4f)}
        "racket"->{drawOval(purple,Offset(23f,5f),Size(69f,75f),style=Stroke(7f));repeat(4){i->line(31f+i*16,18f,31f+i*16,68f,blue,1.5f);line(31f,22f+i*14,85f,22f+i*14,blue,1.5f)};line(58f,81f,58f,115f,brown,11f)}
        "ladder"->{line(30f,8f,20f,112f,brown,7f);line(93f,8f,103f,112f,brown,7f);repeat(5){i->line(29f-i*2,24f+i*18,94f+i*2,24f+i*18,yellow,7f)}}
        "umbrella"->{drawArc(purple,180f,180f,true,Offset(6f,4f),Size(108f,128f));line(60f,33f,60f,96f,brown,5f);drawArc(brown,0f,180f,false,Offset(39f,86f),Size(21f,24f),style=Stroke(5f));line(60f,13f,60f,5f,brown,4f)}
        "magnet"->{val p=Path().apply{moveTo(30f,25f);lineTo(30f,70f);cubicTo(30f,112f,91f,112f,91f,70f);lineTo(91f,25f)};drawPath(p,rose,style=Stroke(22f));box(blue,18f,13f,24f,26f,2f);box(blue,79f,13f,24f,26f,2f)}
        "yoyo"->{dot(purple,60f,76f,31f);dot(peach,60f,76f,22f);dot(ink,60f,76f,4f);val p=Path().apply{moveTo(60f,75f);cubicTo(21f,9f,111f,54f,70f,4f)};drawPath(p,brown,style=Stroke(2f))}
        "bow"->{path(rose,56f,59f,13f,24f,13f,92f,56f,66f);path(rose,64f,59f,108f,24f,108f,92f,64f,66f);dot(purple,60f,63f,13f)}
        "buckle"->{box(brown,4f,41f,112f,40f,1f);drawRoundRect(yellow,Offset(34f,25f),Size(52f,70f),CornerRadius(7f),style=Stroke(8f));line(60f,60f,90f,60f,yellow,5f)}
        "zipper"->{box(purple,31f,5f,58f,110f,4f);repeat(8){i->box(yellow,49f+(i%2)*12,11f+i*12,12f,9f,1f)};box(blue,45f,48f,29f,22f,5f);drawOval(ink,Offset(52f,51f),Size(15f,31f),style=Stroke(4f))}
        "glasses"->{drawCircle(purple,24f,Offset(31f,63f),style=Stroke(6f));drawCircle(purple,24f,Offset(89f,63f),style=Stroke(6f));line(55f,59f,65f,59f,purple,5f);line(7f,57f,1f,42f,purple,4f);line(113f,57f,119f,42f,purple,4f)}
        "envelope"->{box(blue,8f,26f,104f,70f,5f);path(purple,8f,26f,60f,67f,112f,26f);line(9f,96f,43f,64f,Color.White,2f);line(111f,96f,77f,64f,Color.White,2f)}
        "ballotbox"->{box(blue,17f,51f,87f,59f,6f);path(purple,17f,51f,33f,30f,89f,30f,104f,51f);line(42f,43f,83f,43f,ink,5f);rotate(-13f,Offset(60f,32f)){box(Color.White,44f,2f,35f,45f,2f);line(52f,16f,70f,16f,purple,2f)}}
        "shirt","dress","shawl"->{if(a=="shawl"){path(purple,8f,26f,112f,26f,60f,112f);repeat(8){i->line(14f+i*13,28f,19f+i*11,47f,peach,3f)}}else{path(c,35f,18f,12f,38f,23f,64f,36f,54f,if(a=="dress")21f else 36f,108f,if(a=="dress")100f else 85f,108f,85f,54f,98f,64f,109f,38f,85f,18f);drawArc(Color.White,0f,180f,false,Offset(44f,7f),Size(34f,28f),style=Stroke(7f))}}
        "boot","shoe","sneaker"->{if(a=="boot")box(brown,23f,11f,42f,82f,6f);box(if(a=="sneaker")purple else brown,19f,66f,91f,40f,13f);line(22f,104f,107f,104f,ink,5f);if(a=="sneaker")repeat(4){i->line(44f+i*10,68f,41f+i*10,83f,Color.White,3f)}}
        "cup","jug","vase","pot"->{val color=if(a=="pot")blue else c;box(color,25f,34f,69f,74f,14f);if(a=="cup"||a=="jug"){drawOval(color,Offset(78f,44f),Size(34f,41f),style=Stroke(9f));oval(Color(0xFFEFDFCB),28f,28f,64f,16f)};if(a=="pot"){box(purple,15f,31f,91f,11f,4f);box(purple,51f,22f,24f,11f,4f);line(13f,55f,26f,55f,purple,8f);line(93f,55f,108f,55f,purple,8f)};if(a=="vase"){line(60f,34f,60f,3f,green,4f);dot(rose,60f,9f,13f)}}
        "spoon"->{oval(blue,36f,3f,48f,53f);box(blue,53f,43f,15f,73f,7f)}
        "clock"->{dot(purple,60f,61f,48f);dot(Color.White,60f,61f,40f);repeat(12){i->val t=i*PI/6;dot(ink,60f+(cos(t)*33).toFloat(),61f+(sin(t)*33).toFloat(),2f)};line(60f,61f,60f,32f,ink,4f);line(60f,61f,82f,69f,ink,4f);dot(peach,60f,61f,5f)}
        "fan"->{box(purple,54f,62f,12f,43f);box(purple,30f,103f,60f,9f,4f);drawCircle(blue,43f,Offset(60f,49f),style=Stroke(4f));repeat(3){i->rotate(i*120f,Offset(60f,49f)){oval(mint,55f,13f,21f,40f)}};dot(purple,60f,49f,9f)}
        "hydrant"->{box(rose,36f,26f,47f,84f,12f);box(rose,11f,53f,98f,20f,5f);oval(peach,38f,13f,43f,29f);dot(yellow,59f,63f,12f);line(32f,108f,88f,108f,ink,5f)}
        "bell"->{val p=Path().apply{moveTo(17f,90f);quadraticTo(33f,69f,32f,44f);cubicTo(32f,6f,89f,6f,89f,44f);quadraticTo(86f,69f,105f,90f);close()};drawPath(p,yellow);dot(brown,61f,100f,9f);dot(brown,61f,13f,6f)}
        "shampoo"->{box(blue,28f,30f,64f,81f,10f);box(purple,40f,14f,40f,19f,3f);box(Color.White,38f,53f,44f,35f,6f);dot(mint,60f,70f,11f)}
        "tooth"->{val p=Path().apply{moveTo(31f,14f);cubicTo(0f,32f,28f,113f,41f,114f);cubicTo(56f,112f,52f,68f,61f,70f);cubicTo(74f,68f,64f,114f,80f,114f);cubicTo(94f,114f,117f,24f,90f,14f);quadraticTo(74f,4f,61f,17f);quadraticTo(45f,4f,31f,14f);close()};drawPath(p,Color(0xFFE0F1FA));smile(61f,40f)}
        "eye"->{val p=Path().apply{moveTo(6f,61f);quadraticTo(60f,2f,114f,61f);quadraticTo(60f,116f,6f,61f);close()};drawPath(p,Color.White);drawPath(p,ink,style=Stroke(3f));dot(blue,60f,61f,27f);dot(ink,60f,61f,14f);dot(Color.White,66f,52f,5f)}
        "nose"->{val p=Path().apply{moveTo(60f,16f);lineTo(37f,82f);quadraticTo(59f,105f,84f,82f);lineTo(72f,57f)};drawPath(p,peach,style=Stroke(18f,cap=StrokeCap.Round));dot(brown,48f,85f,3f);dot(brown,72f,85f,3f)}
        "ear"->{oval(peach,28f,8f,66f,105f);val p=Path().apply{moveTo(44f,72f);cubicTo(18f,15f,98f,13f,75f,62f);quadraticTo(44f,59f,56f,87f)};drawPath(p,brown,style=Stroke(5f,cap=StrokeCap.Round))}
        "hand"->{box(peach,29f,51f,65f,59f,24f);repeat(4){i->box(peach,29f+i*17,14f-(if(i==1||i==2)10f else 0f),14f,65f,7f)};rotate(-38f,Offset(29f,76f)){box(peach,18f,50f,20f,43f,10f)}}
        "yarn"->{dot(purple,60f,61f,43f);repeat(5){i->drawArc(peach,120f+i*11,210f,false,Offset(23f+i*5,24f+i*3),Size(65f-i*5,73f-i*4),style=Stroke(3f))};line(69f,101f,106f,114f,purple,3f)}
        "nest"->{oval(brown,9f,56f,104f,54f);repeat(5){i->line(14f,74f+i*6,107f,72f+i*6,yellow,2f)};oval(Color.White,35f,31f,23f,43f);oval(Color(0xFFECE2FE),63f,36f,24f,37f)}
        "robot"->{box(purple,24f,18f,73f,55f,11f);box(blue,28f,79f,65f,36f);line(61f,18f,61f,6f,ink,3f);dot(yellow,61f,5f,5f);dot(yellow,42f,43f,9f);dot(yellow,79f,43f,9f);line(43f,61f,79f,61f,Color.White,4f)}
        "dice"->{box(purple,15f,16f,91f,92f,16f);listOf(Offset(36f,37f),Offset(84f,37f),Offset(60f,62f),Offset(36f,87f),Offset(84f,87f)).forEach{dot(Color.White,it.x,it.y,7f)}}
        "domino"->{box(blue,32f,5f,57f,110f,8f);line(36f,60f,85f,60f,ink,3f);repeat(3){i->dot(ink,45f+i*16,22f+i*13,5f)};dot(ink,61f,87f,5f)}
        "puzzle"->{box(purple,12f,15f,96f,93f,13f);line(60f,16f,60f,107f,Color.White,4f);line(13f,62f,108f,62f,Color.White,4f);dot(peach,60f,40f,11f);dot(mint,38f,62f,11f);dot(yellow,82f,62f,11f)}
        "chess"->{repeat(4){r->repeat(4){c2->box(if((r+c2)%2==0)Color.White else purple,12f+c2*24,13f+r*24,24f,24f,0f)}};dot(ink,60f,47f,13f);path(ink,53f,55f,67f,55f,78f,91f,42f,91f)}
        "square"->box(purple,15f,15f,91f,91f,1f)
        "four"->{repeat(2){r->repeat(2){col->dot(listOf(rose,yellow,mint,blue)[r*2+col],35f+col*50,35f+r*50,21f)}}}
        "zero"->drawOval(purple,Offset(34f,8f),Size(52f,104f),style=Stroke(13f))
        "number"->{line(32f,19f,92f,19f,purple,12f);line(92f,19f,49f,108f,purple,12f)}
        "zigzag"->{val p=Path().apply{moveTo(8f,31f);lineTo(31f,79f);lineTo(55f,31f);lineTo(79f,79f);lineTo(110f,31f)};drawPath(p,purple,style=Stroke(10f,cap=StrokeCap.Round))}
        "wheel"->{dot(ink,60f,61f,48f);dot(blue,60f,61f,34f);repeat(6){i->val t=i*PI/3;line(60f,61f,60f+(cos(t)*32).toFloat(),61f+(sin(t)*32).toFloat(),Color.White,4f)};dot(ink,60f,61f,9f)}
        "candle"->{box(purple,37f,48f,47f,64f,3f);oval(yellow,48f,4f,25f,38f);line(60f,42f,60f,50f,ink,3f)}
        "zoo"->{box(mint,5f,7f,110f,105f,14f);translate(8f,20f){scale(.48f,.48f,Offset.Zero){animal("lion",yellow)}};translate(57f,26f){scale(.45f,.45f,Offset.Zero){bird("owl")}};line(11f,104f,110f,104f,green,6f)}
    }
}
