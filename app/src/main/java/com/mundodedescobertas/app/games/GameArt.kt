package com.mundodedescobertas.app.games

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.mundodedescobertas.app.ui.theme.Ink
import kotlin.math.*

internal val gameColors=listOf(Color(0xFFEB9E96),Color(0xFF8EBDE3),Color(0xFF8DCDB1),Color(0xFFFFD580),Color(0xFFAB92CF),Color(0xFFF3B4CD))
internal fun DrawScope.label(text:String,x:Float,y:Float,size:Float=40f,color:Color=Ink) {
    drawContext.canvas.nativeCanvas.drawText(text,x,y,android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color=color.toArgb();textSize=size;typeface=android.graphics.Typeface.create("sans-serif-rounded",android.graphics.Typeface.BOLD)
        textAlign=android.graphics.Paint.Align.CENTER
    })
}
internal fun DrawScope.star(x:Float,y:Float,r:Float,color:Color=gameColors[3]) {
    val p=Path()
    repeat(10){i->val a=i*PI/5-PI/2;val radius=if(i%2==0)r else r*.46f
        val px=x+(cos(a)*radius).toFloat();val py=y+(sin(a)*radius).toFloat()
        if(i==0)p.moveTo(px,py) else p.lineTo(px,py)
    };p.close();drawPath(p,color)
}
internal fun DrawScope.cloud(x:Float,y:Float,r:Float=45f) {
    drawOval(Color.White.copy(alpha=.85f),Offset(x-r*1.8f,y-r*.2f),Size(r*3.6f,r))
    drawCircle(Color.White.copy(alpha=.85f),r*.8f,Offset(x-r*.5f,y));drawCircle(Color.White.copy(alpha=.85f),r,Offset(x+r*.4f,y-r*.2f))
}
internal fun DrawScope.bird(x:Float,y:Float,r:Float,wing:Float=0f) {
    drawOval(gameColors[3],Offset(x-r,y-r*.8f),Size(r*2,r*1.6f))
    drawOval(gameColors[0],Offset(x-r*.7f,y-wing*r*.3f),Size(r*.9f,r*.55f))
    drawCircle(Ink,r*.09f,Offset(x+r*.4f,y-r*.2f))
    drawPath(Path().apply{moveTo(x+r*.9f,y-r*.1f);lineTo(x+r*1.35f,y+r*.12f);lineTo(x+r*.85f,y+r*.3f);close()},gameColors[0])
}
internal fun DrawScope.character(x:Float,y:Float,r:Float,phase:Float=0f) {
    drawOval(Ink.copy(alpha=.1f),Offset(x-r*.8f,y+r*.9f),Size(r*1.6f,r*.3f))
    drawRoundRect(gameColors[4],Offset(x-r*.6f,y-r*.3f),Size(r*1.2f,r*1.3f),CornerRadius(r*.35f))
    drawCircle(Color(0xFFFFD9B8),r*.7f,Offset(x,y-r*.6f))
    drawArc(Ink,180f,190f,true,Offset(x-r*.73f,y-r*1.35f),Size(r*1.46f,r*1.0f))
    drawCircle(Ink,r*.065f,Offset(x-r*.24f,y-r*.57f));drawCircle(Ink,r*.065f,Offset(x+r*.24f,y-r*.57f))
    drawArc(Ink,0f,180f,false,Offset(x-r*.2f,y-r*.48f),Size(r*.4f,r*.2f),style=Stroke(r*.04f))
    drawLine(Ink,Offset(x-r*.25f,y+r*.65f),Offset(x-r*.4f+sin(phase)*r*.15f,y+r*1.1f),r*.22f,StrokeCap.Round)
    drawLine(Ink,Offset(x+r*.25f,y+r*.65f),Offset(x+r*.4f-sin(phase)*r*.15f,y+r*1.1f),r*.22f,StrokeCap.Round)
}
internal fun DrawScope.tree(x:Float,y:Float,r:Float) {
    drawRoundRect(Color(0xFFC9946C),Offset(x-r*.13f,y-r*.2f),Size(r*.26f,r*1.1f),CornerRadius(r*.08f))
    drawCircle(gameColors[2],r*.7f,Offset(x,y-r*.65f));drawCircle(Color(0xFF70B99A),r*.52f,Offset(x-r*.45f,y-r*.2f));drawCircle(Color(0xFF9AD6B7),r*.55f,Offset(x+r*.4f,y-r*.22f))
}
internal fun DrawScope.house(x:Float,y:Float,r:Float) {
    drawRoundRect(gameColors[3],Offset(x-r*.7f,y-r*.2f),Size(r*1.4f,r*1.1f),CornerRadius(r*.1f))
    drawPath(Path().apply{moveTo(x-r*.9f,y-r*.2f);lineTo(x,y-r*1.1f);lineTo(x+r*.9f,y-r*.2f);close()},gameColors[0])
    drawRoundRect(gameColors[4],Offset(x-r*.17f,y+r*.3f),Size(r*.34f,r*.6f),CornerRadius(r*.06f))
    drawRoundRect(Color.White,Offset(x-r*.53f,y+r*.05f),Size(r*.3f,r*.3f),CornerRadius(r*.04f))
}
internal fun DrawScope.flower(x:Float,y:Float,r:Float,color:Color=gameColors[5]) {
    drawLine(gameColors[2],Offset(x,y),Offset(x,y+r),r*.12f,StrokeCap.Round)
    repeat(5){i->val a=i*2*PI/5;drawCircle(color,r*.38f,Offset(x+cos(a).toFloat()*r*.45f,y+sin(a).toFloat()*r*.45f))}
    drawCircle(gameColors[3],r*.27f,Offset(x,y))
}
internal fun DrawScope.animal(kind:Int,x:Float,y:Float,r:Float) {
    val c=gameColors[kind.mod(gameColors.size)]
    if(kind.mod(3)==0) {
        drawOval(c,Offset(x-r*.7f,y-r*1.15f),Size(r*.4f,r*.9f));drawOval(c,Offset(x+r*.3f,y-r*1.15f),Size(r*.4f,r*.9f))
    } else {
        drawPath(Path().apply{moveTo(x-r*.85f,y-r*.5f);lineTo(x-r*.8f,y-r*1.0f);lineTo(x-r*.25f,y-r*.65f);close()},c)
        drawPath(Path().apply{moveTo(x+r*.85f,y-r*.5f);lineTo(x+r*.8f,y-r*1.0f);lineTo(x+r*.25f,y-r*.65f);close()},c)
    }
    drawOval(c,Offset(x-r,y-r*.7f),Size(r*2,r*1.5f));drawCircle(Ink,r*.075f,Offset(x-r*.3f,y-r*.1f));drawCircle(Ink,r*.075f,Offset(x+r*.3f,y-r*.1f))
    drawOval(Color.White.copy(alpha=.6f),Offset(x-r*.28f,y+r*.1f),Size(r*.56f,r*.42f));drawCircle(Ink,r*.07f,Offset(x,y+r*.16f))
}
internal fun DrawScope.shape(kind:Int,center:Offset,r:Float,color:Color,outline:Boolean=false) {
    val style=if(outline) Stroke(6f) else Fill
    when(kind.mod(4)) {
        0->drawCircle(color,r,center,style=style)
        1->drawRoundRect(color,Offset(center.x-r,center.y-r),Size(2*r,2*r),CornerRadius(r*.12f),style=style)
        2->{val p=Path().apply{moveTo(center.x,center.y-r);lineTo(center.x+r,center.y+r);lineTo(center.x-r,center.y+r);close()};drawPath(p,color,style=style)}
        else->{val p=Path();repeat(10){i->val a=i*PI/5-PI/2;val radius=if(i%2==0)r else r*.46f;val x=center.x+(cos(a)*radius).toFloat();val y=center.y+(sin(a)*radius).toFloat();if(i==0)p.moveTo(x,y) else p.lineTo(x,y)};p.close();drawPath(p,color,style=style)}
    }
}
internal fun DrawScope.puzzleScene() {
    drawRect(Color(0xFFDFF1FE))
    drawCircle(gameColors[3],75f,Offset(820f,140f));cloud(220f,170f,68f)
    drawOval(Color(0xFFB7DEC0),Offset(-160f,540f),Size(1300f,750f))
    tree(180f,600f,160f);house(610f,620f,180f);flower(340f,820f,60f);animal(2,830f,830f,80f)
}
