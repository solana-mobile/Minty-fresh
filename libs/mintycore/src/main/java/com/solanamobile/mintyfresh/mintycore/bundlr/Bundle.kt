package com.solanamobile.mintyfresh.mintycore.bundlr

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Bundle(val dataItems: List<DataItem>) {
    val itemCount: Int get() = dataItems.size
    val dataOffset: Int get() = 32 + itemCount * 64

    fun bundle() = bundle(dataItems)
    suspend fun sign(signer: Signer) {
        dataItems.forEach { item ->
            item.sign(signer)
        }
    }

    companion object {
        fun bundle(dataItems: List<DataItem>): ByteArray {
            val header = ByteBuffer.allocate(32 + 64*dataItems.size)

            header.order(ByteOrder.LITTLE_ENDIAN)

            // data item count
            header.put(dataItems.size.to32ByteArray())

            // data item info
            dataItems.forEach { item ->
                header.put(item.byteArray.size.to32ByteArray())
                header.put(item.rawId)
            }

            return dataItems.fold(header.array()) { acc, item -> acc + item.byteArray}
        }

        suspend fun signAndBundle(dataItems: List<DataItem>, signer: Signer): ByteArray {
            dataItems.forEach { item ->
//                if (!item.isSigned()) item.sign(signer)
                item.sign(signer)
            }

            return bundle(dataItems)
        }
    }
}

fun Int.to32ByteArray() = toNByteArray(32)
fun Int.toNByteArray(size: Int) =
    ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN).putInt(this)