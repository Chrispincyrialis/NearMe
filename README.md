## NearMe (Android NFC Prototype)

NearMe is an Android prototype for simulating NFC-based transactions for an Information Systems final year project. The app demonstrates:

- phone-to-phone payload exchange via NDEF
- phone-to-tag merchant payment flow
- local persistence of wallets and transaction history (Room)
- simple transaction integrity hashing
- PIN confirmation before transfer approval

> This is a simulation app only. No real money movement occurs.

## Implemented architecture

The codebase is structured with OOP-focused domain classes and handlers:

- `User`, `Wallet`, `Transaction`, `Merchant` entities in Room
- `NfcHandler` for NFC setup, payload extraction, and NDEF operations
- `NearMeRepository` for payment logic and balance updates
- `SecurityUtils` for PIN validation + SHA-256 integrity checks
- `MainActivity` for demo UI and interaction flow

## NFC flows covered

### 1) Phone-to-phone

1. Sender fills receiver + amount and confirms PIN.
2. App prepares an NDEF transaction payload.
3. Receiver processes payload from `ACTION_NDEF_DISCOVERED`.
4. Hash integrity is validated.
5. Local balances and transaction log are updated.

### 2) Phone-to-tag (merchant simulation)

1. Tag is detected with foreground dispatch.
2. Merchant ID + amount are used to create a simulated payment.
3. Wallet is debited and receipt-like confirmation is shown.

## Storage model

Room database: `nearme.db`

Tables:

- `users`
- `wallets`
- `merchants`
- `transactions`

## Build prerequisites

- Android Studio Iguana+ (or compatible with AGP 8.5.x)
- Android SDK 34
- NFC-capable test device(s)

## Quick run

1. Open project in Android Studio.
2. Let Gradle sync.
3. Run on NFC-enabled Android device.
4. Use demo PIN: `1234`.
5. Try:
   - `user_bob` as receiver for phone-to-phone demo
   - `merchant_demo` for tag payment simulation

## Next suggested extensions

- Use HCE for richer emulated card behavior.
- Add biometric prompt in place of fixed demo PIN.
- Add backend sync (REST/WebSocket) for cross-device reporting.
- Add signed payloads with asymmetric keys for stronger integrity/authenticity.
