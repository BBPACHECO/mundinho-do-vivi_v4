package com.mundodedescobertas.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.*
import com.mundodedescobertas.app.ui.theme.Ink
import kotlin.math.*

enum class Scene { ATELIER, DJ, DISCOVER, GAMES, CAMERA, MASCOT }

/** Ilustrações vetoriais originais. Sem bitmaps externos, emojis ou dependências de rede. */
@Composable
fun Illustration(scene: Scene, modifier: Modifier = Modifier, happy: Boolean = false) {
    Canvas(modifier) {
        val unit = min(size.width, size.height) / 120f
        translate((size.width - 120 * unit) / 2, (size.height - 120 * unit) / 2) {
            scale(unit, unit, Offset.Zero) {
                when (scene) {
                    Scene.MASCOT -> mascot(happy)
                    Scene.ATELIER -> atelier()
                    Scene.DJ -> dj()
                    Scene.DISCOVER -> discover()
                    Scene.GAMES -> games()
                    Scene.CAMERA -> camera()
                }
            }
        }
    }
}
private val Purple = Color(0xFF8C77C0)
private val Peach = Color(0xFFFFAD94)
private val Mint = Color(0xFF8FCDB2)
private val Yellow = Color(0xFFFFD880)
private val Blue = Color(0xFF8EBDE3)
private fun DrawScope.line(a: Offset, b: Offset, color: Color = Ink, width: Float = 3f) = drawLine(color, a, b, width, StrokeCap.Round)
private fun DrawScope.face(x: Float, y: Float, gap: Float = 11f, happy: Boolean = false) {
    if (happy) {
        drawArc(Ink, 195f, 150f, false, Offset(x-gap-3,y-2), Size(7f,6f), style=Stroke(2.5f, cap=StrokeCap.Round))
        drawArc(Ink, 195f, 150f, false, Offset(x+gap-3,y-2), Size(7f,6f), style=Stroke(2.5f, cap=StrokeCap.Round))
    } else {
        drawOval(Ink, Offset(x-gap-2,y-3), Size(4f,6f))
        drawOval(Ink, Offset(x+gap-2,y-3), Size(4f,6f))
    }
    drawArc(Ink, 10f, 160f, false, Offset(x-5,y+2), Size(10f,8f), style=Stroke(2.3f,cap=StrokeCap.Round))
    drawOval(Peach.copy(alpha=.7f),Offset(x-gap-9,y+4),Size(10f,5f))
    drawOval(Peach.copy(alpha=.7f),Offset(x+gap,y+4),Size(10f,5f))
}
private fun DrawScope.star(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    for (i in 0..9) {
        val a = i * PI / 5 - PI/2
        val r = if (i%2==0) radius else radius*.48f
        val x = cx+(cos(a)*r).toFloat(); val y=cy+(sin(a)*r).toFloat()
        if(i==0) path.moveTo(x,y) else path.lineTo(x,y)
    }
    path.close(); drawPath(path,color)
}
private fun DrawScope.mascot(happy: Boolean) {
    drawOval(Purple.copy(alpha=.12f),Offset(22f,105f),Size(78f,9f))
    // Nuvino: uma nuvem-coelhinho com orelhas lilás, botinhas e uma estrela.
    rotate(-16f,Offset(37f,37f)) { drawRoundRect(Purple, Offset(27f,5f),Size(18f,48f),CornerRadius(10f)) }
    rotate(17f,Offset(79f,37f)) { drawRoundRect(Purple, Offset(72f,3f),Size(18f,50f),CornerRadius(10f)) }
    drawRoundRect(Color(0xFFE7DCF8),Offset(31f,16f),Size(8f,26f),CornerRadius(5f))
    drawRoundRect(Color(0xFFE7DCF8),Offset(78f,15f),Size(8f,27f),CornerRadius(5f))
    drawOval(Purple,Offset(15f,44f),Size(91f,62f))
    drawCircle(Purple,27f,Offset(43f,55f)); drawCircle(Purple,25f,Offset(77f,54f))
    drawOval(Color(0xFFF6EDFF),Offset(23f,45f),Size(75f,54f))
    drawOval(Purple,Offset(28f,95f),Size(23f,14f));drawOval(Purple,Offset(72f,95f),Size(23f,14f))
    drawOval(Purple,Offset(8f,69f),Size(19f,15f));drawOval(Purple,Offset(95f,69f),Size(19f,15f))
    face(60f,65f,14f,happy)
    star(97f,37f,11f,Yellow); star(16f,37f,6f,Peach)
}
private fun DrawScope.atelier() {
    drawOval(Color.White,Offset(12f,24f),Size(94f,77f))
    drawOval(Yellow,Offset(6f,18f),Size(94f,77f))
    drawCircle(Color(0xFFFFEBD0),10f,Offset(76f,72f))
    listOf(Peach,Mint,Blue,Purple).forEachIndexed { i,c -> drawCircle(c,9f,Offset(25f+i*19,40f+if(i%2==0) 0f else -8f)) }
    rotate(32f,Offset(81f,76f)) {
        drawRoundRect(Purple,Offset(76f,46f),Size(10f,58f),CornerRadius(5f))
        drawRoundRect(Color(0xFFDCD7E5),Offset(76f,41f),Size(10f,17f),CornerRadius(2f))
        val tip=Path().apply{moveTo(76f,42f);quadraticTo(72f,20f,82f,17f);quadraticTo(94f,32f,86f,42f);close()}
        drawPath(tip,Peach)
    }
    face(43f,65f,9f)
}
private fun DrawScope.dj() {
    drawRoundRect(Purple,Offset(8f,38f),Size(104f,66f),CornerRadius(16f))
    drawRoundRect(Color(0xFF58486D),Offset(13f,42f),Size(94f,56f),CornerRadius(12f))
    listOf(36f,85f).forEach{ x -> drawCircle(Ink,20f,Offset(x,68f));drawCircle(Blue,10f,Offset(x,68f));drawCircle(Yellow,3f,Offset(x,68f)) }
    line(Offset(55f,89f),Offset(69f,89f),Color.White,2f)
    drawCircle(Peach,4f,Offset(61f,89f))
    drawArc(Purple,180f,180f,false,Offset(33f,7f),Size(55f,61f),style=Stroke(8f,cap=StrokeCap.Round))
    drawRoundRect(Peach,Offset(29f,29f),Size(13f,23f),CornerRadius(5f));drawRoundRect(Peach,Offset(80f,29f),Size(13f,23f),CornerRadius(5f))
    star(104f,20f,7f,Yellow)
}
private fun DrawScope.discover() {
    rotate(-10f,Offset(40f,58f)) {
        drawRoundRect(Peach,Offset(12f,24f),Size(52f,69f),CornerRadius(12f))
        line(Offset(25f,76f),Offset(39f,43f),Color.White,6f);line(Offset(39f,43f),Offset(52f,76f),Color.White,6f)
        line(Offset(30f,65f),Offset(47f,65f),Color.White,5f)
    }
    rotate(12f,Offset(81f,70f)) {
        drawRoundRect(Mint,Offset(61f,44f),Size(47f,57f),CornerRadius(10f))
        line(Offset(77f,65f),Offset(84f,59f),Ink,5f);line(Offset(84f,59f),Offset(84f,84f),Ink,5f)
        line(Offset(76f,85f),Offset(92f,85f),Ink,5f)
    }
    star(89f,23f,11f,Yellow)
}
private fun DrawScope.games() {
    drawRoundRect(Mint,Offset(11f,37f),Size(98f,62f),CornerRadius(25f))
    drawRoundRect(Color(0xFFD9F2DF),Offset(18f,43f),Size(84f,45f),CornerRadius(20f))
    line(Offset(26f,63f),Offset(48f,63f),Ink,7f);line(Offset(37f,52f),Offset(37f,74f),Ink,7f)
    drawCircle(Purple,6f,Offset(81f,56f));drawCircle(Peach,6f,Offset(91f,69f))
    drawCircle(Yellow,5f,Offset(70f,70f))
    drawArc(Ink,0f,180f,false,Offset(50f,66f),Size(15f,9f),style=Stroke(2.5f))
    star(28f,22f,10f,Yellow);star(91f,23f,6f,Peach)
}
private fun DrawScope.camera() {
    drawRoundRect(Blue,Offset(10f,36f),Size(101f,66f),CornerRadius(17f))
    drawRoundRect(Blue,Offset(30f,24f),Size(34f,24f),CornerRadius(8f))
    drawCircle(Color(0xFFE9F6FF),27f,Offset(62f,67f));drawCircle(Purple,21f,Offset(62f,67f))
    drawCircle(Color(0xFFF2EDFF),16f,Offset(62f,67f));face(62f,64f,6f)
    drawCircle(Yellow,5f,Offset(96f,49f));star(96f,22f,9f,Peach)
    line(Offset(25f,50f),Offset(30f,50f),Color.White,3f)
}
