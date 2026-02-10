package com.nearme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchants")
data class Merchant(
    @PrimaryKey val merchantId: String,
    val name: String,
    val defaultRequestAmountCents: Long?
)
