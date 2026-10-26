package com.mundodedescobertas.app.games

import kotlin.math.*
import kotlin.random.Random

/** Normalized simulation independent of frame rate, Android and rendering. */
data class FlightObstacle(var x: Float, val gapY: Float, var scored: Boolean = false)
class FlightWorld(val difficulty: Int, private val random: Random = Random.Default) {
    var y = .5f
    var velocity = 0f
    var score = 0
    var resting = true
    var collided = false
    val gap = listOf(.42f,.34f,.28f)[difficulty.coerceIn(0,2)]
    val obstacles = mutableListOf<FlightObstacle>()
    private var next = 0f
    fun flap() {
        if(collided) reset()
        resting=false; velocity=-.39f
    }
    fun reset() {y=.5f;velocity=0f;score=0;resting=true;collided=false;obstacles.clear();next=.65f}
    fun update(seconds: Float, speed: Float) {
        if(resting || collided) return
        val dt=seconds.coerceIn(0f,.04f)*speed.coerceIn(.2f,1.3f)
        velocity += .76f*dt; y += velocity*dt
        next-=dt
        if(next<=0f) {
            obstacles.add(FlightObstacle(1.12f,random.nextFloat()*.36f+.32f))
            next=listOf(2.35f,2.05f,1.8f)[difficulty.coerceIn(0,2)]
        }
        obstacles.forEach {
            it.x-=.24f*dt
            if(!it.scored && it.x+.055f<.22f) {it.scored=true;score++}
            if(abs(it.x-.22f)<.078f && (y-.028f<it.gapY-gap/2 || y+.028f>it.gapY+gap/2)) collided=true
        }
        obstacles.removeAll{it.x<-.2f}
        if(y<.04f || y>.94f) {collided=true;y=y.coerceIn(.04f,.94f)}
    }
}

data class RunnerObject(var distance: Float, val lane: Int, val star: Boolean)
class RunnerWorld(private val random: Random = Random.Default) {
    var lane=0
    var jump=0f
    var jumpVelocity=0f
    var stars=0
    var elapsed=0f
    var freeMode=true
    var maxObjects=8
    var gentleBumps=0
    val objects=mutableListOf<RunnerObject>()
    private var spawn=.2f
    fun move(direction:Int) {lane=(lane+direction).coerceIn(-1,1)}
    fun leap() {if(jump<=.001f) jumpVelocity=1.7f}
    fun update(seconds:Float,speed:Float) {
        val dt=seconds.coerceIn(0f,.04f)*speed.coerceIn(.2f,1.3f)
        elapsed+=dt
        jumpVelocity-=4.4f*dt;jump=(jump+jumpVelocity*dt).coerceAtLeast(0f)
        if(jump==0f)jumpVelocity=0f
        spawn-=dt
        if(spawn<=0f) {
            if(objects.size<maxObjects.coerceIn(1,12)) objects.add(RunnerObject(0f,random.nextInt(-1,2),random.nextFloat()>.42f))
            spawn=max(.65f,1.45f-elapsed*.005f)
        }
        objects.forEach {it.distance+=dt*(.24f+min(elapsed*.0006f,.12f))}
        val reached=objects.filter{it.distance>=.92f}
        reached.forEach {
            if(it.lane==lane) {
                if(it.star)stars++
                else if(jump<.1f) {gentleBumps++;if(!freeMode) elapsed=max(0f,elapsed-2f)}
            }
        }
        objects.removeAll{it.distance>=.92f}
    }
}

data class WorldObject(val x:Int,val y:Int,val kind:Int)
class BlockWorld {
    var playerX=4
    var playerY=5
    var jumping=0f
    var selected=0
    var dirty=false
    val objects=mutableListOf(WorldObject(1,1,1),WorldObject(7,1,1),WorldObject(3,2,2),WorldObject(6,3,3))
    fun walk(dx:Int,dy:Int) {playerX=(playerX+dx).coerceIn(0,9);playerY=(playerY+dy).coerceIn(0,7);dirty=true}
    fun place(x:Int,y:Int) {
        if(x !in 0..9 || y !in 0..7) return
        objects.removeAll{it.x==x && it.y==y}
        if(selected<5) objects.add(WorldObject(x,y,selected))
        dirty=true
    }
    fun snapshot():String=buildString {
        append("VIVI1\n$playerX,$playerY\n")
        objects.forEach{append("${it.x},${it.y},${it.kind}\n")}
    }
    fun restore(value:String) {
        val lines=value.lines()
        if(lines.firstOrNull()!="VIVI1" || lines.size>84)return
        val playerFields=lines.getOrNull(1)?.split(',') ?: return
        if(playerFields.size!=2)return
        val player=playerFields.map{it.toIntOrNull() ?: return}
        if(player.size!=2 || player[0] !in 0..9 || player[1] !in 0..7)return
        val decoded=mutableListOf<WorldObject>()
        for(line in lines.drop(2).filter{it.isNotBlank()}) {
            val fields=line.split(',');if(fields.size!=3)return
            val v=fields.map{it.toIntOrNull() ?: return}
            if(v.size!=3 || v[0] !in 0..9 || v[1] !in 0..7 || v[2] !in 0..4)return
            if(decoded.any{it.x==v[0] && it.y==v[1]})return
            decoded.add(WorldObject(v[0],v[1],v[2]))
        }
        playerX=player[0];playerY=player[1];objects.clear();objects.addAll(decoded);dirty=false
    }
}

/** Returns target slot only inside the generous, scale-independent snap radius. */
fun snapSlot(x:Float,y:Float,targets:List<Pair<Float,Float>>,radius:Float):Int? =
    targets.indices.minByOrNull{hypot(x-targets[it].first,y-targets[it].second)}
        ?.takeIf{hypot(x-targets[it].first,y-targets[it].second)<=radius}
