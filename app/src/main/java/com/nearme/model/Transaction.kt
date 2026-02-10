package com.nearme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val amountCents: Long,
    val timestamp: Long,
    val direction: Direction,
    val channel: Channel,
    val integrityHash: String
)

enum class Direction {
    SENT,
    RECEIVED
}

enum class Channel {
    PHONE_TO_PHONE,
    PHONE_TO_TAG
}
