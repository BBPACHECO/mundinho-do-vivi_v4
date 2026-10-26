package com.mundodedescobertas.app.camera

import org.junit.Assert.*
import org.junit.Test

class FaceGeometryTest {
    private val pose=FacePose(FacePoint(200f,200f),200f,250f,0f,
        FacePoint(165f,170f),FacePoint(235f,170f),FacePoint(200f,220f),FacePoint(200f,260f))
    @Test fun outsideFaceRemainsUnchangedForEveryWarp() {
        val point=FacePoint(1000f,1000f)
        FaceFilter.entries.filter{it.deformation}.forEach { assertEquals(point,FaceWarp.transform(point,pose,it)) }
    }
    @Test fun accessoriesNeverDeformCameraPixels() {
        FaceFilter.entries.filterNot{it.deformation}.forEach {
            assertEquals(FacePoint(210f,220f),FaceWarp.transform(FacePoint(210f,220f),pose,it))
        }
    }
    @Test fun noseMagnifiesNeighbouringPixelsInsteadOfMovingNoseCenter() {
        assertEquals(pose.nose,FaceWarp.transform(pose.nose!!,pose,FaceFilter.NOSE))
        val neighbour=FacePoint(212f,220f)
        val warped=FaceWarp.transform(neighbour,pose,FaceFilter.NOSE)
        assertTrue(warped.x>neighbour.x)
        assertEquals(neighbour.y,warped.y,.001f)
    }
    @Test fun elongatedFaceMovesPixelsAlongItsLocalVerticalAxis() {
        val below=FacePoint(200f,240f)
        val transformed=FaceWarp.transform(below,pose,FaceFilter.LONG)
        assertTrue(transformed.y>below.y)
        val rotated=pose.copy(roll=90f)
        val beside=FacePoint(160f,200f)
        val transformedRotated=FaceWarp.transform(beside,rotated,FaceFilter.LONG)
        assertTrue(transformedRotated.x<beside.x)
        assertEquals(200f,transformedRotated.y,.001f)
    }
    @Test fun landmarkAnchorsAreTransformedBackIntoTheRotatedFaceFrame() {
        val local=pose.copy(roll=90f).localPoint(FacePoint(180f,230f))
        assertEquals(30f,local.x,.001f)
        assertEquals(20f,local.y,.001f)
    }
    @Test fun smoothingUsesShortestRotationAcrossBoundary() {
        val before=pose.copy(roll=179f)
        val after=before.smoothed(pose.copy(roll=-179f))
        assertTrue(after.roll in 179f..182f)
    }
    @Test fun aNewFaceDoesNotLeaveAnAccessorySlidingAcrossTheImage() {
        val next=pose.copy(center=FacePoint(900f,100f),roll=30f)
        assertEquals(next,pose.smoothed(next))
    }
}
