package com.solanamobile.mintyfresh.mintycore.metaplex.jen.shadowdrive

import kotlin.Int
import kotlin.String

//
// Errors
//
// This code was generated locally by Funkatronics on 2023-01-24
//
sealed interface ShadowDriveError {
    val code: Int

    val message: String
}

class NotEnoughStorage : ShadowDriveError {
    override val code: Int = 6000

    override val message: String = "Not enough storage available on this Storage Account"
}

class FileNameLengthExceedsLimit : ShadowDriveError {
    override val code: Int = 6001

    override val message: String = "The length of the file name exceeds the limit of 32 bytes"
}

class InvalidSha256Hash : ShadowDriveError {
    override val code: Int = 6002

    override val message: String = "Invalid sha256 hash"
}

class HasHadBadCsam : ShadowDriveError {
    override val code: Int = 6003

    override val message: String = "User at some point had a bad csam scan"
}

class StorageAccountMarkedImmutable : ShadowDriveError {
    override val code: Int = 6004

    override val message: String = "Storage account is marked as immutable"
}

class ClaimingStakeTooSoon : ShadowDriveError {
    override val code: Int = 6005

    override val message: String = "User has not waited enough time to claim stake"
}

class SolanaStorageAccountNotMutable : ShadowDriveError {
    override val code: Int = 6006

    override val message: String =
            "The storage account needs to be marked as mutable to update last fee collection epoch"
}

class RemovingTooMuchStorage : ShadowDriveError {
    override val code: Int = 6007

    override val message: String = "Attempting to decrease storage by more than is available"
}

class UnsignedIntegerCastFailed : ShadowDriveError {
    override val code: Int = 6008

    override val message: String = "u128 -> u64 cast failed"
}

class NonzeroRemainingFileAccounts : ShadowDriveError {
    override val code: Int = 6009

    override val message: String =
            "This storage account still has some file accounts associated with it that have not been deleted"
}

class AccountStillInGracePeriod : ShadowDriveError {
    override val code: Int = 6010

    override val message: String = "This account is still within deletion grace period"
}

class AccountNotMarkedToBeDeleted : ShadowDriveError {
    override val code: Int = 6011

    override val message: String = "This account is not marked to be deleted"
}

class FileStillInGracePeriod : ShadowDriveError {
    override val code: Int = 6012

    override val message: String = "This file is still within deletion grace period"
}

class FileNotMarkedToBeDeleted : ShadowDriveError {
    override val code: Int = 6013

    override val message: String = "This file is not marked to be deleted"
}

class FileMarkedImmutable : ShadowDriveError {
    override val code: Int = 6014

    override val message: String = "File has been marked as immutable and cannot be edited"
}

class NoStorageIncrease : ShadowDriveError {
    override val code: Int = 6015

    override val message: String = "User requested an increase of zero bytes"
}

class ExceededStorageLimit : ShadowDriveError {
    override val code: Int = 6016

    override val message: String = "Requested a storage account with storage over the limit"
}

class InsufficientFunds : ShadowDriveError {
    override val code: Int = 6017

    override val message: String =
            "User does not have enough funds to store requested number of bytes."
}

class NotEnoughStorageOnShadowDrive : ShadowDriveError {
    override val code: Int = 6018

    override val message: String = "There is not available storage on Shadow Drive. Good job!"
}

class AccountTooSmall : ShadowDriveError {
    override val code: Int = 6019

    override val message: String = "Requested a storage account with storage under the limit"
}

class DidNotAgreeToToS : ShadowDriveError {
    override val code: Int = 6020

    override val message: String = "User did not agree to terms of service"
}

class InvalidTokenTransferAmounts : ShadowDriveError {
    override val code: Int = 6021

    override val message: String = "Invalid token transfers. Stake account nonempty."
}

class FailedToCloseAccount : ShadowDriveError {
    override val code: Int = 6022

    override val message: String = "Failed to close spl token account"
}

class FailedToTransferToEmissionsWallet : ShadowDriveError {
    override val code: Int = 6023

    override val message: String = "Failed to transfer to emissions wallet"
}

class FailedToTransferToEmissionsWalletFromUser : ShadowDriveError {
    override val code: Int = 6024

    override val message: String = "Failed to transfer to emissions wallet from user"
}

class FailedToReturnUserFunds : ShadowDriveError {
    override val code: Int = 6025

    override val message: String = "Failed to return user funds"
}

class NeedSomeFees : ShadowDriveError {
    override val code: Int = 6026

    override val message: String = "Turning on fees and passing in None for storage cost per epoch"
}

class NeedSomeCrankBps : ShadowDriveError {
    override val code: Int = 6027

    override val message: String = "Turning on fees and passing in None for crank bps"
}

class AlreadyMarkedForDeletion : ShadowDriveError {
    override val code: Int = 6028

    override val message: String = "This account is already marked to be deleted"
}

class EmptyStakeAccount : ShadowDriveError {
    override val code: Int = 6029

    override val message: String =
            "User has an empty stake account and must refresh stake account before unmarking account for deletion"
}

class IdentifierExceededMaxLength : ShadowDriveError {
    override val code: Int = 6030

    override val message: String = "New identifier exceeds maximum length of 64 bytes"
}

class OnlyAdmin1CanChangeAdmins : ShadowDriveError {
    override val code: Int = 6031

    override val message: String = "Only admin1 can change admins"
}

class OnlyOneOwnerAllowedInV1_5 : ShadowDriveError {
    override val code: Int = 6032

    override val message: String =
            "(As part of on-chain storage optimizations, only one owner is allowed in Shadow Drive v1.5)"
}
