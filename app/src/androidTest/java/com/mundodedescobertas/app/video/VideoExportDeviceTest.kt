package com.mundodedescobertas.app.video

import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteOrder
import kotlin.math.abs

/** Actual codec/GL tests, runnable on Android; merely compiling this class does not execute them. */
class VideoExportDeviceTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test fun exportBakesColorStickerSoundAndShortReactionWithoutTruncatingBackground() = runBlocking {
        val base = fixture("base-red.mp4")
        val reaction = fixture("reaction-blue.mp4")
        var output: File? = null
        try {
            val project = VideoProject(
                source = VideoSource(base, 2000, 320, 240, false),
                color = VideoColor.MONO,
                stickers = listOf(VideoSticker(VideoEmoji.HAPPY, sizeFraction = .28f, endMs = 2000)),
                sounds = listOf(VideoSound("elec_blip", "Blip", 400)),
                reaction = VideoReaction(reaction, durationMs = 1000),
            )
            output = withTimeout(90_000) { VideoExporter(context).export(project) {} }
            val metadata = MediaMetadataRetriever()
            try {
                metadata.setDataSource(output.absolutePath)
                val duration = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
                assertTrue("Short reaction must not truncate the 2-second base: $duration", duration in 1900..2200)
                assertEquals("320", metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                assertEquals("240", metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
                val before = metadata.getFrameAtTime(500_000, MediaMetadataRetriever.OPTION_CLOSEST)!!
                val after = metadata.getFrameAtTime(1_500_000, MediaMetadataRetriever.OPTION_CLOSEST)!!
                try {
                    assertGray(before.getPixel(30, 200))
                    val pipBefore = before.getPixel(270, 40)
                    assertTrue("Reaction's blue pixels must be encoded", Color.blue(pipBefore) > Color.red(pipBefore) + 80)
                    assertGray(after.getPixel(270, 40))
                    val colorfulEmojiPixels = (120..200 step 3).sumOf { x ->
                        (80..160 step 3).count { y ->
                            val pixel = before.getPixel(x, y)
                            maxOf(Color.red(pixel), Color.green(pixel), Color.blue(pixel)) -
                                minOf(Color.red(pixel), Color.green(pixel), Color.blue(pixel)) > 40
                        }
                    }
                    assertTrue("Emoji pixels must be baked into the encoded video", colorfulEmojiPixels > 10)
                } finally { before.recycle(); after.recycle() }
            } finally { metadata.release() }
            val audio = decodeMono(output)
            assertTrue("Sound effect must produce non-silent encoded audio", audio.samples.any { abs(it.toInt()) > 200 })
        } finally { base.delete(); reaction.delete(); output?.delete() }
    }

    @Test fun babyVoiceChangesPitchOfEncodedAudioWithoutChangingMovieLength() = runBlocking {
        val base = fixture("voice-tone.mp4")
        var output: File? = null
        try {
            val project = VideoProject(VideoSource(base, 2000, 320, 240, true), voice = VoiceEffect.BABY)
            output = withTimeout(90_000) { VideoExporter(context).export(project) {} }
            val audio = decodeMono(output)
            val start = audio.sampleRate / 4
            val end = minOf(audio.samples.size, audio.sampleRate * 7 / 4)
            assertTrue("Export must keep roughly two seconds of audio", audio.samples.size in audio.sampleRate * 18 / 10..audio.sampleRate * 23 / 10)
            var upwardCrossings = 0
            for (i in start + 1 until end) if (audio.samples[i - 1] <= 0 && audio.samples[i] > 0) upwardCrossings++
            val frequency = upwardCrossings.toDouble() * audio.sampleRate / (end - start)
            assertTrue("440 Hz tone should shift near 704 Hz, observed $frequency Hz", frequency in 650.0..760.0)
        } finally { base.delete(); output?.delete() }
    }

    private fun fixture(name: String): File = File.createTempFile("video_test_", ".mp4", context.cacheDir).also { target ->
        instrumentation.context.assets.open("video/$name").use { input -> target.outputStream().use { input.copyTo(it) } }
    }

    private fun assertGray(color: Int) {
        assertTrue(abs(Color.red(color) - Color.green(color)) < 15)
        assertTrue(abs(Color.red(color) - Color.blue(color)) < 15)
    }

    private data class DecodedAudio(val samples: ShortArray, val sampleRate: Int)

    private fun decodeMono(file: File): DecodedAudio {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null
        try {
            extractor.setDataSource(file.absolutePath)
            val track = (0 until extractor.trackCount).first { extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            assertEquals("audio/mp4a-latm", format.getString(MediaFormat.KEY_MIME))
            format.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
            decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            decoder.configure(format, null, null, 0)
            decoder.start()
            val info = MediaCodec.BufferInfo()
            var inputEnded = false
            var outputEnded = false
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val samples = ArrayList<Short>()
            val deadline = System.nanoTime() + 15_000_000_000L
            while (!outputEnded && System.nanoTime() < deadline) {
                if (!inputEnded) {
                    val index = decoder.dequeueInputBuffer(10_000)
                    if (index >= 0) {
                        val buffer = decoder.getInputBuffer(index)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputEnded = true
                        } else {
                            decoder.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val index = decoder.dequeueOutputBuffer(info, 10_000)
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val outputFormat = decoder.outputFormat
                    channels = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    sampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    if (outputFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) assertEquals(AudioFormat.ENCODING_PCM_16BIT, outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING))
                } else if (index >= 0) {
                    val buffer = decoder.getOutputBuffer(index)!!.order(ByteOrder.nativeOrder())
                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)
                    while (buffer.remaining() >= channels * 2) {
                        samples += buffer.short
                        repeat(channels - 1) { buffer.short }
                    }
                    outputEnded = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    decoder.releaseOutputBuffer(index, false)
                }
            }
            assertTrue("AAC decoder must reach end of stream", outputEnded)
            return DecodedAudio(samples.toShortArray(), sampleRate)
        } finally {
            decoder?.let { runCatching { it.stop() }; it.release() }
            extractor.release()
        }
    }
}
