package com.solanamobile.mintyfresh.mintycore.ipld

import com.solanamobile.mintyfresh.mintycore.usecase.Web3IdUseCase

class CarFileDirectory(rootCid: CID, data: Map<CID, ByteArray>)
    : CarFile(rootCid, data) {

    class Builder(val cidUseCase: Web3IdUseCase): CarFile.Builder() {

        private val links = mutableListOf<PBLink>()

        fun addDirectory(name: String, files: Map<String, ByteArray>) {
            val links = mutableListOf<PBLink>()

            files.forEach { (fileName, data) ->
                addFile(fileName, data) { cid ->
                    links.add(PBLink(fileName, data.size, cid))
                }
            }

            val rootNode = IpdlDirectory(links).encode()
            val rootCid = cidUseCase.getRootContentId(rootNode)
            add(rootCid, rootNode)
            links.add(PBLink(name, rootNode.size, rootCid))
        }

        fun addFile(fileName: String, data: ByteArray,
                    cidCallback: (Builder.(CID) -> Unit)? = null): Builder {
            val (cid, block) = if (data.size > MAX_BLOCK_SIZE) {
                val fileBlocks = data.asIterable().chunked(MAX_BLOCK_SIZE).associate { chunk ->
                    val chunkBytes = chunk.toByteArray()
                    cidUseCase.getContentId(chunkBytes) to chunkBytes
                }

                add(fileBlocks)

                val fileRoot = IpdlFile(fileBlocks.map { (cid, data) ->
                    PBLink(null, data.size, cid)
                }).encode()

                cidUseCase.getRootContentId(fileRoot) to fileRoot
            } else {
                cidUseCase.getContentId(data) to data
            }

            add(cid, block)
            links.add(PBLink(fileName, data.size, cid))
            cidCallback?.invoke(this, cid)

            return this
        }

        override fun build(): CarFile {
            val rootNode = IpdlDirectory(links).encode()
            val rootCid = cidUseCase.getRootContentId(rootNode)
            addRoot(rootCid, rootNode)

            return super.build()
        }
    }
}