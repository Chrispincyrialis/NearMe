package com.nearme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nearme.model.Merchant
import com.nearme.model.Transaction
import com.nearme.model.User
import com.nearme.model.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface NearMeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: Wallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMerchant(merchant: Merchant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWallet(userId: String): Wallet?

    @Query("SELECT * FROM merchants WHERE merchantId = :merchantId")
    suspend fun getMerchant(merchantId: String): Merchant?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<Transaction>>
}
