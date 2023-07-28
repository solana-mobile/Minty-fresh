package com.solanamobile.mintyfresh.mintycore.bundlr

import com.solana.core.HotAccount
import com.solana.vendor.TweetNaclFast
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
import java.util.Base64

class DataItemTests {

    @Test
    fun testSerializeTags() {
        // given
        val tags = mapOf("Hello" to "Bundlr!", "This is a " to "Test")
        val expected = byteArrayOf(
            4, 10, 72, 101, 108, 108, 111, 14, 66, 117, 110, 100, 108, 114, 33, 20,
            84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 8, 84, 101, 115, 116, 0
        )

        // when
        val result = DataItem.serializeTags(tags)

        // then
        assertArrayEquals(expected, result)
    }

    @Test
    fun testSerializeTagsShortTags() {
        // given
        val tags = mapOf("ThisIsAShortName" to "ThisIsAShortValue")
        val expected = byteArrayOf(
            2, 32, 84, 104, 105, 115, 73, 115, 65, 83, 104, 111, 114, 116, 78, 97,
            109, 101, 34, 84, 104, 105, 115, 73, 115, 65, 83, 104, 111, 114, 116, 86,
            97, 108, 117, 101, 0,
        )

        // when
        val result = DataItem.serializeTags(tags)

        // then
        assertArrayEquals(expected, result)
    }

    @Test
    fun testSerializeTagsLongTags() {
        // given
        val tags = mapOf("ThisIsALongNameAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" to
                "ThisIsALongValueAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",)
        val expected = byteArrayOf(
            2, -128, 1, 84, 104, 105, 115, 73, 115, 65, 76, 111, 110, 103, 78, 97,
            109, 101, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, -100, 1, 84, 104, 105, 115, 73, 115, 65, 76, 111, 110, 103, 86,
            97, 108, 117, 101, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65,
            65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 0,
        )

        // when
        val result = DataItem.serializeTags(tags)

        // then
        assertArrayEquals(expected, result)
    }

    @Test
    fun testDeserializeTags() {
        // given
        val expectedTags = mapOf("Hello" to "Bundlr!", "This is a " to "Test")
        val bytes = byteArrayOf(
            4, 10, 72, 101, 108, 108, 111, 14, 66, 117, 110, 100, 108, 114, 33, 20,
            84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 8, 84, 101, 115, 116, 0
        )

        // when
        val result = DataItem.deserializeTags(bytes)

        // then
        assertEquals(expectedTags, result)
    }

    @Test
    fun testDataItemBasic() {
        // given
        val owner = HotSolanaSigner()
        val data = "hello"

        // when
        val dataItem = DataItem.Builder()
            .owner(owner)
            .data(data.encodeToByteArray())
            .build()

        // then
        assertEquals(SignatureConfig.ED25519.signatureType.toInt(), dataItem.signatureType)
        assertArrayEquals(owner.publicKey, dataItem.owner)
        assertNull(dataItem.target)
        assertNull(dataItem.anchor)
        assertEquals(0, dataItem.tagCount)
        assertEquals(data, dataItem.rawData.decodeToString())
    }

    @Test
    fun testDataItemWithTags() {
        // given
        val owner = HotSolanaSigner()
        val data = "hello"
        val tags = mapOf("Hello" to "Bundlr!", "This is a " to "Test")
        val expectedTags = byteArrayOf(
            4, 10, 72, 101, 108, 108, 111, 14, 66, 117, 110, 100, 108, 114, 33, 20,
            84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 8, 84, 101, 115, 116, 0
        )

        // when
        val dataItem = DataItem.Builder()
            .owner(owner)
            .tag("Hello", "Bundlr!")
            .tag("This is a ", "Test")
            .data(data.encodeToByteArray())
            .build()

        // then
        assertEquals(SignatureConfig.ED25519.signatureType.toInt(), dataItem.signatureType)
        assertArrayEquals(owner.publicKey, dataItem.owner)
        assertNull(dataItem.target)
        assertNull(dataItem.anchor)
        assertEquals(2, dataItem.tagCount)
        assertEquals(tags, dataItem.tags)
        assertArrayEquals(expectedTags, dataItem.rawTags)
        assertEquals(data, dataItem.rawData.decodeToString())
    }

    @Test
    fun testDataItem() {
        // given
        val owner = HotSolanaSigner()
        val target = "OXcT1sVRSA5eGwt2k6Yuz8-3e3g9WJi5uSE99CWqsBs"
        val anchor ="Math.apt'#]gng(36).substring(30)"
        val data = "hello"

        // when
        val dataItem = DataItem.Builder()
            .owner(owner)
            .target(target)
            .anchor(anchor)
            .tag("Content-Type", "image/png")
            .data(data.encodeToByteArray())
            .build()

        // then
        assertEquals(SignatureConfig.ED25519.signatureType.toInt(), dataItem.signatureType)
        assertArrayEquals(owner.publicKey, dataItem.owner)
        assertEquals(target, Base64.getUrlEncoder().encodeToString(dataItem.target).dropLastWhile { it == '=' })
        assertEquals(anchor, dataItem.anchor?.decodeToString())
        assertEquals(1, dataItem.tagCount)
        assertEquals(data, dataItem.rawData.decodeToString())
    }

    @Test
    fun testSolanaSigner() = runTest {
        // given
        val keyPair = TweetNaclFast.Signature.keyPair()
        val owner = HotSolanaSigner(keyPair.secretKey)
        val dataItem = DataItem.Builder()
            .data("hello".encodeToByteArray())
            .owner(owner)
            .target("OXcT1sVRSA5eGwt2k6Yuz8-3e3g9WJi5uSE99CWqsBs")
            .anchor("Math.apt'#]gng(36).substring(30)")
            .tag("Content-Type", "image/png")
            .build()

        // when
        dataItem.sign(owner)
        val signature = dataItem.signature
        val verify = TweetNaclFast.Signature(dataItem.owner, keyPair.secretKey)
            .detached_verify(dataItem.getSignatureData(), signature)

        // then
        assert(verify)
        assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(signature), dataItem.rawId)
    }

    class HotSolanaSigner(secretKey: ByteArray? = null) : Ed25519Signer() {
        private val account = secretKey?.let { HotAccount(secretKey) } ?: HotAccount()
        override val publicKey: ByteArray = account.publicKey.toByteArray()
        override suspend fun sign(message: ByteArray): ByteArray = account.sign(message)
    }
}