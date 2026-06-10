# Yalla Khedma — Cloud Functions

Server-side OTP (generate, send, verify) for email verification and the
provider profile-edit gate. Replaces the old client-side OTP, so:

- the verification code is generated, stored **hashed**, and verified **only**
  on the server — a tampered client can't read or fake it;
- the SMTP credentials live in **Secret Manager**, never in the app binary.

## Functions

| Name        | Type     | Input                          | Effect |
|-------------|----------|--------------------------------|--------|
| `sendOtp`   | callable | `{ purpose }`                  | Generates a 4-digit code, stores its SHA-256 hash in `otps/{uid}_{purpose}` (2-min TTL, 5-attempt cap, 30s resend cooldown), emails it. |
| `verifyOtp` | callable | `{ purpose, code }`            | Validates the code; on success consumes it and (for `email_verify`) sets `users/{uid}.emailVerified = true`. |

`purpose` is one of `email_verify` or `profile_edit`.

## One-time setup

```bash
cd functions
npm install

# Set the SMTP secrets (you'll be prompted for each value):
firebase functions:secrets:set SMTP_EMAIL       # e.g. yallakhidmasup@mail.ru
firebase functions:secrets:set SMTP_PASSWORD    # the provider app-password
firebase functions:secrets:set SMTP_HOST        # e.g. smtp.mail.ru
firebase functions:secrets:set SMTP_PORT        # e.g. 465
```

## Deploy

```bash
firebase deploy --only functions
# rules too, if changed:
firebase deploy --only firestore:rules
```

## Local run (emulator)

```bash
npm run serve
```

## Region

Deployed to `us-central1`. If you change it, update `REGION` in
`shared/.../data/auth/OtpService.kt` to match.
