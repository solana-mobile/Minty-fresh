package com.solanamobile.mintyfresh.networkinterfaceimpl.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solanamobile.mintyfresh.networkinterface.usecase.IMyMintsUseCase
import com.solanamobile.mintyfresh.networkinterfaceimpl.repository.NFTRepository
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import com.solanamobile.mintyfresh.persistence.diskcache.MyMintsCacheRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MyMintsUseCase @Inject constructor(
    private val nftRepository: NFTRepository,
    private val myMintsCacheRepository: MyMintsCacheRepository,
    private val metaplexToCacheMapper: MetaplexToCacheMapper
) : IMyMintsUseCase<MyMint> {

    override fun getCachedMints(publicKey: PublicKey): Flow<List<MyMint>> {
        return myMintsCacheRepository.get(
            pubKey = publicKey.toString(),
            rpcClusterName = RpcCluster.Devnet.name //TODO: This value will come from networking layer
        )
    }

    override suspend fun getAllUserMintyFreshNfts(publicKey: PublicKey): List<NFT> {
        val nfts = nftRepository.getAllUserMintyFreshNfts(publicKey)

        val currentMintList = metaplexToCacheMapper.map(nfts, RpcCluster.Devnet.name)   //TODO: Cluster will come from networking module
        myMintsCacheRepository.deleteStaleData(
            currentMintList = currentMintList,
            publicKey.toString()
        )

        if (nfts.isNotEmpty()) {
            // Fetch and update each NFT data.
            nfts.forEach { nft ->
                val cachedMint = myMintsCacheRepository.get(
                    id = nft.mint.toString(),
                    pubKey = publicKey.toString(),
                    rpcClusterName = RpcCluster.Devnet.name //TODO: This value will come from networking layer
                )
                if (cachedMint == null) {
                    val metadata = nftRepository.getNftsMetadata(nft)
                    val mint = metaplexToCacheMapper.map(nft, metadata, RpcCluster.Devnet.name)   //TODO: Cluster will come from networking module

                    if (mint != null) {
                        myMintsCacheRepository.insertAll(listOf(mint))
                    }
                }
            }
        }

        return nfts
    }
}