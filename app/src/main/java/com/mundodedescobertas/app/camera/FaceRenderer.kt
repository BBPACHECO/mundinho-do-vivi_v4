package com.mundodedescobertas.app.camera

import android.graphics.*
import kotlin.math.*

/** Draws the exact bitmap shown in the preview and later saved, including pixel deformation. */
internal object FaceRenderer {
    private val ink=Color.rgb(59,47,67)
    fun render(frame:Bitmap,pose:FacePose?,filter:FaceFilter):Bitmap {
        val result=Bitmap.createBitmap(frame.width,frame.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(result)
        val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        if(pose!=null && filter.deformation) {
            val columns=36;val rows=48
            val vertices=FloatArray((columns+1)*(rows+1)*2)
            var index=0
            for(y in 0..rows) for(x in 0..columns) {
                val warped=FaceWarp.transform(FacePoint(frame.width*x/columns.toFloat(),frame.height*y/rows.toFloat()),pose,filter)
                vertices[index++]=warped.x;vertices[index++]=warped.y
            }
            canvas.drawBitmapMesh(frame,columns,rows,vertices,0,null,0,paint)
        } else canvas.drawBitmap(frame,0f,0f,paint)
        if(pose!=null && !filter.deformation) accessories(canvas,pose,filter,paint)
        return result
    }

    private fun accessories(c:Canvas,p:FacePose,f:FaceFilter,paint:Paint) {
        // A face-local frame follows detector translation, size and the eyes' roll angle.
        c.save();c.translate(p.center.x,p.center.y);c.rotate(p.roll);c.scale(p.width/200f,p.height/250f)
        fun fill(color:Int){paint.color=color;paint.style=Paint.Style.FILL;paint.shader=null;paint.colorFilter=null}
        fun oval(x:Float,y:Float,w:Float,h:Float,color:Int){fill(color);c.drawOval(x,y,x+w,y+h,paint)}
        fun line(x:Float,y:Float,xx:Float,yy:Float,color:Int,width:Float=4f){fill(color);paint.strokeWidth=width;paint.strokeCap=Paint.Cap.ROUND;c.drawLine(x,y,xx,yy,paint)}
        fun triangle(x:Float,y:Float,w:Float,h:Float,color:Int) {
            fill(color);val path=Path().apply{moveTo(x,y);lineTo(x+w/2,y-h);lineTo(x+w,y);close()};c.drawPath(path,paint)
        }
        val pink=Color.rgb(255,166,177);val cream=Color.rgb(255,235,216)
        val nose=p.nose?.let(p::localPoint) ?: FacePoint(0f,20f)
        val mouth=p.mouth?.let(p::localPoint) ?: FacePoint(0f,62f)
        val eyeL=p.leftEye?.let(p::localPoint) ?: FacePoint(-38f,-30f)
        val eyeR=p.rightEye?.let(p::localPoint) ?: FacePoint(38f,-30f)
        when(f) {
            FaceFilter.DOG -> {
                c.save();c.rotate(-20f,-75f,-88f);oval(-115f,-145f,55f,122f,Color.rgb(153,100,70));oval(-104f,-125f,30f,84f,Color.rgb(211,154,125));c.restore()
                c.save();c.rotate(20f,75f,-88f);oval(60f,-145f,55f,122f,Color.rgb(153,100,70));oval(71f,-125f,30f,84f,Color.rgb(211,154,125));c.restore()
                c.save();c.translate(nose.x,nose.y-18f)
                oval(-48f,10f,96f,60f,cream);oval(-24f,4f,48f,28f,ink)
                line(0f,31f,0f,53f,ink,3f);line(-23f,55f,0f,49f,ink,3f);line(0f,49f,23f,55f,ink,3f)
                oval(-9f,54f,18f,(mouth.y-nose.y).coerceIn(22f,40f),pink);c.restore()
            }
            FaceFilter.CAT -> {
                triangle(-107f,-83f,68f,78f,Color.rgb(185,161,226));triangle(39f,-83f,68f,78f,Color.rgb(185,161,226))
                triangle(-95f,-91f,43f,49f,pink);triangle(52f,-91f,43f,49f,pink)
                c.save();c.translate(nose.x,nose.y-24f)
                triangle(-14f,31f,28f,18f,pink);line(0f,32f,0f,48f,ink,3f)
                for(i in 0..2){line(-38f,34f+i*10,-106f,18f+i*24,ink,3f);line(38f,34f+i*10,106f,18f+i*24,ink,3f)}
                oval(-64f,32f,22f,12f,Color.argb(130,255,143,163));oval(42f,32f,22f,12f,Color.argb(130,255,143,163));c.restore()
            }
            FaceFilter.RABBIT -> {
                c.save();c.rotate(-12f,-48f,-100f);oval(-76f,-242f,48f,170f,Color.WHITE);oval(-64f,-220f,24f,120f,pink);c.restore()
                c.save();c.rotate(12f,48f,-100f);oval(28f,-242f,48f,170f,Color.WHITE);oval(40f,-220f,24f,120f,pink);c.restore()
                c.save();c.translate(nose.x,nose.y-23f)
                oval(-36f,17f,72f,50f,Color.WHITE);oval(-12f,14f,24f,18f,pink)
                fill(Color.WHITE);c.drawRoundRect(-14f,54f,14f,75f,3f,3f,paint);line(0f,56f,0f,74f,Color.LTGRAY,2f);c.restore()
            }
            FaceFilter.RAINBOW -> {
                val colors=listOf(0xffffa5a5.toInt(),0xffffcf89.toInt(),0xffffe891.toInt(),0xffa5dcb7.toInt(),0xffa3c6ec.toInt(),0xffc1a6e5.toInt())
                colors.forEachIndexed{i,color->fill(color);paint.style=Paint.Style.STROKE;paint.strokeWidth=9f;c.drawArc(-75f+i*7,-124f+i*8,75f-i*7,-4f-i*2,190f,160f,false,paint)}
                oval(-88f,20f,38f,18f,Color.argb(160,255,164,182));oval(50f,20f,38f,18f,Color.argb(160,255,164,182))
                fill(Color.WHITE);star(c,-76f,-2f,10f,paint);star(c,80f,-4f,10f,paint)
            }
            FaceFilter.GLASSES -> {
                val lensWidth=((eyeR.x-eyeL.x)*.92f).coerceIn(48f,85f)
                val lensHeight=lensWidth*.78f
                fill(Color.rgb(133,103,190));paint.style=Paint.Style.STROKE;paint.strokeWidth=9f
                listOf(eyeL,eyeR).forEach{eye->c.drawRoundRect(eye.x-lensWidth/2,eye.y-lensHeight/2,eye.x+lensWidth/2,eye.y+lensHeight/2,18f,18f,paint)}
                line(eyeL.x+lensWidth/2,eyeL.y,eyeR.x-lensWidth/2,eyeR.y,Color.rgb(133,103,190),8f)
                line(eyeL.x-lensWidth/2,eyeL.y,-115f,eyeL.y-15f,Color.rgb(133,103,190),7f)
                line(eyeR.x+lensWidth/2,eyeR.y,115f,eyeR.y-15f,Color.rgb(133,103,190),7f)
                listOf(eyeL,eyeR).forEach{eye->line(eye.x-15f,eye.y-7f,eye.x+2f,eye.y-15f,Color.WHITE,5f)}
                fill(0xffffd879.toInt());star(c,eyeR.x+lensWidth/2+9f,eyeR.y-lensHeight/2-10f,14f,paint)
            }
            FaceFilter.CARTOON -> {
                fill(Color.argb(55,255,224,141));c.drawOval(-92f,-112f,92f,115f,paint)
                fill(ink);paint.style=Paint.Style.STROKE;paint.strokeWidth=3f;c.drawOval(-92f,-112f,92f,115f,paint)
                listOf(eyeL,eyeR).forEach{eye->
                    oval(eye.x-22f,eye.y-25f,44f,50f,Color.WHITE)
                    oval(eye.x-11f,eye.y-13f,22f,31f,ink)
                    oval(eye.x-7f,eye.y-9f,7f,10f,Color.WHITE)
                }
                oval(-78f,25f,31f,16f,pink);oval(47f,25f,31f,16f,pink)
                fill(ink);paint.style=Paint.Style.STROKE;paint.strokeWidth=4f;c.drawArc(mouth.x-33f,mouth.y-13f,mouth.x+33f,mouth.y+24f,0f,180f,false,paint)
            }
            FaceFilter.MONSTER -> {
                fill(Color.argb(165,127,201,163));c.drawOval(-103f,-118f,103f,118f,paint)
                triangle(-94f,-89f,40f,69f,0xffe5b4ef.toInt());triangle(54f,-89f,40f,69f,0xffe5b4ef.toInt())
                listOf(eyeL,eyeR,FacePoint(0f,-82f)).forEach{eye->
                    oval(eye.x-23f,eye.y-21f,46f,42f,Color.WHITE);oval(eye.x-9f,eye.y-12f,18f,25f,ink)
                }
                oval(mouth.x-42f,mouth.y-20f,84f,44f,ink)
                triangle(mouth.x-28f,mouth.y-17f,20f,-20f,Color.WHITE);triangle(mouth.x+8f,mouth.y-17f,20f,-20f,Color.WHITE)
                oval(-88f,10f,27f,19f,pink);oval(61f,10f,27f,19f,pink)
            }
            else -> Unit
        }
        c.restore()
    }
    private fun star(canvas:Canvas,cx:Float,cy:Float,radius:Float,paint:Paint) {
        val path=Path()
        for(i in 0..9){val angle=i*PI/5-PI/2;val r=if(i%2==0)radius else radius*.45f;val x=cx+cos(angle).toFloat()*r;val y=cy+sin(angle).toFloat()*r;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)}
        path.close();canvas.drawPath(path,paint)
    }
}
