package com.nft.gallery.metaplex.jen.shadowdrive

import com.metaplex.lib.serialization.format.Borsh
import com.metaplex.lib.serialization.serializers.solana.AnchorInstructionSerializer
import com.metaplex.lib.serialization.serializers.solana.PublicKeyAs32ByteSerializer
import com.nft.gallery.metaplex.jen.U128
import com.nft.gallery.metaplex.jen.U128Serializer
import com.solana.core.AccountMeta
import com.solana.core.PublicKey
import com.solana.core.TransactionInstruction
import kotlin.String
import kotlin.UInt
import kotlin.ULong
import kotlinx.serialization.Serializable

//
// Instructions
//
// This code was generated locally by Funkatronics on 2023-01-24
//
object ShadowDriveInstructions {
    fun initializeConfig(
        storageConfig: PublicKey,
        admin1: PublicKey,
        systemProgram: PublicKey,
        rent: PublicKey,
        uploader: PublicKey,
        admin2: PublicKey?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(admin1, true, true),
            AccountMeta(systemProgram, false, false), AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("initialize_config"),
            Args_initializeConfig(uploader, admin2)
            ))

    fun updateConfig(
        storageConfig: PublicKey,
        admin: PublicKey,
        newStorageCost: ULong?,
        newStorageAvailable: U128?,
        newAdmin2: PublicKey?,
        newMaxAcctSize: ULong?,
        newMinAcctSize: ULong?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(admin, true, true)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("update_config"),
            Args_updateConfig(newStorageCost, newStorageAvailable, newAdmin2, newMaxAcctSize,
            newMinAcctSize)
            ))

    fun mutableFees(
        storageConfig: PublicKey,
        admin: PublicKey,
        shadesPerGbPerEpoch: ULong?,
        crankBps: UInt?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(admin, true, true)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("mutable_fees"),
            Args_mutableFees(shadesPerGbPerEpoch, crankBps)
            ))

    fun initializeAccount(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        owner1: PublicKey,
        uploader: PublicKey,
        owner1TokenAccount: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        rent: PublicKey,
        identifier: String,
        storage: ULong,
        owner2: PublicKey?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(owner1, true, true),
            AccountMeta(uploader, true, false), AccountMeta(owner1TokenAccount, false, true),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("initialize_account"),
            Args_initializeAccount(identifier, storage, owner2)
            ))

    fun initializeAccount2(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        owner1: PublicKey,
        uploader: PublicKey,
        owner1TokenAccount: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        rent: PublicKey,
        identifier: String,
        storage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(owner1, true, true),
            AccountMeta(uploader, true, false), AccountMeta(owner1TokenAccount, false, true),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("initialize_account2"),
            Args_initializeAccount2(identifier, storage)
            ))

    fun updateAccount(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        identifier: String?,
        owner2: PublicKey?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("update_account"),
            Args_updateAccount(identifier, owner2)
            ))

    fun updateAccount2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        identifier: String?
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("update_account2"),
            Args_updateAccount2(identifier)
            ))

    fun requestDeleteAccount(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("request_delete_account"),
            Args_requestDeleteAccount()
            ))

    fun requestDeleteAccount2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("request_delete_account2"),
            Args_requestDeleteAccount2()
            ))

    fun unmarkDeleteAccount(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(owner, true, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("unmark_delete_account"),
            Args_unmarkDeleteAccount()
            ))

    fun unmarkDeleteAccount2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        owner: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(owner, true, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("unmark_delete_account2"),
            Args_unmarkDeleteAccount2()
            ))

    fun redeemRent(
        storageAccount: PublicKey,
        file: PublicKey,
        owner: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageAccount, false, false), AccountMeta(file, false, true),
            AccountMeta(owner, true, true)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("redeem_rent"), Args_redeemRent()))

    fun deleteAccount(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        owner: PublicKey,
        shdwPayer: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(owner, false, true), AccountMeta(shdwPayer, false, true),
            AccountMeta(uploader, true, false), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("delete_account"),
            Args_deleteAccount()
            ))

    fun deleteAccount2(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        owner: PublicKey,
        shdwPayer: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(owner, false, true), AccountMeta(shdwPayer, false, true),
            AccountMeta(uploader, true, false), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("delete_account2"),
            Args_deleteAccount2()
            ))

    fun makeAccountImmutable(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        emissionsWallet: PublicKey,
        owner: PublicKey,
        uploader: PublicKey,
        ownerAta: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        associatedTokenProgram: PublicKey,
        rent: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(emissionsWallet, false,
            true), AccountMeta(owner, true, true), AccountMeta(uploader, true, false),
            AccountMeta(ownerAta, false, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(associatedTokenProgram, false, false), AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("make_account_immutable"),
            Args_makeAccountImmutable()
            ))

    fun makeAccountImmutable2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        emissionsWallet: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        uploader: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        associatedTokenProgram: PublicKey,
        rent: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(emissionsWallet, false,
            true), AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(uploader, true, false), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(associatedTokenProgram, false, false), AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("make_account_immutable2"),
            Args_makeAccountImmutable2()
            ))

    fun badCsam(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        storageAvailable: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(uploader, true, true), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("bad_csam"),
            Args_badCsam(storageAvailable)
            ))

    fun badCsam2(
        storageConfig: PublicKey,
        userInfo: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        storageAvailable: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(userInfo, false, true),
            AccountMeta(storageAccount, false, true), AccountMeta(stakeAccount, false, true),
            AccountMeta(uploader, true, true), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("bad_csam2"),
            Args_badCsam2(storageAvailable)
            ))

    fun increaseStorage(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        uploader: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        additionalStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(uploader, true, false),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("increase_storage"),
            Args_increaseStorage(additionalStorage)
            ))

    fun increaseStorage2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        uploader: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        additionalStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(uploader, true, false),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("increase_storage2"),
            Args_increaseStorage2(additionalStorage)
            ))

    fun increaseImmutableStorage(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        emissionsWallet: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        uploader: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        additionalStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(emissionsWallet, false, true), AccountMeta(owner, true, true),
            AccountMeta(ownerAta, false, true), AccountMeta(uploader, true, false),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("increase_immutable_storage"),
            Args_increaseImmutableStorage(additionalStorage)
            ))

    fun increaseImmutableStorage2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        emissionsWallet: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        uploader: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        additionalStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(emissionsWallet, false, true), AccountMeta(owner, true, true),
            AccountMeta(ownerAta, false, true), AccountMeta(uploader, true, false),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("increase_immutable_storage2"),
            Args_increaseImmutableStorage2(additionalStorage)
            ))

    fun decreaseStorage(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        unstakeInfo: PublicKey,
        unstakeAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        rent: PublicKey,
        removeStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(unstakeInfo, false, true), AccountMeta(unstakeAccount, false, true),
            AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(tokenMint, false, false),
            AccountMeta(uploader, true, false), AccountMeta(emissionsWallet, false, true),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("decrease_storage"),
            Args_decreaseStorage(removeStorage)
            ))

    fun decreaseStorage2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        unstakeInfo: PublicKey,
        unstakeAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        uploader: PublicKey,
        emissionsWallet: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey,
        rent: PublicKey,
        removeStorage: ULong
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(unstakeInfo, false, true), AccountMeta(unstakeAccount, false, true),
            AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(tokenMint, false, false),
            AccountMeta(uploader, true, false), AccountMeta(emissionsWallet, false, true),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false),
            AccountMeta(rent, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("decrease_storage2"),
            Args_decreaseStorage2(removeStorage)
            ))

    fun claimStake(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        unstakeInfo: PublicKey,
        unstakeAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(unstakeInfo, false, true), AccountMeta(unstakeAccount, false, true),
            AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("claim_stake"), Args_claimStake()))

    fun claimStake2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        unstakeInfo: PublicKey,
        unstakeAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(unstakeInfo, false, true), AccountMeta(unstakeAccount, false, true),
            AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("claim_stake2"),
            Args_claimStake2()
            ))

    fun crank(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        cranker: PublicKey,
        crankerAta: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(cranker, true, true),
            AccountMeta(crankerAta, false, true), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("crank"), Args_crank()))

    fun crank2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        stakeAccount: PublicKey,
        cranker: PublicKey,
        crankerAta: PublicKey,
        emissionsWallet: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, true), AccountMeta(storageAccount, false,
            true), AccountMeta(stakeAccount, false, true), AccountMeta(cranker, true, true),
            AccountMeta(crankerAta, false, true), AccountMeta(emissionsWallet, false, true),
            AccountMeta(tokenMint, false, false), AccountMeta(systemProgram, false, false),
            AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("crank2"), Args_crank2()))

    fun refreshStake(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("refresh_stake"),
            Args_refreshStake()
            ))

    fun refreshStake2(
        storageConfig: PublicKey,
        storageAccount: PublicKey,
        owner: PublicKey,
        ownerAta: PublicKey,
        stakeAccount: PublicKey,
        tokenMint: PublicKey,
        systemProgram: PublicKey,
        tokenProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageConfig, false, false), AccountMeta(storageAccount, false,
            true), AccountMeta(owner, true, true), AccountMeta(ownerAta, false, true),
            AccountMeta(stakeAccount, false, true), AccountMeta(tokenMint, false, false),
            AccountMeta(systemProgram, false, false), AccountMeta(tokenProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("refresh_stake2"),
            Args_refreshStake2()
            ))

    fun migrateStep1(
        storageAccount: PublicKey,
        migration: PublicKey,
        owner: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageAccount, false, true), AccountMeta(migration, false, true),
            AccountMeta(owner, true, true), AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("migrate_step1"),
            Args_migrateStep1()
            ))

    fun migrateStep2(
        storageAccount: PublicKey,
        migration: PublicKey,
        owner: PublicKey,
        systemProgram: PublicKey
    ): TransactionInstruction = TransactionInstruction(PROGRAM_ADDRESS,
            listOf(AccountMeta(storageAccount, false, true), AccountMeta(migration, false, true),
            AccountMeta(owner, true, true), AccountMeta(systemProgram, false, false)),
            Borsh.encodeToByteArray(AnchorInstructionSerializer("migrate_step2"),
            Args_migrateStep2()
            ))

    @Serializable
    class Args_initializeConfig(@Serializable(with = PublicKeyAs32ByteSerializer::class) val
            uploader: PublicKey, @Serializable(with = PublicKeyAs32ByteSerializer::class) val
            admin2: PublicKey?)

    @Serializable
    class Args_updateConfig(
        val newStorageCost: ULong?,
        @Serializable(with = U128Serializer::class) val newStorageAvailable: U128?,
        @Serializable(with = PublicKeyAs32ByteSerializer::class) val newAdmin2: PublicKey?,
        val newMaxAcctSize: ULong?,
        val newMinAcctSize: ULong?
    )

    @Serializable
    class Args_mutableFees(val shadesPerGbPerEpoch: ULong?, val crankBps: UInt?)

    @Serializable
    class Args_initializeAccount(
        val identifier: String,
        val storage: ULong,
        @Serializable(with = PublicKeyAs32ByteSerializer::class) val owner2: PublicKey?
    )

    @Serializable
    class Args_initializeAccount2(val identifier: String, val storage: ULong)

    @Serializable
    class Args_updateAccount(val identifier: String?, @Serializable(with =
            PublicKeyAs32ByteSerializer::class) val owner2: PublicKey?)

    @Serializable
    class Args_updateAccount2(val identifier: String?)

    @Serializable
    class Args_requestDeleteAccount()

    @Serializable
    class Args_requestDeleteAccount2()

    @Serializable
    class Args_unmarkDeleteAccount()

    @Serializable
    class Args_unmarkDeleteAccount2()

    @Serializable
    class Args_redeemRent()

    @Serializable
    class Args_deleteAccount()

    @Serializable
    class Args_deleteAccount2()

    @Serializable
    class Args_makeAccountImmutable()

    @Serializable
    class Args_makeAccountImmutable2()

    @Serializable
    class Args_badCsam(val storageAvailable: ULong)

    @Serializable
    class Args_badCsam2(val storageAvailable: ULong)

    @Serializable
    class Args_increaseStorage(val additionalStorage: ULong)

    @Serializable
    class Args_increaseStorage2(val additionalStorage: ULong)

    @Serializable
    class Args_increaseImmutableStorage(val additionalStorage: ULong)

    @Serializable
    class Args_increaseImmutableStorage2(val additionalStorage: ULong)

    @Serializable
    class Args_decreaseStorage(val removeStorage: ULong)

    @Serializable
    class Args_decreaseStorage2(val removeStorage: ULong)

    @Serializable
    class Args_claimStake()

    @Serializable
    class Args_claimStake2()

    @Serializable
    class Args_crank()

    @Serializable
    class Args_crank2()

    @Serializable
    class Args_refreshStake()

    @Serializable
    class Args_refreshStake2()

    @Serializable
    class Args_migrateStep1()

    @Serializable
    class Args_migrateStep2()
}
