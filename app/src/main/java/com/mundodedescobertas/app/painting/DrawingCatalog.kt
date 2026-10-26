package com.mundodedescobertas.app.painting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.*

data class ViviDrawing(val id: String, val title: String, val category: String, val kind: Int)

object DrawingCatalog {
    val originals = listOf(
        ViviDrawing("gatinho", "Gatinho de almofada", "Animais", 0), ViviDrawing("coelho", "Coelhinho no jardim", "Animais", 1), ViviDrawing("urso", "Ursinho aconchegante", "Animais", 2),
        ViviDrawing("dino", "Dino e seu ovo", "Dinossauros", 3), ViviDrawing("bronto", "Dinossauro das nuvens", "Dinossauros", 4),
        ViviDrawing("flor", "Jardim de margaridas", "Natureza", 5), ViviDrawing("arvore", "Árvore dos passarinhos", "Natureza", 6), ViviDrawing("borboleta", "Borboleta no céu", "Natureza", 7),
        ViviDrawing("casa", "Casinha de biscoito", "Casinhas e cidades", 8), ViviDrawing("cidade", "Nossa pequena cidade", "Casinhas e cidades", 9), ViviDrawing("farol", "Farol na praia", "Casinhas e cidades", 10),
        ViviDrawing("foguete", "Foguete das estrelas", "Espaço", 11), ViviDrawing("planeta", "Planeta sorridente", "Espaço", 12), ViviDrawing("robo", "Robô da Lua", "Espaço", 13),
        ViviDrawing("castelo", "Castelo das nuvens", "Fantasia", 14), ViviDrawing("nuvino", "Nuvino e a estrela", "Fantasia", 15),
        ViviDrawing("carro", "Carrinho de passeio", "Veículos", 16), ViviDrawing("barco", "Barquinho tranquilo", "Veículos", 17), ViviDrawing("trem", "Trenzinho das cores", "Veículos", 18),
        ViviDrawing("vivi", "O jardim do Vivi", "Para o Vivi", 19), ViviDrawing("arco", "Arco-íris de carinho", "Para o Vivi", 20), ViviDrawing("presente", "Uma surpresa para o Vivi", "Para o Vivi", 21),
    )
    fun render(item: ViviDrawing, side: Int = 640): Bitmap {
        val bitmap = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap); c.drawColor(Color.WHITE); c.scale(side / 100f, side / 100f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(40, 42, 56); style = Paint.Style.STROKE; strokeWidth = 1.15f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        fun oval(x:Float,y:Float,w:Float,h:Float) = c.drawOval(RectF(x,y,x+w,y+h),p)
        fun circle(x:Float,y:Float,r:Float) = c.drawCircle(x,y,r,p)
        fun rect(x:Float,y:Float,w:Float,h:Float,r:Float=2f) = c.drawRoundRect(RectF(x,y,x+w,y+h),r,r,p)
        fun line(x:Float,y:Float,xx:Float,yy:Float) = c.drawLine(x,y,xx,yy,p)
        fun path(vararg xy:Float) { val q=Path();q.moveTo(xy[0],xy[1]); for(i in 2 until xy.size step 2)q.lineTo(xy[i],xy[i+1]);q.close();c.drawPath(q,p) }
        fun smile(x:Float,y:Float,s:Float=1f) {
            circle(x-5*s,y,1.2f*s);circle(x+5*s,y,1.2f*s)
            c.drawArc(RectF(x-4*s,y+2*s,x+4*s,y+8*s),0f,180f,false,p)
        }
        fun star(x:Float,y:Float,r:Float) {
            val q=Path();for(i in 0..9){val a=i*PI/5-PI/2; val rr=if(i%2==0)r else r*.46f;val xx=x+cos(a).toFloat()*rr;val yy=y+sin(a).toFloat()*rr;if(i==0)q.moveTo(xx,yy)else q.lineTo(xx,yy)};q.close();c.drawPath(q,p)
        }
        fun flower(x:Float,y:Float,r:Float=5f) { for(i in 0..5){val a=i*PI/3;circle(x+cos(a).toFloat()*r,y+sin(a).toFloat()*r,r*.58f)};circle(x,y,r*.6f);line(x,y+r,x,y+r*3);oval(x,y+r*1.5f,r*1.3f,r*.65f) }
        fun cloud(x:Float,y:Float,s:Float=1f) { val q=Path();q.moveTo(x,y+6*s);q.cubicTo(x-8*s,y+4*s,x-6*s,y-5*s,x,y-5*s);q.cubicTo(x+3*s,y-14*s,x+13*s,y-12*s,x+16*s,y-5*s);q.cubicTo(x+25*s,y-7*s,x+28*s,y+5*s,x+20*s,y+6*s);q.close();c.drawPath(q,p) }
        fun animal(type:Int) {
            oval(25f,44f,50f,39f);oval(29f,77f,15f,9f);oval(56f,77f,15f,9f)
            when(type){
                0->{path(29f,42f,30f,19f,46f,36f);path(54f,36f,70f,19f,72f,43f)}
                1->{oval(31f,10f,13f,35f);oval(56f,10f,13f,35f);oval(35f,16f,5f,21f);oval(60f,16f,5f,21f)}
                else->{circle(31f,32f,10f);circle(69f,32f,10f);circle(31f,32f,5f);circle(69f,32f,5f)}
            }
            oval(25f,31f,50f,39f);smile(50f,48f);oval(35f,68f,30f,12f)
            if(type==0){line(25f,52f,14f,49f);line(25f,56f,14f,58f);line(75f,52f,86f,49f);line(75f,56f,86f,58f)}
            flower(14f,75f,3f);flower(85f,78f,3f)
        }
        when(item.kind) {
            0,1,2 -> animal(item.kind)
            3,4 -> { oval(20f,49f,57f,30f);oval(59f,24f,22f,26f);rect(61f,39f,12f,24f,5f);oval(31f,71f,11f,15f);oval(57f,71f,11f,15f);path(22f,61f,7f,48f,13f,74f,27f,73f);smile(70f,34f,.7f);for(i in 0..3)path(24f+i*9,49f,28f+i*9,39f,33f+i*9,49f);oval(81f,67f,12f,18f);cloud(13f,21f,.7f) }
            5 -> { flower(50f,30f,12f);flower(19f,54f,7f);flower(80f,57f,7f);line(8f,89f,93f,89f);cloud(73f,16f,.5f) }
            6 -> { rect(44f,52f,13f,34f);circle(32f,40f,16f);circle(66f,39f,17f);circle(50f,23f,21f);circle(50f,50f,17f);smile(50f,38f);oval(67f,23f,8f,6f);path(74f,25f,81f,24f,76f,29f);flower(21f,81f,4f);flower(78f,81f,4f) }
            7 -> { oval(22f,21f,28f,34f);oval(50f,21f,28f,34f);oval(27f,54f,23f,24f);oval(50f,54f,23f,24f);oval(46f,30f,8f,49f);line(48f,31f,40f,18f);line(52f,31f,60f,18f);circle(34f,38f,6f);circle(66f,38f,6f);circle(38f,64f,5f);circle(62f,64f,5f);flower(16f,80f,3f) }
            8 -> { rect(24f,42f,53f,43f);path(17f,43f,50f,16f,84f,43f);rect(45f,61f,15f,24f,6f);rect(31f,51f,10f,12f);rect(63f,51f,10f,12f);circle(53f,74f,1f);rect(64f,18f,8f,19f);flower(14f,77f,3f);flower(88f,77f,3f) }
            9 -> { for(i in 0..2){val x=9f+i*29;rect(x,42f-i*6,24f,43f+i*6);path(x-3,42f-i*6,x+12,26f-i*6,x+27,42f-i*6);for(j in 0..1)for(k in 0..1)rect(x+4+j*11,49f+k*14,6f,8f,1f)};line(5f,89f,95f,89f);cloud(18f,18f,.7f) }
            10 -> { path(34f,82f,41f,31f,61f,31f,69f,82f);rect(36f,23f,30f,12f);path(32f,23f,51f,10f,70f,23f);line(39f,47f,64f,47f);line(37f,62f,66f,62f);rect(46f,68f,11f,14f);oval(71f,77f,19f,5f);line(8f,87f,92f,87f);cloud(12f,32f,.6f) }
            11 -> { oval(35f,16f,30f,56f);path(35f,49f,21f,71f,39f,66f);path(65f,49f,79f,71f,61f,66f);circle(50f,36f,9f);path(40f,70f,50f,92f,60f,70f);star(17f,22f,6f);star(80f,35f,7f);star(20f,52f,4f) }
            12 -> { circle(50f,50f,25f);oval(14f,39f,73f,24f);smile(50f,45f);circle(41f,61f,5f);star(17f,20f,7f);star(80f,20f,5f);star(79f,82f,7f);circle(19f,78f,7f) }
            13 -> { rect(26f,26f,48f,35f,8f);line(50f,14f,50f,26f);circle(50f,11f,3f);circle(39f,40f,5f);circle(61f,40f,5f);rect(41f,50f,18f,4f);rect(32f,62f,36f,21f);rect(23f,65f,8f,17f);rect(69f,65f,8f,17f);rect(35f,83f,10f,8f);rect(55f,83f,10f,8f);star(13f,23f,5f);star(85f,33f,6f) }
            14 -> { rect(21f,42f,58f,43f);rect(14f,29f,18f,56f);rect(68f,29f,18f,56f);path(10f,30f,23f,12f,36f,30f);path(64f,30f,77f,12f,90f,30f);rect(40f,60f,20f,25f,10f);rect(40f,43f,8f,10f);rect(54f,43f,8f,10f);star(50f,23f,8f) }
            15 -> { animal(1);star(85f,35f,10f);star(15f,30f,6f) }
            16 -> { rect(12f,48f,77f,29f,9f);path(25f,48f,36f,27f,65f,27f,78f,48f);path(34f,46f,40f,33f,49f,33f,49f,46f);path(54f,46f,54f,33f,62f,33f,70f,46f);circle(30f,77f,10f);circle(73f,77f,10f);circle(30f,77f,4f);circle(73f,77f,4f);smile(52f,60f,.8f);cloud(13f,17f,.6f) }
            17 -> { path(15f,66f,85f,66f,72f,83f,29f,83f);line(50f,17f,50f,65f);path(46f,24f,23f,59f,46f,59f);path(54f,20f,54f,59f,79f,59f);circle(36f,74f,3f);circle(50f,74f,3f);circle(64f,74f,3f);cloud(13f,23f,.7f);line(9f,89f,92f,89f) }
            18 -> { rect(9f,48f,28f,28f);rect(37f,48f,24f,28f);rect(63f,41f,28f,35f);rect(73f,31f,10f,10f);rect(15f,54f,13f,10f);rect(43f,54f,12f,10f);rect(68f,48f,15f,11f);listOf(19f,31f,45f,57f,72f,85f).forEach{circle(it,79f,5f)};line(5f,87f,96f,87f);cloud(74f,18f,.6f) }
            19 -> { for(i in 0..3)flower(17f+i*22,64f-(i%2)*12f,6f);cloud(14f,24f,.8f);circle(79f,24f,11f);smile(79f,21f,.5f);p.textSize=10f;p.textAlign=Paint.Align.CENTER;c.drawText("VIVI",50f,93f,p);star(50f,26f,8f) }
            20 -> { for(i in 0..4)c.drawArc(RectF(15f+i*5,17f+i*5,85f-i*5,87f-i*5),180f,180f,false,p);cloud(14f,57f,.8f);cloud(67f,57f,.8f);star(50f,84f,11f);star(19f,86f,5f);star(83f,85f,5f) }
            else -> { rect(21f,42f,58f,42f);rect(16f,34f,68f,12f);rect(45f,35f,11f,49f,0f);oval(27f,18f,24f,17f);oval(50f,18f,24f,17f);star(78f,17f,6f);star(18f,17f,6f);smile(50f,61f) }
        }
        return bitmap
    }
}
