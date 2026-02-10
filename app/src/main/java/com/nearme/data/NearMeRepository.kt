package com.nearme.data

import com.nearme.model.Channel
import com.nearme.model.Direction
import com.nearme.model.Merchant
import com.nearme.model.Transaction
import com.nearme.model.Wallet
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NearMeRepository(private val dao: NearMeDao) {
    fun observeTransactions(): Flow<List<Transaction>> = dao.observeTransactions()

    suspend fun processPhonePayment(
        senderId: String,
        receiverId: String,
        amountCents: Long,
        timestamp: Long,
        integrityHash: String
    ): Result<Transaction> {
        if (amountCents <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))

        val senderWallet = dao.getWallet(senderId) ?: return Result.failure(IllegalStateException("Sender wallet missing"))
        val receiverWallet = dao.getWallet(receiverId) ?: return Result.failure(IllegalStateException("Receiver wallet missing"))

        if (senderWallet.balanceCents < amountCents) {
            return Result.failure(IllegalStateException("Insufficient balance"))
        }

        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            amountCents = amountCents,
            timestamp = timestamp,
            direction = Direction.SENT,
            channel = Channel.PHONE_TO_PHONE,
            integrityHash = integrityHash
        )

        dao.upsertWallet(senderWallet.copy(balanceCents = senderWallet.balanceCents - amountCents))
        dao.upsertWallet(receiverWallet.copy(balanceCents = receiverWallet.balanceCents + amountCents))
        dao.insertTransaction(txn)

        return Result.success(txn)
    }

    suspend fun processMerchantPayment(
        userId: String,
        merchant: Merchant,
        amountCents: Long,
        timestamp: Long,
        integrityHash: String
    ): Result<Transaction> {
        val wallet = dao.getWallet(userId) ?: return Result.failure(IllegalStateException("User wallet missing"))
        if (wallet.balanceCents < amountCents) return Result.failure(IllegalStateException("Insufficient balance"))

        dao.upsertWallet(wallet.copy(balanceCents = wallet.balanceCents - amountCents))
        dao.upsertMerchant(merchant)

        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            senderId = userId,
            receiverId = merchant.merchantId,
            amountCents = amountCents,
            timestamp = timestamp,
            direction = Direction.SENT,
            channel = Channel.PHONE_TO_TAG,
            integrityHash = integrityHash
        )
        dao.insertTransaction(txn)
        return Result.success(txn)
    }

    suspend fun seedWallet(userId: String, balanceCents: Long) {
        dao.upsertWallet(Wallet(userId = userId, balanceCents = balanceCents))
    }
}
