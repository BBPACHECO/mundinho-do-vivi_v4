package com.mundodedescobertas.app.painting

import org.junit.Assert.*
import org.junit.Test

class PaintPixelsTest {
    private val white=0xFFFFFFFF.toInt()
    private val black=0xFF000000.toInt()
    private val red=0xFFFF3333.toInt()
    @Test fun `catalog has distinct drawings in all eight requested categories`() {
        assertTrue(DrawingCatalog.originals.size >= 20)
        assertEquals(8,DrawingCatalog.originals.map{it.category}.distinct().size)
        assertEquals(DrawingCatalog.originals.size,DrawingCatalog.originals.map{it.id}.distinct().size)
    }
    @Test fun `fill never crosses the original closed contour`() {
        val base=IntArray(49){white}
        for(y in 1..5)for(x in 1..5)if(x==1 || x==5 || y==1 || y==5)base[y*7+x]=black
        val pixels=base.copyOf()
        assertEquals(9,PaintPixels.fill(pixels,base,7,7,3,3,red))
        assertEquals(white,pixels[0]);assertEquals(black,pixels[1*7+3]);assertEquals(red,pixels[3*7+3])
    }
    @Test fun `fill on outline or outside raster changes nothing`() {
        val base=intArrayOf(black,white,white,white);val pixels=base.copyOf()
        assertEquals(0,PaintPixels.fill(pixels,base,2,2,0,0,red))
        assertEquals(0,PaintPixels.fill(pixels,base,2,2,-1,1,red))
        assertArrayEquals(base,pixels)
    }
    @Test fun `touching an already colored region recolors only that region`() {
        val base=IntArray(25){white};for(y in 0..4)base[y*5+2]=black
        val pixels=base.copyOf();PaintPixels.fill(pixels,base,5,5,0,0,red)
        val blue=0xFF3366FF.toInt();assertEquals(10,PaintPixels.fill(pixels,base,5,5,1,3,blue))
        assertEquals(blue,pixels[0]);assertEquals(white,pixels[4]);assertEquals(black,pixels[2])
    }
    @Test fun `new edit after undo invalidates redo and capacity remains bounded`() {
        val history=PixelHistory(2)
        history.beforeEdit(intArrayOf(1));history.beforeEdit(intArrayOf(2));history.beforeEdit(intArrayOf(3))
        assertArrayEquals(intArrayOf(3),history.undo(intArrayOf(4)))
        assertArrayEquals(intArrayOf(2),history.undo(intArrayOf(3)))
        assertFalse(history.canUndo)
        assertArrayEquals(intArrayOf(3),history.redo(intArrayOf(2)))
        history.beforeEdit(intArrayOf(3));assertFalse(history.canRedo)
    }
}
