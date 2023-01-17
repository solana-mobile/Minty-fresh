/*
 * PerformMintUseCase
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.usecase

import com.nft.gallery.repository.*
import com.nft.gallery.viewmodel.iconUri
import com.nft.gallery.viewmodel.identityName
import com.nft.gallery.viewmodel.solanaUri
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class MintState {
    NONE,
    UPLOADING_FILE,
    CREATING_METADATA,
    BUILDING_TRANSACTION,
    SIGNING,
    MINTING,
    COMPLETE
}

class PerformMintUseCase @Inject constructor(
    private val storageRepository: StorageUploadRepository,
    private val persistenceUseCase: PersistenceUseCase,
    private val mintTransactionRepository: MintTransactionRepository,
    private val blockhashRepository: LatestBlockhashRepository,
    private val sendTransactionRepository: SendTransactionRepository
) {

    private val _mintState = MutableStateFlow(MintState.NONE)

    val mintState: StateFlow<MintState> = _mintState

    suspend fun performMint(sender: ActivityResultSender, creator: PublicKey, authToken:String,
                            title: String, desc: String, imgUrl: String) {

//        val userPublicKey = persistenceUseCase.walletDetails.map {
//            if (it is Connected) it.publicKey else null
//        }.last()
//
//        check(creator == userPublicKey)
//
//        val authToken = persistenceUseCase.walletDetails.map {
//            if (it is Connected) it.authToken else null
//        }.last()

        // upload the media file
        _mintState.value = MintState.UPLOADING_FILE

        val nftImageUrl = storageRepository.uploadFile(imgUrl)

        // create and upload the NFT metadata
        _mintState.value = MintState.CREATING_METADATA

        val metadataUrl = storageRepository.uploadMetadata(title, desc, nftImageUrl)

        // begin building the transaction
        _mintState.value = MintState.BUILDING_TRANSACTION

        val mintAccount = HotAccount()
        val mintTxn = mintTransactionRepository.buildMintTransaction(title, metadataUrl, mintAccount.publicKey, creator)

        mintTxn.setRecentBlockHash(blockhashRepository.getLatestBlockHash())

        // begin signing transaction step
        _mintState.value = MintState.SIGNING
        delay(700)

        val primarySignature = MobileWalletAdapter().transact(sender) {

            authToken?.let {
                reauthorize(solanaUri, iconUri, identityName, authToken)
            } ?: authorize(solanaUri, iconUri, identityName)

            val transactionBytes =
                mintTxn.serialize(SerializeConfig(
                    requireAllSignatures = false,
                    verifySignatures = false
                ))

            val signingResult = signTransactions(arrayOf(transactionBytes))

            return@transact signingResult.signedPayloads[0].sliceArray(1 until 1 + SIGNATURE_LENGTH)
        }

        // rebuild transaction object from signed bytes
        // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
        // build up the Message (and Transaction) object manually (for now)
        // val signed = Transaction.from(signedBytes)
        val signed = Transaction().apply {
            setRecentBlockHash(mintTxn.recentBlockhash)
            feePayer = creator
            addInstruction(*mintTxn.instructions.toTypedArray())
            addSignature(creator, primarySignature)
        }

        // now that the primary signer (creator) has signed, the mint account can sign
        signed.partialSign(mintAccount)

        _mintState.value = MintState.MINTING

        // send the signed transaction to the cluster
        val transactionSignature = sendTransactionRepository.sendTransaction(signed)

        // Await for transaction confirmation
        sendTransactionRepository.confirmTransaction(transactionSignature)

        _mintState.value = MintState.COMPLETE
    }
}