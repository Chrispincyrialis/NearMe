package com.nearme.ui

import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nearme.R
import com.nearme.data.NearMeDatabase
import com.nearme.data.NearMeRepository
import com.nearme.model.Merchant
import com.nearme.nfc.NfcHandler
import com.nearme.nfc.NfcPayload
import com.nearme.security.SecurityUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var nfcHandler: NfcHandler
    private lateinit var repository: NearMeRepository

    private lateinit var statusText: TextView
    private lateinit var amountInput: EditText
    private lateinit var targetInput: EditText
    private lateinit var pinInput: EditText

    private val localUserId = "user_alice"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcHandler = NfcHandler(this)
        repository = NearMeRepository(NearMeDatabase.getInstance(this).nearMeDao())

        statusText = findViewById(R.id.statusText)
        amountInput = findViewById(R.id.amountInput)
        targetInput = findViewById(R.id.targetInput)
        pinInput = findViewById(R.id.pinInput)

        findViewById<Button>(R.id.sendPhoneButton).setOnClickListener {
            sendPhonePayment()
        }

        findViewById<Button>(R.id.sendMerchantButton).setOnClickListener {
            payMerchantFromTagData()
        }

        lifecycleScope.launch {
            repository.seedWallet(localUserId, 500_00)
            repository.seedWallet("user_bob", 250_00)
        }

        handleIntent()
    }

    override fun onResume() {
        super.onResume()
        nfcHandler.enableForegroundDispatch()
    }

    override fun onPause() {
        nfcHandler.disableForegroundDispatch()
        super.onPause()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }

    private fun sendPhonePayment() {
        val receiverId = targetInput.text.toString().ifBlank { "user_bob" }
        val amountCents = (amountInput.text.toString().toDoubleOrNull()?.times(100))?.toLong() ?: 0L

        if (!verifyPin()) return

        val timestamp = System.currentTimeMillis()
        val baseString = "$localUserId|$receiverId|$amountCents|$timestamp"
        val hash = SecurityUtils.hashSha256(baseString)
        val payload = NfcPayload(localUserId, receiverId, amountCents, timestamp, hash)

        val message = nfcHandler.buildTransactionMessage(payload.toJson())
        statusText.text = getString(R.string.nfc_ready_message, message.records.size)
    }

    private fun payMerchantFromTagData() {
        if (!verifyPin()) return

        val merchantId = targetInput.text.toString().ifBlank { "merchant_demo" }
        val amountCents = (amountInput.text.toString().toDoubleOrNull()?.times(100))?.toLong() ?: 100L
        val timestamp = System.currentTimeMillis()
        val hash = SecurityUtils.hashSha256("$localUserId|$merchantId|$amountCents|$timestamp")

        lifecycleScope.launch {
            val merchant = Merchant(merchantId = merchantId, name = "Campus Cafe", defaultRequestAmountCents = amountCents)
            val result = repository.processMerchantPayment(localUserId, merchant, amountCents, timestamp, hash)
            statusText.text = result.fold(
                onSuccess = { getString(R.string.transaction_ok, it.amountCents / 100.0, it.receiverId) },
                onFailure = { getString(R.string.transaction_error, it.message ?: "Unknown") }
            )
        }
    }

    private fun handleIntent() {
        val intent = intent ?: return
        val action = intent.action ?: return

        if (action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val payloadRaw = nfcHandler.extractPayload(intent) ?: return
            val payload = NfcPayload.fromJson(payloadRaw)
            val expectedHash = SecurityUtils.hashSha256(
                "${payload.senderId}|${payload.receiverId}|${payload.amountCents}|${payload.timestamp}"
            )

            if (payload.hash != expectedHash) {
                Toast.makeText(this, R.string.invalid_hash, Toast.LENGTH_SHORT).show()
                return
            }

            lifecycleScope.launch {
                val result = repository.processPhonePayment(
                    senderId = payload.senderId,
                    receiverId = payload.receiverId,
                    amountCents = payload.amountCents,
                    timestamp = payload.timestamp,
                    integrityHash = payload.hash
                )

                statusText.text = result.fold(
                    onSuccess = { getString(R.string.transaction_ok, it.amountCents / 100.0, it.receiverId) },
                    onFailure = { getString(R.string.transaction_error, it.message ?: "Unknown") }
                )
            }
        }

        if (action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tagId = nfcHandler.readTagId(intent) ?: return
            statusText.text = getString(R.string.tag_detected, tagId)
        }
    }

    private fun verifyPin(): Boolean {
        val enteredPin = pinInput.text.toString()
        val expectedHash = SecurityUtils.hashSha256("1234")
        val valid = SecurityUtils.validatePin(enteredPin, expectedHash)
        if (!valid) {
            Toast.makeText(this, R.string.invalid_pin, Toast.LENGTH_SHORT).show()
        }
        return valid
    }
}
