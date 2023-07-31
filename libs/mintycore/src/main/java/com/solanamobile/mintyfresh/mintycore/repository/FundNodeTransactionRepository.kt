package com.solanamobile.mintyfresh.mintycore.repository

import com.solana.core.PublicKey
import com.solana.core.Transaction
import com.solana.programs.SystemProgram
import javax.inject.Inject

class FundNodeTransactionRepository @Inject constructor(
    val storageRepository: StorageUploadRepository
) {
    suspend fun buildNodeFundingTransaction(account: PublicKey, bytesToUpload: Int): Transaction {
        val bundlrNode = PublicKey(storageRepository.getBundlrNodeInfo().addresses.solana)
        val price = (storageRepository.getBundlrPrice(bytesToUpload)*1.1).toLong()

        return Transaction().apply {
            addInstruction(SystemProgram.transfer(account, bundlrNode, price))
            feePayer = account
        }
    }
}