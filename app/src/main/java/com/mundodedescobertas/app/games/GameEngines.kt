package com.mundodedescobertas.app.games

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.mundodedescobertas.app.data.ChildPreferences
import com.mundodedescobertas.app.ui.theme.Ink
import kotlin.math.*
import kotlin.random.Random

internal abstract class PlayEngine {
    var message="Vamos brincar no seu ritmo."
    var feedback: (String?)->Unit = {}
    open val actions:List<String> get()=emptyList()
    open val animating:Boolean get()=false
    open val status:String get()=""
    open fun action(index:Int) {}
    open fun update(dt:Float) {}
    open fun tap(p:Offset) {}
    open fun down(p:Offset) {}
    open fun drag(p:Offset,delta:Offset) {}
    open fun release(p:Offset) {}
    abstract fun DrawScope.render()
    fun draw(scope:DrawScope) {with(scope){render()}}
}

internal class FlightGame(private val settings:ChildPreferences):PlayEngine() {
    override val animating:Boolean get()=true
    private var level=settings.difficulty.ordinal
    private var world=FlightWorld(level)
    private var time=0f
    override val status get()="Estrelas de voo: ${world.score} · ${listOf("Suave","Explorar","Desafio")[level]}"
    override val actions get()=listOf("Bater as asas","Nível: ${listOf("Suave","Explorar","Desafio")[level]}")
    init{message="Toque para voar. Passe pelos espaços entre as árvores."}
    override fun tap(p:Offset) {world.flap();if(world.score==0)message="Asas leves, um toque de cada vez."}
    override fun action(index:Int) {if(index==0)tap(Offset.Zero) else {level=(level+1)%3;world=FlightWorld(level)}}
    override fun update(dt:Float) {
        time+=dt
        val old=world.score;val collision=world.collided
        world.update(dt,settings.effectiveSpeed)
        if(world.score>old){feedback(null);message="Um espaço descoberto!"}
        if(!collision && world.collided)message="Vamos descansar as asas? Toque e tente de novo, sem pressa."
    }
    override fun DrawScope.render() {
        drawRect(Color(0xFFE3F2FA));drawCircle(gameColors[3],65f,Offset(840f,130f))
        cloud(200f-(time*15f)%1200,170f,60f);cloud(800f-(time*10f)%1200,300f,48f)
        drawOval(Color(0xFFBFE2C7),Offset(-300f,850f),Size(1600f,400f))
        world.obstacles.forEach {o->
            val x=o.x*1000;val top=(o.gapY-world.gap/2)*1000;val bottom=(o.gapY+world.gap/2)*1000
            drawRoundRect(Color(0xFF8CC7A5),Offset(x-45f,-100f),Size(90f,top+100f),CornerRadius(25f))
            drawRoundRect(Color(0xFF8CC7A5),Offset(x-45f,bottom),Size(90f,1100f-bottom),CornerRadius(25f))
            drawRoundRect(Color(0xFF6BAF8A),Offset(x-58f,top-35f),Size(116f,35f),CornerRadius(12f))
            drawRoundRect(Color(0xFF6BAF8A),Offset(x-58f,bottom),Size(116f,35f),CornerRadius(12f))
        }
        bird(220f,world.y*1000,38f,sin(time*9))
        if(world.resting)label("Toque para começar a voar",500f,760f,38f)
        if(world.collided)star(220f,world.y*1000-75f,22f,gameColors[4])
    }
}

internal class RunnerGame(private val settings:ChildPreferences):PlayEngine() {
    override val animating:Boolean get()=true
    private val world=RunnerWorld().apply{freeMode=settings.difficulty.ordinal==0 || settings.calmMode;maxObjects=settings.effectiveElements}
    private var swipe=Offset.Zero
    override val status get()="Estrelas: ${world.stars} · ${if(world.freeMode)"Modo livre" else "Aventura"}"
    override val actions get()=listOf("Esquerda","Pular","Direita",if(world.freeMode)"Modo livre: sim" else "Modo livre: não")
    init{message="Deslize para os lados para trocar de pista e para cima para pular."}
    override fun down(p:Offset){swipe=p}
    override fun release(p:Offset){val d=p-swipe;if(abs(d.x)>65f || abs(d.y)>65f){if(abs(d.x)>abs(d.y))world.move(if(d.x>0)1 else -1) else if(d.y<0)world.leap()}}
    override fun tap(p:Offset){world.leap()}
    override fun action(index:Int){when(index){0->world.move(-1);1->world.leap();2->world.move(1);3->world.freeMode=!world.freeMode}}
    override fun update(dt:Float) {
        val stars=world.stars;val bumps=world.gentleBumps
        world.update(dt,settings.effectiveSpeed)
        if(stars<world.stars){feedback(null);message="Uma estrela para sua coleção."}
        if(bumps<world.gentleBumps)message="Tudo bem! Podemos pular a próxima caixa."
    }
    override fun DrawScope.render() {
        drawRect(Color(0xFFDFEFFA));drawCircle(gameColors[3],70f,Offset(820f,130f));cloud(220f,180f,65f)
        drawRect(Color(0xFFBDE2C3),Offset(0f,320f),Size(1000f,680f))
        val road=Path().apply{moveTo(420f,280f);lineTo(580f,280f);lineTo(1000f,1000f);lineTo(0f,1000f);close()}
        drawPath(road,Color(0xFFE5D8C4))
        listOf(-.5f,.5f).forEach {lane->drawLine(Color.White.copy(alpha=.8f),Offset(500f+lane*110f,300f),Offset(500f+lane*650f,1000f),8f)}
        repeat(6){i->val p=((world.elapsed*.25f+i/6f)%1f);val y=300f+p*p*680f
            drawLine(Color.White.copy(alpha=.5f),Offset(500f-40f-p*300f,y),Offset(500f+40f+p*300f,y),3f+p*4f)}
        tree(100f,510f,90f);tree(920f,670f,130f);house(820f,350f,70f)
        world.objects.sortedBy{it.distance}.forEach{o->
            val d=o.distance;val y=310f+d*d*640f;val x=500f+o.lane*(65f+d*270f);val r=12f+d*44f
            if(o.star)star(x,y,r) else {
                drawRoundRect(gameColors[0],Offset(x-r,y-r),Size(r*2,r*1.7f),CornerRadius(r*.15f))
                drawLine(Color(0xFFC77970),Offset(x-r,y-r*.2f),Offset(x+r,y-r*.2f),r*.12f)
            }
        }
        character(500f+world.lane*305f,840f-world.jump*400f,66f,world.elapsed*10f)
    }
}

internal class BlocksGame(private val settings:ChildPreferences,initial:String):PlayEngine() {
    override val animating:Boolean get()=true
    val world=BlockWorld().apply{restore(initial)}
    private var phase=0f
    override val status get()="Seu mundo · ${world.objects.size} construções · ${listOf("Casa","Árvore","Flor","Bichinho","Bloco","Apagar")[world.selected]}"
    override val actions get()=listOf("Esquerda","Frente","Direita","Atrás","Pular","Construir: ${listOf("Casa","Árvore","Flor","Bicho","Bloco","Apagar")[world.selected]}")
    init{message="Escolha o que construir e toque no chão. Seu mundinho é salvo neste aparelho."}
    override fun tap(p:Offset){if(p.x !in 50f..950f || p.y !in 150f..870f)return;val x=((p.x-50f)/90f).toInt();val y=((p.y-150f)/90f).toInt();world.place(x,y);feedback(null)}
    override fun action(index:Int){when(index){0->world.walk(-1,0);1->world.walk(0,-1);2->world.walk(1,0);3->world.walk(0,1);4->world.jumping=1f;5->world.selected=(world.selected+1)%6}}
    override fun update(dt:Float){phase+=dt;world.jumping=(world.jumping-dt*1.6f).coerceAtLeast(0f)}
    override fun DrawScope.render() {
        drawRect(Color(0xFFEAF5EB));cloud(200f,45f,32f);cloud(760f,55f,26f)
        repeat(8){y->repeat(10){x->drawRoundRect(if((x+y)%2==0) Color(0xFFCDE7C9) else Color(0xFFC3DFBF),Offset(51f+x*90,151f+y*90),Size(87f,87f),CornerRadius(8f))}}
        world.objects.sortedBy{it.y}.forEach {o->val x=95f+o.x*90;val y=180f+o.y*90
            when(o.kind){0->house(x,y,39f);1->tree(x,y,40f);2->flower(x,y,25f);3->animal(2,x,y,28f);4->{drawRoundRect(gameColors[4],Offset(x-30,y-30),Size(60f,62f),CornerRadius(7f));drawLine(Color.White,Offset(x-22,y-10),Offset(x+22,y-10),5f)}}
        }
        character(95f+world.playerX*90,180f+world.playerY*90-world.jumping*55,32f,phase*4)
    }
}

private data class Bubble(var x:Float,var y:Float,val r:Float,val speed:Float,val phase:Float,val color:Int)
private data class Pop(val x:Float,val y:Float,val color:Int,var life:Float=1f)
internal class BubblesGame(private val settings:ChildPreferences):PlayEngine() {
    override val animating:Boolean get()=true
    private val random=Random.Default
    private val bubbles=MutableList(settings.effectiveElements.coerceIn(3,12)){newBubble(true)}
    private val pops=mutableListOf<Pop>()
    private var count=0;private var time=0f
    override val status get()="Bolhas descobertas: $count · Sem relógio"
    override val actions get()=listOf("Novas bolhas")
    init{message="Toque nas bolhas. Elas podem passear sem pressa."}
    private fun newBubble(anywhere:Boolean=false)=Bubble(random.nextFloat()*800+100,if(anywhere)random.nextFloat()*800+100 else 1100f,random.nextFloat()*40+45,random.nextFloat()*35+25,random.nextFloat()*6.28f,random.nextInt(gameColors.size))
    override fun action(index:Int){bubbles.indices.forEach{bubbles[it]=newBubble(true)}}
    override fun update(dt:Float){time+=dt
        bubbles.indices.forEach {i->val b=bubbles[i];b.y-=b.speed*dt*settings.effectiveSpeed;if(b.y< -120)bubbles[i]=newBubble()}
        pops.forEach{it.life-=dt*2};pops.removeAll{it.life<=0}
    }
    override fun tap(p:Offset) {
        val i=bubbles.indexOfLast{hypot(p.x-it.x-sin(time+it.phase)*20f*settings.effectiveAnimation,p.y-it.y)<it.r*1.2f}
        if(i>=0){val b=bubbles[i];if(!settings.reducedStimuli && !settings.calmMode)pops.add(Pop(b.x,b.y,b.color));bubbles[i]=newBubble();count++;feedback(null)}
    }
    override fun DrawScope.render() {
        drawRect(Color(0xFFE7F2FA));drawOval(Color.White.copy(alpha=.5f),Offset(-300f,700f),Size(1600f,700f))
        bubbles.forEach{b->val x=b.x+sin(time+b.phase)*20*settings.effectiveAnimation;val c=gameColors[b.color]
            drawCircle(c.copy(alpha=.27f),b.r,Offset(x,b.y));drawCircle(c.copy(alpha=.8f),b.r,Offset(x,b.y),style=Stroke(4f))
            drawArc(Color.White.copy(alpha=.9f),200f,80f,false,Offset(x-b.r*.65f,b.y-b.r*.65f),Size(b.r*1.3f,b.r*1.3f),style=Stroke(5f,cap=StrokeCap.Round))
        }
        pops.forEach{p->repeat(6){n->val a=n*PI/3;val d=(1-p.life)*80;drawCircle(gameColors[p.color].copy(alpha=p.life*.6f),7f*p.life,Offset(p.x+cos(a).toFloat()*d,p.y+sin(a).toFloat()*d))}}
    }
}

internal class MemoryGame(private val settings:ChildPreferences):PlayEngine() {
    private var pairs=listOf(4,6,8)[settings.difficulty.ordinal]
    private var deck=emptyList<Int>();private val matched=mutableSetOf<Int>();private val open=mutableListOf<Int>()
    private var flip=FloatArray(0);private var waiting=0f
    override val animating:Boolean get()=waiting>0 || deck.indices.any{i->abs(flip[i]-(if(i in matched || i in open)1f else 0f))>.002f}
    override val status get()="Pares: ${matched.size/2} de $pairs"
    override val actions get()=listOf("Peças: ${pairs*2}")
    init{shuffle();message="Encontre duas figuras iguais. Podemos olhar com calma."}
    private fun shuffle(){deck=(0 until pairs).flatMap{listOf(it,it)}.shuffled();matched.clear();open.clear();flip=FloatArray(deck.size);waiting=0f}
    override fun action(index:Int){pairs=when(pairs){4->6;6->8;else->4};shuffle()}
    private val columns get()=4
    private val cell get()=900f/columns
    private val rows get()=deck.size/columns
    private fun origin(index:Int):Offset=Offset(50f+(index%columns)*cell, (1000f-rows*cell*.9f)/2+(index/columns)*cell*.9f)
    override fun tap(p:Offset) {
        if(waiting>0 || open.size>=2)return
        val i=deck.indices.firstOrNull{val o=origin(it);p.x in o.x..o.x+cell-12 && p.y in o.y..o.y+cell*.9f-12} ?: return
        if(i in matched || i in open)return
        open.add(i)
        if(open.size==2){if(deck[open[0]]==deck[open[1]]){matched.addAll(open);open.clear();feedback(null);message=if(matched.size==deck.size)"Todos os pares encontrados! Podemos jogar outra vez." else "Você encontrou um par."} else {waiting=1.1f;message="Vamos guardar essas figuras na memória."}}
    }
    override fun update(dt:Float) {if(waiting>0){waiting-=dt;if(waiting<=0)open.clear()};deck.indices.forEach{i->val target=if(i in matched || i in open)1f else 0f;flip[i]=if(settings.reducedStimuli || settings.effectiveAnimation==0f)target else flip[i]+(target-flip[i])*min(1f,dt*12f)}}
    override fun DrawScope.render() {
        drawRect(Color(0xFFFFF5E8))
        deck.indices.forEach{i->val o=origin(i);val c=Offset(o.x+(cell-12)/2,o.y+(cell*.9f-12)/2);val w=abs(cos(flip[i]*PI)).toFloat().coerceAtLeast(.04f)
            scale(w,1f,c){drawRoundRect(if(flip[i]>.5f) Color.White else gameColors[4],o,Size(cell-12,cell*.9f-12),CornerRadius(20f))
                if(flip[i]>.5f){val r=cell*.24f;when(deck[i]){0,1->animal(deck[i],c.x,c.y,r);2->{drawCircle(gameColors[0],r,c);drawLine(gameColors[2],Offset(c.x,c.y-r),Offset(c.x+r*.25f,c.y-r*1.4f),r*.12f,StrokeCap.Round)};3->flower(c.x,c.y,r);4->house(c.x,c.y,r);5->bird(c.x,c.y,r);6->tree(c.x,c.y,r);else->star(c.x,c.y,r)}}else star(c.x,c.y,cell*.16f,Color(0xFFE8DDF4))
                if(i in matched)drawRoundRect(gameColors[2],o,Size(cell-12,cell*.9f-12),CornerRadius(20f),style=Stroke(6f))
            }
        }
    }
}

internal class PuzzleGame(settings:ChildPreferences):PlayEngine() {
    private var n=listOf(2,3,4)[settings.difficulty.ordinal]
    private var slots=IntArray(0);private val locked=mutableSetOf<Int>();private var held:Int?=null
    private var heldPos=Offset.Zero;private var peek=0f
    override val animating:Boolean get()=peek>0
    private val side=920f
    private val cell get()=side/n
    override val status get()="Peças encaixadas: ${locked.size} de ${n*n}"
    override val actions get()=listOf("Ver desenho","Peças: ${n*n}")
    init{shuffle();message="Arraste cada pedaço para o lugar do desenho. Ele encaixa quando chega perto."}
    private fun shuffle(){slots=(0 until n*n).shuffled().toIntArray();locked.clear();held=null}
    private fun origin(slot:Int)=Offset(40f+(slot%n)*cell,40f+(slot/n)*cell)
    override fun action(index:Int){if(index==0)peek=2f else {n=if(n==4)2 else n+1;shuffle()}}
    override fun update(dt:Float){peek=(peek-dt).coerceAtLeast(0f)}
    override fun down(p:Offset){if(peek>0 || p.x<40f || p.y<40f || p.x>=960f || p.y>=960f)return;val s=((p.y-40)/cell).toInt()*n+((p.x-40)/cell).toInt();if(s in slots.indices && slots[s] !in locked){held=s;heldPos=origin(s)}}
    override fun drag(p:Offset,delta:Offset){if(held!=null)heldPos+=delta}
    override fun release(p:Offset) {
        val old=held ?: return;val piece=slots[old]
        val centers=(0 until n*n).map{val o=origin(it);Pair(o.x+cell/2,o.y+cell/2)}
        val target=snapSlot(heldPos.x+cell/2,heldPos.y+cell/2,centers,cell*.65f)
        if(target==piece){val other=slots[target];slots[target]=piece;slots[old]=other;locked.add(piece);feedback(null);message=if(locked.size==n*n)"Seu desenho ficou completo!" else "Encaixou, bem pertinho!"}
        held=null
    }
    private fun DrawScope.piece(id:Int,pos:Offset) {
        clipRect(pos.x,pos.y,pos.x+cell,pos.y+cell){translate(pos.x-(id%n)*cell,pos.y-(id/n)*cell){scale(side/1000,side/1000,Offset.Zero){puzzleScene()}}}
        drawRect(if(id in locked)gameColors[2] else Color.White,pos,Size(cell,cell),style=Stroke(if(id in locked)8f else 5f))
    }
    override fun DrawScope.render() {
        drawRect(Color(0xFFF1E9F6))
        if(peek>0){translate(40f,40f){scale(side/1000,side/1000,Offset.Zero){puzzleScene()}};return}
        slots.indices.filter{it!=held}.forEach{piece(slots[it],origin(it))}
        held?.let{piece(slots[it],heldPos)}
    }
}

internal class ShapesGame(settings:ChildPreferences):PlayEngine() {
    private val kinds=(0 until if(settings.difficulty.ordinal==0)3 else 4).toList()
    private var order=kinds.shuffled();private val placed=mutableSetOf<Int>()
    private var held:Int?=null;private var selected:Int?=null;private var pos=Offset.Zero
    override val status get()="Formas encaixadas: ${placed.size} de ${kinds.size}"
    override val actions get()=listOf("Novas formas")
    init{message="Arraste as formas até os contornos da mesma cor. Também pode tocar na forma e depois no contorno."}
    private fun source(k:Int)=Offset(130f+k*(740f/(kinds.size-1)),800f)
    private fun target(k:Int)=Offset(130f+order.indexOf(k)*(740f/(kinds.size-1)),350f)
    override fun action(index:Int){placed.clear();order=kinds.shuffled();held=null;selected=null}
    override fun down(p:Offset){held=kinds.firstOrNull{it !in placed && (p-source(it)).getDistance()<110f};held?.let{pos=source(it)}}
    override fun drag(p:Offset,delta:Offset){if(held!=null)pos+=delta}
    override fun release(p:Offset){held?.let{k->if((pos-target(k)).getDistance()<135f)complete(k)};held=null}
    override fun tap(p:Offset){val old=selected;if(old!=null && (p-target(old)).getDistance()<130){complete(old);selected=null}else selected=kinds.firstOrNull{it !in placed && (p-source(it)).getDistance()<120}}
    private fun complete(k:Int){placed.add(k);feedback(null);message=if(placed.size==kinds.size)"Todas as formas encontraram seu cantinho!"else"Essa forma encontrou seu lugar."}
    override fun DrawScope.render() {
        drawRect(Color(0xFFFFF5E7));drawRoundRect(Color.White.copy(alpha=.6f),Offset(30f,130f),Size(940f,440f),CornerRadius(40f))
        kinds.forEach{k->shape(k,target(k),88f,gameColors[k],outline=k !in placed);if(k !in placed && held!=k){shape(k,source(k),80f,gameColors[k]);if(selected==k)drawCircle(Ink.copy(alpha=.5f),102f,source(k),style=Stroke(4f))}}
        held?.let{shape(it,pos,80f,gameColors[it])}
    }
}

private data class CountingToken(val id:Int,val p:Offset,val star:Boolean)
internal class NumbersGame(private val settings:ChildPreferences):PlayEngine() {
    private var target=0;private var count=0;private var issued=0;private var nextId=0
    private val tokens=mutableListOf<CountingToken>();private val random=Random.Default
    override val status get()="Colete $target estrelas · Você já encontrou $count"
    override val actions get()=listOf(if(count==target)"Próximo desafio" else "Ouvir o desafio")
    init{newChallenge()}
    private fun newChallenge(){target=random.nextInt(1,listOf(6,11,16)[settings.difficulty.ordinal]);count=0;issued=0;tokens.clear();fill();message="Encontre $target estrelas. As flores podem descansar."}
    private fun fill(){val capacity=settings.effectiveElements.coerceIn(3,9)
        while(issued<target && tokens.count{it.star}<capacity-1){val positions=(0..7).map{Offset(170f+(it%3)*320f,320f+(it/3)*230f)}
            val position=positions.firstOrNull{p->tokens.none{it.star && it.p==p}} ?: break
            tokens.add(CountingToken(nextId++,position,true));issued++}
        if(tokens.none{!it.star})tokens.add(CountingToken(nextId++,Offset(820f,820f),false))
    }
    override fun action(index:Int){if(count==target)newChallenge() else feedback("Encontre $target estrelas.")}
    override fun tap(p:Offset){val token=tokens.firstOrNull{(p-it.p).getDistance()<90f} ?: return
        if(token.star){tokens.remove(token);count++;feedback(if(count==target)"Você encontrou $target estrelas!" else count.toString());fill();message=if(count==target)"Você completou a coleção. Vamos descobrir outro número?" else "Uma estrela de cada vez."}
        else message="Essa é uma flor. Estamos procurando estrelas."
    }
    override fun DrawScope.render() {
        drawRect(Color(0xFFEEF2FA));label(target.toString(),500f,150f,100f,gameColors[4]);label("$count de $target",500f,235f,44f)
        tokens.forEach{if(it.star)star(it.p.x,it.p.y,65f)else flower(it.p.x,it.p.y,55f)}
        if(count==target){animal(0,500f,560f,150f);repeat(min(target,8)){i->star(130f+i*100,840f,25f)}}
    }
}
