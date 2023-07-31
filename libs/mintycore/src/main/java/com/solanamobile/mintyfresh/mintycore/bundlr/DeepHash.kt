package com.solanamobile.mintyfresh.mintycore.bundlr

import java.security.MessageDigest

typealias DeepHashChunk = ByteArray
typealias DeepHashChunks = List<DeepHashChunk>

fun deepHash(data: DeepHashChunk): ByteArray {
    val hashProvider = MessageDigest.getInstance("SHA-384")
    val tag = "blob".encodeToByteArray() + data.size.toString().encodeToByteArray()
    val taggedHash = hashProvider.digest(tag) + hashProvider.digest(data)
    return hashProvider.digest(taggedHash)
}

fun deepHash(chunks: DeepHashChunks): ByteArray {
    val tag = "list".encodeToByteArray() + chunks.size.toString().encodeToByteArray()
    return deepHash(chunks, MessageDigest.getInstance("SHA-384").digest(tag))
}

fun deepHash(vararg chunks: DeepHashChunk): ByteArray = deepHash(chunks.asList())

fun deepHash(chunks: DeepHashChunks, acc: ByteArray): ByteArray {
    if (chunks.isEmpty()) return acc
    val hashPair = acc + deepHash(chunks.first())
    val newAcc = MessageDigest.getInstance("SHA-384").digest(hashPair)
    return deepHash(chunks.drop(1), newAcc)
}