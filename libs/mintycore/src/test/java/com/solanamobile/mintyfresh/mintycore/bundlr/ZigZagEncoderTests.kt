package com.solanamobile.mintyfresh.mintycore.bundlr

import org.junit.Assert
import org.junit.Test

class ZigZagEncoderTests {

    @Test
    fun testZigzag() {
        // given
        val value = 2147483647L
        val expected = 4294967294L

        // when
        val result = value.zigzag()

        // then
        Assert.assertEquals(expected, result)
    }
}