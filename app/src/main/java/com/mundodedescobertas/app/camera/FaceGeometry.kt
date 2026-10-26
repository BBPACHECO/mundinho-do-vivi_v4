package com.mundodedescobertas.app.camera

import kotlin.math.*

/** Coordinates are pixels in the upright, already mirrored preview frame. */
data class FacePoint(val x: Float, val y: Float) {
    fun mix(other: FacePoint, amount: Float) = FacePoint(x + (other.x-x)*amount, y + (other.y-y)*amount)
}

data class FacePose(
    val center: FacePoint,
    val width: Float,
    val height: Float,
    val roll: Float,
    val leftEye: FacePoint?,
    val rightEye: FacePoint?,
    val nose: FacePoint?,
    val mouth: FacePoint?,
) {
    /** Inverse of the renderer's translate/rotate/non-uniform scale transform. */
    fun localPoint(point:FacePoint):FacePoint {
        if(width<=0f || height<=0f)return FacePoint(0f,0f)
        val angle=roll*PI.toFloat()/180f
        val dx=point.x-center.x;val dy=point.y-center.y
        return FacePoint((dx*cos(angle)+dy*sin(angle))*200f/width,(-dx*sin(angle)+dy*cos(angle))*250f/height)
    }
    fun smoothed(next: FacePose): FacePose {
        // A large jump probably indicates a new detection; do not drag an old mask across the frame.
        if (hypot(next.center.x-center.x,next.center.y-center.y) > width*.6f) return next
        val a=.58f
        fun smooth(old: FacePoint?, fresh: FacePoint?) = if(old!=null && fresh!=null) old.mix(fresh,a) else fresh
        var delta=(next.roll-roll)%360f
        if(delta>180f) delta-=360f
        if(delta < -180f) delta+=360f
        return FacePose(center.mix(next.center,a), width+(next.width-width)*a,height+(next.height-height)*a,
            roll+delta*a,smooth(leftEye,next.leftEye),smooth(rightEye,next.rightEye),smooth(nose,next.nose),smooth(mouth,next.mouth))
    }
}

enum class FaceFilter(val title: String, val deformation: Boolean = false) {
    DOG("Cachorrinho"), CAT("Gatinho"), RABBIT("Coelhinho"),
    NOSE("Nariz gigante",true), EYES("Olhos gigantes",true), CHEEKS("Bochechas",true),
    HEAD("Cabeça grandona",true), LONG("Rosto comprido",true),
    RAINBOW("Arco-íris"), GLASSES("Óculos divertidos"), CARTOON("Desenho animado"), MONSTER("Monstrinho fofo");
}

/** Forward deformation used by BitmapMesh. Outside the face the original frame remains unchanged. */
object FaceWarp {
    fun transform(point: FacePoint, pose: FacePose, filter: FaceFilter): FacePoint {
        if(!filter.deformation || pose.width<=0 || pose.height<=0) return point
        fun bulge(center: FacePoint, rx: Float, ry: Float, strengthX:Float, strengthY:Float): FacePoint {
            val radians=-pose.roll*PI.toFloat()/180f
            val dx=point.x-center.x; val dy=point.y-center.y
            val lx=dx*cos(radians)-dy*sin(radians); val ly=dx*sin(radians)+dy*cos(radians)
            val radius=sqrt((lx/rx).pow(2)+(ly/ry).pow(2))
            if(radius>=1f) return point
            val influence=(1f-radius).pow(2)
            val wx=lx*strengthX*influence; val wy=ly*strengthY*influence
            val back=-radians
            return FacePoint(point.x+wx*cos(back)-wy*sin(back),point.y+wx*sin(back)+wy*cos(back))
        }
        val w=pose.width;val h=pose.height
        return when(filter) {
            FaceFilter.NOSE -> bulge(pose.nose ?: pose.center,w*.38f,h*.3f,1.8f,1.55f)
            FaceFilter.EYES -> {
                val l=pose.leftEye ?: FacePoint(pose.center.x-w*.19f,pose.center.y-h*.12f)
                val r=pose.rightEye ?: FacePoint(pose.center.x+w*.19f,pose.center.y-h*.12f)
                val a=bulge(l,w*.24f,h*.21f,1.35f,1.35f)
                if(a!=point) a else bulge(r,w*.24f,h*.21f,1.35f,1.35f)
            }
            FaceFilter.CHEEKS -> {
                val radians=pose.roll*PI.toFloat()/180f
                val side=if((point.x-pose.center.x)*cos(radians)+(point.y-pose.center.y)*sin(radians)<0) -1 else 1
                val cx=pose.center.x+side*w*.28f*cos(radians)-h*.12f*sin(radians)
                val cy=pose.center.y+side*w*.28f*sin(radians)+h*.12f*cos(radians)
                bulge(FacePoint(cx,cy),w*.33f,h*.28f,1.3f,.9f)
            }
            FaceFilter.HEAD -> bulge(pose.center,w*.85f,h*.85f,1.1f,1.05f)
            FaceFilter.LONG -> bulge(pose.center,w*.8f,h*.85f,-.15f,1.65f)
            else -> point
        }
    }
}
