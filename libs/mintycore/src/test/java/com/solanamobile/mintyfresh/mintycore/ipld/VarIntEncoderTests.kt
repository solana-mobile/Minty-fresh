package com.solanamobile.mintyfresh.mintycore.ipld

import org.junit.Assert
import org.junit.Test

class VarIntEncoderTests {

    @Test
    fun testVarint() {
        // given
        val value = 16385
        val expected = byteArrayOf(-127, -128, 1)

        // when
        val result = value.asVarint()

        // then
        Assert.assertArrayEquals(expected, result)
    }
}