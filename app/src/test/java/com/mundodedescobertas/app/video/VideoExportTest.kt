package com.mundodedescobertas.app.video

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(UnstableApi::class)
class VideoExportTest {
    @Test fun `HD export respects portrait and landscape without upscaling normal small inputs`() {
        assertEquals(1280 to 720, VideoExportGeometry.outputSize(3840, 2160))
        assertEquals(720 to 1280, VideoExportGeometry.outputSize(2160, 3840))
        assertEquals(640 to 480, VideoExportGeometry.outputSize(640, 480))
        val (width, height) = VideoExportGeometry.outputSize(4031, 1701)
        assertTrue(width <= 1280 && height <= 720)
        assertEquals(0, width % 2)
        assertEquals(0, height % 2)
    }

    @Test fun `reaction is visible at its start and disappears exactly at the end`() {
        val reaction = VideoReaction(File("reaction.mp4"), durationMs = 3000, startMs = 2000)
        assertFalse(VideoExportGeometry.reactionVisibleAt(reaction, 1999))
        assertTrue(VideoExportGeometry.reactionVisibleAt(reaction, 2000))
        assertTrue(VideoExportGeometry.reactionVisibleAt(reaction, 4999))
        assertFalse(VideoExportGeometry.reactionVisibleAt(reaction, 5000))
    }

    @Test fun `robot effect preserves stereo frame timing across arbitrary input buffer boundaries`() {
        val frames = 4800
        val input = ShortArray(frames * 2) { 10000 }
        val whole = render(input, frames, robot = true, volume = 1f)
        val partitioned = render(input, 17, robot = true, volume = 1f)
        assertArrayEquals(whole, partitioned)
        assertEquals(input.size, whole.size)
        assertTrue(whole.any { it < 0 })
        for (frame in 0 until frames) assertEquals(whole[frame * 2], whole[frame * 2 + 1])
    }

    @Test fun `PCM gain preserves signed samples and muting emits silent frames`() {
        val input = shortArrayOf(-30000, 30000, -12000, 12000, 0, 0)
        assertArrayEquals(shortArrayOf(-15000, 15000, -6000, 6000, 0, 0), render(input, 3, false, .5f))
        assertArrayEquals(ShortArray(input.size), render(input, 1, true, 0f))
    }

    private fun render(input: ShortArray, framesPerBuffer: Int, robot: Boolean, volume: Float): ShortArray {
        val processor = VideoPcmEffect(volume, robot)
        processor.configure(AudioProcessor.AudioFormat(48000, 2, C.ENCODING_PCM_16BIT))
        processor.flush()
        val result = ArrayList<Short>()
        var position = 0
        while (position < input.size) {
            val count = minOf(input.size - position, framesPerBuffer * 2)
            val buffer = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder())
            repeat(count) { buffer.putShort(input[position++]) }
            buffer.flip()
            processor.queueInput(buffer)
            val output = processor.output.order(ByteOrder.nativeOrder())
            while (output.hasRemaining()) result += output.short
        }
        processor.queueEndOfStream()
        assertTrue(processor.isEnded)
        processor.reset()
        return result.toShortArray()
    }
}
