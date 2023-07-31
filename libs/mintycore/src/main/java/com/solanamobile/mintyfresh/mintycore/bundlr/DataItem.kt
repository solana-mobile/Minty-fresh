package com.solanamobile.mintyfresh.mintycore.bundlr

import com.solanamobile.mintyfresh.mintycore.util.asVarint
import com.solanamobile.mintyfresh.mintycore.util.decodeVarint
import com.solanamobile.mintyfresh.mintycore.util.zigzag
import com.solanamobile.mintyfresh.mintycore.util.zigzagToTwosComp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and
import kotlin.math.ceil

open class DataItem(val byteArray: ByteArray) {

    private val sha256HashProvider = MessageDigest.getInstance("SHA-256")
    private val targetOffset get() = 2 + signatureConfig.signatureLength + signatureConfig.ownerLength
    private val anchorOffset get() = targetOffset + 1 + (target?.let { TARGET_LENGTH } ?: 0)
    private val tagsOffset get() = anchorOffset + 1 + (anchor?.let { ANCHOR_LENGTH } ?: 0)
    private val dataOffset get() = tagsOffset + 16 + tagLength.toInt()

    val signatureType: Int get() =
        ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

    val signatureConfig: SignatureConfig get() =
        SignatureConfig[signatureType] ?: throw IllegalStateException("Invalid signature type")

    val signature: ByteArray get() = byteArray.copyOfRange(2, 2 + signatureConfig.signatureLength)
    val owner: ByteArray get() = byteArray.copyOfRange(
        2 + signatureConfig.signatureLength, 2 + signatureConfig.signatureLength + signatureConfig.ownerLength
    )

    val target: ByteArray? get() =
        if (byteArray[targetOffset] > 0) byteArray.copyOfRange(targetOffset + 1, targetOffset + 1 + TARGET_LENGTH) else null
    val anchor: ByteArray? get() =
        if (byteArray[anchorOffset] > 0) byteArray.copyOfRange(anchorOffset + 1, anchorOffset + 1 + TARGET_LENGTH) else null

    val tagCount get() = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getLong(tagsOffset)
    val tagLength get() = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).getLong(tagsOffset + 8)
    val rawTags get() = byteArray.copyOfRange(tagsOffset + 16, tagsOffset + 16 + tagLength.toInt())
    val tags get() = deserializeTags(rawTags)

    val rawData get() = byteArray.copyOfRange(dataOffset, byteArray.size)
    
    val id: String get() = Base64.getUrlEncoder().withoutPadding().encodeToString(rawId)
    val rawId: ByteArray get() = sha256HashProvider.digest(signature)

    fun isSigned() = !signature.contentEquals(ByteArray(signatureConfig.signatureLength))

    fun getSignatureData() = deepHash(
        "dataitem".encodeToByteArray(),
        "1".encodeToByteArray(),
        signatureType.toString().encodeToByteArray(),
        owner,
        target ?: byteArrayOf(),
        anchor ?: byteArrayOf(),
        rawTags,
        rawData
    )

    suspend fun sign(signer: Signer): ByteArray {
        check(signer.signatureType.toInt() == signatureType) { "Wrong signer, incorrect signature type for item" }
        check(signer.publicKey.contentEquals(owner)) { "Wrong signer, item must be signed by its owner" }
        return withContext(Dispatchers.IO) {
            signer.sign(getSignatureData()).also {
                it.copyInto(byteArray, 2)
            }
        }
    }

    companion object {
        const val MIN_BINARY_SIZE = 80
        const val MAX_TAG_BYTES = 4096
        const val TARGET_LENGTH = 32
        const val ANCHOR_LENGTH = 32

        @JvmStatic
        fun create(data: ByteArray, signer: Signer,
                   target: String?, anchor: String?, tags : Map<String, String>?): DataItem {

            val owner = signer
            val target = target?.run { Base64.getUrlDecoder().decode(this) }
            val targetLength = 1 + (target?.size ?: 0)
            val anchor = anchor?.take(ANCHOR_LENGTH)?.encodeToByteArray()
            val anchorLength = 1 + (anchor?.size ?: 0)
            val serializedTags = if (tags?.isNotEmpty() == true) serializeTags(tags) else null
            val tagsLength = 16 + (serializedTags?.size ?: 0)

            val dataLength = data.size

            val length = 2 + signer.signatureLength + signer.ownerLength +
                    targetLength + anchorLength + tagsLength + dataLength

            return DataItem(ByteBuffer.allocate(length).apply {
                // https://github.com/ArweaveTeam/arweave-standards/blob/master/ans/ANS-104.md
                order(ByteOrder.LITTLE_ENDIAN)
                // Signature Type
                putShort(signer.signatureType)
                // Signature
                put(ByteArray(signer.signatureLength))
                // owner
                put(owner.publicKey)
                // target (+ presence byte)
                put(if (target != null) 1 else 0) // presence byte
                target?.let {
                    check(it.size == TARGET_LENGTH) { "Target must be ${TARGET_LENGTH} bytes" }
                    put(target)
                }
                // anchor (+ presence byte)
                put(if (anchor != null) 1 else 0) // presence byte
                anchor?.let {
                    check(it.size == ANCHOR_LENGTH) { "Anchor must be ${ANCHOR_LENGTH} bytes" }
                    put(anchor)
                }
                // tags (+ sizes)
                putLong(tags?.size?.toLong() ?: 0L)
                putLong(serializedTags?.size?.toLong() ?: 0L)
                serializedTags?.let { put(serializedTags) }
                // data
                put(data)
            }.array())
        }

        internal fun serializeTags(tags: Map<String, String>): ByteArray = tags.map { (name, value) ->
            name.encodeAvro().array() + value.encodeAvro().array()
        }.run {
            val encodedLength = tags.size.zigzagVInt()
            ByteBuffer.allocate(this.fold(encodedLength.size) { acc, encodedTag -> acc + encodedTag.size} + 1).apply {
                put(encodedLength)
                tags.forEach { (name, value) ->
                    put(name.encodeAvro().array())
                    put(value.encodeAvro().array())
                }
                put(0)
            }.array()
        }
        fun deserializeTags(byteArray: ByteArray): Map<String, String> {
            val tags = mutableMapOf<String, String>()
            val buffer = ByteBuffer.wrap(byteArray)

            val numTags = buffer.decodeZigzagVInt()

            repeat(numTags) {
                val name = buffer.decodeAvroString()
                val value = buffer.decodeAvroString()
                tags[name] = value
            }

            return tags
        }

        private fun ByteBuffer.decodeZigzagVInt(): Int = this.decodeVarint().zigzagToTwosComp()

        /*
            Tag Avro Array Schema
             {
               "type": "array",
               "items": {
                 "type": "record",
                 "name": "Tag",
                 "fields": [
                   { "name": "name", "type": "bytes" },
                   { "name": "value", "type": "bytes" }
                 ]
               }
             }

             // we could implement the Avro spec, but we only use strings so for now skip all this
             sealed class Avro(open val type: String) {
                data class Field(val name: String, override val type: String) : Avro(type)
                data class Array(val items: Avro) : Avro("array")
                data class Record(val name: String, val fields: List<Field>) : Avro("record")
             }
         */
        private fun String.encodeAvro(): ByteBuffer = encodeToByteArray().run {
            val encodedSize = size.zigzagVInt()
            ByteBuffer.allocate(encodedSize.size + size).apply {
                put(encodedSize)
                put(this@run)
            }
        }

        private fun ByteBuffer.decodeAvroString(): String {
            val size = this.decodeZigzagVInt()
            val bytes = ByteArray(size)
            this.get(bytes)
            return String(bytes)
        }
    }

    data class Builder(
        var data: ByteArray = byteArrayOf(),
        var owner: Signer? = null,
        var target: String? = null,
        var anchor: String? = null,
        val tags: MutableMap<String, String> = mutableMapOf()
    ) {
        fun data(data: ByteArray) = apply { this.data = data }
        fun owner(owner: Signer) = apply { this.owner = owner }
        fun target(target: String) = apply { this.target = target }
        fun anchor(anchor: String) = apply { this.anchor = anchor }
        fun tag(name: String, value: String) = apply { tags[name] = value }
        fun build(): DataItem {
            val owner = owner
            check(owner != null) { "No Owner Provided" }
            return create(data, owner, target, anchor, tags)
        }
    }
}

fun Int.zigzagVInt() = zigzag().asVarint()
fun Long.zigzagVInt() = zigzag().asVarint()