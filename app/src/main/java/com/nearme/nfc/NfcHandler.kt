package com.nearme.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef

class NfcHandler(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun enableForegroundDispatch() {
        val intent = Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefFilter.addDataType(MIME_TYPE)
        } catch (_: IntentFilter.MalformedMimeTypeException) {
            return
        }

        val intentFilters = arrayOf(ndefFilter, IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))

        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
    }

    fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun buildTransactionMessage(payload: String): NdefMessage {
        return NdefMessage(
            arrayOf(
                NdefRecord.createMime(MIME_TYPE, payload.toByteArray())
            )
        )
    }

    fun extractPayload(intent: Intent): String? {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) ?: return null
        val first = rawMessages.firstOrNull() as? NdefMessage ?: return null
        val record = first.records.firstOrNull() ?: return null
        return record.payload.decodeToString()
    }

    fun readTagId(intent: Intent): String? {
        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java) ?: return null
        return tag.id.joinToString("") { "%02X".format(it) }
    }

    fun writeMessageToTag(tag: Tag, message: NdefMessage): Boolean {
        val ndef = Ndef.get(tag) ?: return false
        return runCatching {
            ndef.connect()
            ndef.writeNdefMessage(message)
            true
        }.getOrElse { false }
            .also { runCatching { ndef.close() } }
    }

    companion object {
        const val MIME_TYPE = "application/vnd.com.nearme.transaction"
    }
}
