package com.mundodedescobertas.app.video

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Bounded provider reads matter even when a document reports an incorrect/unknown size. */
class VideoImportsTest {
    @Test fun exactLimitCopiesEveryByteWithoutTruncation() = runBlocking {
        val expected = ByteArray(100_000) { (it % 251).toByte() }
        val output = ByteArrayOutputStream()
        assertEquals(expected.size.toLong(), VideoImports.copyBounded(ByteArrayInputStream(expected), output, expected.size.toLong()))
        assertArrayEquals(expected, output.toByteArray())
    }

    @Test fun streamingProviderCannotWritePastBoundEvenWithoutSizeMetadata() = runBlocking {
        val input = object : ByteArrayInputStream(byteArrayOf(1, 2, 3, 4, 5, 6)) {
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int = super.read(buffer, offset, minOf(length, 2))
        }
        val output = ByteArrayOutputStream()
        val error = runCatching { VideoImports.copyBounded(input, output, 4) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), output.toByteArray())
    }

    @Test fun emptyDocumentIsRejectedInsteadOfBecomingAnEditableMovie() = runBlocking {
        val error = runCatching { VideoImports.copyBounded(ByteArrayInputStream(byteArrayOf()), ByteArrayOutputStream(), 10) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test fun cancellationStopsReadingProviderAtTheNextChunk() = runBlocking {
        var bytesRead = 0
        val input = object : ByteArrayInputStream(ByteArray(100)) {
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int = super.read(buffer, offset, minOf(length, 2)).also { if (it > 0) bytesRead += it }
        }
        val job = launch {
            val importingJob = currentCoroutineContext()[Job]!!
            val output = object : ByteArrayOutputStream() {
                override fun write(buffer: ByteArray, offset: Int, length: Int) { super.write(buffer, offset, length); importingJob.cancel() }
            }
            VideoImports.copyBounded(input, output, 100)
        }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(2, bytesRead)
    }
}
