package com.solanamobile.mintyfresh.networkinterfaceimpl.usecase

import android.util.Log
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.networkinterface.usecase.IMyMintsUseCase
import com.solanamobile.mintyfresh.networkinterfaceimpl.repository.NFTRepository
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import com.solanamobile.mintyfresh.persistence.diskcache.MyMintsCacheRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsUseCase @Inject constructor(
    private val nftRepository: NFTRepository,
    private val myMintsCacheRepository: MyMintsCacheRepository,
    private val metaplexToCacheMapper: MetaplexToCacheMapper,
    private val rpcConfig: IRpcConfig
) : IMyMintsUseCase {

    override fun getCachedMints(publicKey: String): Flow<List<MyMint>> {
        return myMintsCacheRepository.get(
            pubKey = publicKey,
            clusterName = rpcConfig.rpcCluster.name
        )
    }

    override suspend fun getAllUserMintyFreshNfts(publicKey: String): List<MyMint> {
        val nfts = nftRepository.getAllUserMintyFreshNfts(publicKey)
        val clusterName = rpcConfig.rpcCluster.name

        val currentMintList = metaplexToCacheMapper.map(nfts, clusterName)
        myMintsCacheRepository.deleteStaleData(
            currentMintList = currentMintList,
            clusterName = clusterName,
            pubKey = publicKey
        )

        if (nfts.isNotEmpty()) {
            // Fetch and update each NFT data.
            nfts.forEach { nft ->
                val cachedMint = myMintsCacheRepository.get(
                    id = nft.mint.toString(),
                    pubKey = publicKey,
                    clusterName = clusterName
                )
                if (cachedMint == null) {
                    try {
                        val metadata = nftRepository.getNftsMetadata(nft)
                        val mint = metaplexToCacheMapper.map(nft, metadata, clusterName)

                        if (mint != null) {
                            myMintsCacheRepository.insertAll(listOf(mint))
                        }
                    } catch (e: Exception) {
                        Log.e("MintyFresh", "Error loading NFT", e)
                    }
                }
            }
        }

        return currentMintList
    }
}