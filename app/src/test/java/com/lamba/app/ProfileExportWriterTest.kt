package com.lamba.app

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ProfileExportWriterTest {

    @Test
    fun copyWritesTheExportStreamWithoutChangingItsBytes() {
        val source = byteArrayOf(80, 75, 3, 4, 20, 0, 6, 0)
        val destination = ByteArrayOutputStream()

        ProfileExportWriter.copy(ByteArrayInputStream(source), destination)

        assertArrayEquals(source, destination.toByteArray())
    }
}
