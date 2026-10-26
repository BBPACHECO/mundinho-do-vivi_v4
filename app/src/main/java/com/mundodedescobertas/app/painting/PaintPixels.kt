package com.mundodedescobertas.app.painting

/** Raster algorithms kept independent of Android for deterministic tests. */
object PaintPixels {
    fun fill(pixels: IntArray, boundaries: IntArray, width: Int, height: Int, x: Int, y: Int, color: Int): Int {
        require(width > 0 && height > 0 && pixels.size == width * height && boundaries.size == pixels.size)
        if (x !in 0 until width || y !in 0 until height) return 0
        val start = y * width + x
        val target = pixels[start]
        if (target == color || isBoundary(boundaries[start])) return 0
        val queued = BooleanArray(pixels.size)
        val queue = IntArray(pixels.size)
        var head = 0; var tail = 1; var changed = 0
        queue[0] = start; queued[start] = true
        while (head < tail) {
            val at = queue[head++]
            if (isBoundary(boundaries[at]) || !similar(pixels[at], target)) continue
            pixels[at] = color; changed++
            val px = at % width
            fun offer(next: Int) { if (!queued[next]) { queued[next] = true; queue[tail++] = next } }
            if (px > 0) offer(at - 1)
            if (px + 1 < width) offer(at + 1)
            if (at >= width) offer(at - width)
            if (at + width < pixels.size) offer(at + width)
        }
        return changed
    }
    private fun isBoundary(color: Int): Boolean = ((color shr 16 and 255) + (color shr 8 and 255) + (color and 255)) < 300
    private fun similar(a: Int, b: Int): Boolean = kotlin.math.abs((a shr 16 and 255) - (b shr 16 and 255)) <= 12 &&
        kotlin.math.abs((a shr 8 and 255) - (b shr 8 and 255)) <= 12 && kotlin.math.abs((a and 255) - (b and 255)) <= 12
}

/** A bounded undo stack; applying an edit after undo invalidates the redo branch. */
class PixelHistory(private val capacity: Int = 10) {
    private val undo = ArrayDeque<IntArray>()
    private val redo = ArrayDeque<IntArray>()
    val canUndo get() = undo.isNotEmpty()
    val canRedo get() = redo.isNotEmpty()
    fun beforeEdit(pixels: IntArray) {
        undo.addLast(pixels.copyOf())
        while (undo.size > capacity) undo.removeFirst()
        redo.clear()
    }
    fun undo(current: IntArray): IntArray? {
        if (undo.isEmpty()) return null
        redo.addLast(current.copyOf()); return undo.removeLast()
    }
    fun redo(current: IntArray): IntArray? {
        if (redo.isEmpty()) return null
        undo.addLast(current.copyOf()); return redo.removeLast()
    }
}
