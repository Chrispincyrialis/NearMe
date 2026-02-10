package com.nearme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nearme.model.Merchant
import com.nearme.model.Transaction
import com.nearme.model.User
import com.nearme.model.Wallet

@Database(
    entities = [User::class, Wallet::class, Merchant::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NearMeDatabase : RoomDatabase() {
    abstract fun nearMeDao(): NearMeDao

    companion object {
        @Volatile
        private var INSTANCE: NearMeDatabase? = null

        fun getInstance(context: Context): NearMeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NearMeDatabase::class.java,
                    "nearme.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
