package com.nearme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey val userId: String,
    val balanceCents: Long
)
