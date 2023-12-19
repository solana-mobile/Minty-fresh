package com.solanamobile.mintyfresh.mintycore.repository

import com.solana.core.PublicKey
import com.solana.core.Transaction
import com.solana.programs.SystemProgram
import javax.inject.Inject

class FundNodeTransactionRepository @Inject constructor(
    val storageRepository: StorageUploadRepository
) {
    suspend fun getPriceForBytes(bytesToUpload: Int): Long {
        return (storageRepository.getBundlerPrice(bytesToUpload)*1.1).toLong()
    }

    suspend fun buildNodeFundingTransaction(account: PublicKey, bytesToUpload: Int): Transaction? {
        val price = getPriceForBytes(bytesToUpload)
        return if (price > 0) buildNodeFundingTransaction(account, price) else null
    }

    suspend fun buildNodeFundingTransaction(account: PublicKey, lamports: Long): Transaction {
        val nodeInfo = storageRepository.getBundlerNodeInfo()
        nodeInfo.addresses.solana?.let {
            val bundlrNode = PublicKey(it)
            return Transaction().apply {
                addInstruction(SystemProgram.transfer(account, bundlrNode, lamports))
                feePayer = account
            }
        } ?: throw Error("No Solana funding address was provided by gateway (${nodeInfo.gateway})")
    }
}