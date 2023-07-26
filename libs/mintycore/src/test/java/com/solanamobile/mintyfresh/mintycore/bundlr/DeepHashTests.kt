package com.solanamobile.mintyfresh.mintycore.bundlr

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepHashTests {

    @Test
    fun testDeepHash() {
        // given
        val data = byteArrayOf(1, 2, 3)
        val expectedHashLength = 48
        val expectedHash =
            "41300af79285f856e833164518c7ec4974f5869ec77ca3458113fe6c587680d050f9f6864fd77f9eb62bd4e2faea9ae8"
                .hexToByteArray()

        // when
        val result = deepHash(data)

        // then
        assertEquals(expectedHashLength, result.size)
        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun testDeepHashEmptyArray() {
        // given
        val data = byteArrayOf()
        val expectedHashLength = 48
        val expectedHash =
            "fbf00cc444f5fea9dc3bedf62a13fba8ae87e7445fc910567a23bec4eb82fadb1143c433069314d8362983dc3c2e4a38"
                .chunked(2).map {
                    it.toInt(16).toByte()
                }.toByteArray()

        // when
        val result = deepHash(data)

        // then
        assertEquals(expectedHashLength, result.size)
        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun testDeepHashChunks() {
        // given
        val chunks = listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))
        val expectedHashLength = 48
        val expectedHash =
            "4dacdcc81acd09f38c77a07a2a7ae81f77c61e6b97ee5cc7b92f3a7f258e8d5ba69d14d7d66070797b083873717c9896"
                .hexToByteArray()

        // when
        val result = deepHash(chunks)

        // then
        assertEquals(expectedHashLength, result.size)
        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun testDeepHashChunksAcc() {
        // given
        val chunks = listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))
        val expectedHashLength = 48
        val expectedHash =
            "2241894113b88da6daac09ef227a26e51423083c8d033fcc4f143a2a30f92ed3d163b2a66fcdf9ecc39da5a045ed9afc"
                .hexToByteArray()

        // when
        val result = deepHash(chunks, "test".encodeToByteArray())

        // then
        assertEquals(expectedHashLength, result.size)
        assertArrayEquals(expectedHash, result)
    }

    private fun String.hexToByteArray() =
        this.chunked(2).map {
            it.toInt(16).toByte()
        }.toByteArray()
}