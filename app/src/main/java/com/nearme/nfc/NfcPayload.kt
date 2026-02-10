package com.nearme.nfc

import org.json.JSONObject

data class NfcPayload(
    val senderId: String,
    val receiverId: String,
    val amountCents: Long,
    val timestamp: Long,
    val hash: String
) {
    fun toJson(): String = JSONObject().apply {
        put("senderId", senderId)
        put("receiverId", receiverId)
        put("amountCents", amountCents)
        put("timestamp", timestamp)
        put("hash", hash)
    }.toString()

    companion object {
        fun fromJson(raw: String): NfcPayload {
            val json = JSONObject(raw)
            return NfcPayload(
                senderId = json.getString("senderId"),
                receiverId = json.getString("receiverId"),
                amountCents = json.getLong("amountCents"),
                timestamp = json.getLong("timestamp"),
                hash = json.getString("hash")
            )
        }
    }
}
