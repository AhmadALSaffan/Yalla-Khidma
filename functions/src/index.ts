/**
 * Yalla Khedma — server-side OTP.
 *
 * Resolves the two client-side weaknesses:
 *   1. SMTP credentials live in Secret Manager, never in the app binary.
 *   2. The code is generated, stored (hashed), and verified ONLY on the server —
 *      a tampered client can't read or fake it.
 *
 * Two callable functions:
 *   - sendOtp({ purpose })          → generates + emails a 4-digit code
 *   - verifyOtp({ purpose, code })  → validates; for "email_verify" flips
 *                                      users/{uid}.emailVerified = true
 *
 * The code is stored hashed (SHA-256 + per-code salt) in otps/{uid}_{purpose},
 * with a 2-minute TTL, a 5-attempt cap, and a 30s resend cooldown.
 */
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as admin from "firebase-admin";
import * as crypto from "crypto";
import * as nodemailer from "nodemailer";

admin.initializeApp();
const db = admin.firestore();

// Configure via:  firebase functions:secrets:set SMTP_EMAIL  (and SMTP_PASSWORD, SMTP_HOST, SMTP_PORT)
const SMTP_EMAIL = defineSecret("SMTP_EMAIL");
const SMTP_PASSWORD = defineSecret("SMTP_PASSWORD");
const SMTP_HOST = defineSecret("SMTP_HOST");
const SMTP_PORT = defineSecret("SMTP_PORT");

const CODE_LENGTH = 4;
const CODE_TTL_MS = 2 * 60 * 1000; // 2 minutes
const MAX_ATTEMPTS = 5;
const RESEND_COOLDOWN_MS = 30 * 1000; // 30 seconds
const ALLOWED_PURPOSES = ["email_verify", "profile_edit"];

type OtpDoc = {
  codeHash: string;
  salt: string;
  purpose: string;
  expiresAt: number;
  attempts: number;
  createdAt: number;
};

function docId(uid: string, purpose: string): string {
  return `${uid}_${purpose}`;
}

function hashCode(code: string, salt: string): string {
  return crypto.createHash("sha256").update(`${salt}:${code}`).digest("hex");
}

function generateCode(): string {
  let out = "";
  for (let i = 0; i < CODE_LENGTH; i++) out += crypto.randomInt(0, 10).toString();
  return out;
}

function assertAuthed(uid: string | undefined): asserts uid is string {
  if (!uid) throw new HttpsError("unauthenticated", "يجب تسجيل الدخول أولاً");
}

function assertPurpose(purpose: unknown): asserts purpose is string {
  if (typeof purpose !== "string" || !ALLOWED_PURPOSES.includes(purpose)) {
    throw new HttpsError("invalid-argument", "غرض غير صالح");
  }
}

// ---------------------------------------------------------------------------
// sendOtp
// ---------------------------------------------------------------------------
export const sendOtp = onCall(
  { secrets: [SMTP_EMAIL, SMTP_PASSWORD, SMTP_HOST, SMTP_PORT], region: "us-central1" },
  async (request) => {
    const uid = request.auth?.uid;
    assertAuthed(uid);
    const purpose = request.data?.purpose;
    assertPurpose(purpose);

    const ref = db.collection("otps").doc(docId(uid, purpose));
    const now = Date.now();

    // Resend cooldown — don't let a client spam emails.
    const existing = (await ref.get()).data() as OtpDoc | undefined;
    if (existing && now - existing.createdAt < RESEND_COOLDOWN_MS) {
      throw new HttpsError("resource-exhausted", "انتظر قليلاً قبل إعادة الإرسال");
    }

    // Resolve the recipient from the verified auth token, never from the client.
    const user = await admin.auth().getUser(uid);
    const email = user.email;
    if (!email) throw new HttpsError("failed-precondition", "لا يوجد بريد للحساب");

    const code = generateCode();
    const salt = crypto.randomBytes(16).toString("hex");
    const otp: OtpDoc = {
      codeHash: hashCode(code, salt),
      salt,
      purpose,
      expiresAt: now + CODE_TTL_MS,
      attempts: 0,
      createdAt: now,
    };
    await ref.set(otp);

    const transporter = nodemailer.createTransport({
      host: SMTP_HOST.value(),
      port: Number(SMTP_PORT.value()),
      secure: Number(SMTP_PORT.value()) === 465,
      auth: { user: SMTP_EMAIL.value(), pass: SMTP_PASSWORD.value() },
    });

    await transporter.sendMail({
      from: `"يلّا خِدمة" <${SMTP_EMAIL.value()}>`,
      to: email,
      subject: "رمز التحقق - يلّا خِدمة",
      html: buildHtml(code),
    });

    return { sent: true };
  }
);

// ---------------------------------------------------------------------------
// verifyOtp
// ---------------------------------------------------------------------------
export const verifyOtp = onCall({ region: "us-central1" }, async (request) => {
  const uid = request.auth?.uid;
  assertAuthed(uid);
  const purpose = request.data?.purpose;
  assertPurpose(purpose);
  const code = request.data?.code;
  if (typeof code !== "string" || !/^\d{4}$/.test(code)) {
    throw new HttpsError("invalid-argument", "رمز غير صالح");
  }

  const ref = db.collection("otps").doc(docId(uid, purpose));
  const snap = await ref.get();
  const otp = snap.data() as OtpDoc | undefined;
  if (!otp) throw new HttpsError("not-found", "اطلب رمزاً جديداً");

  if (Date.now() > otp.expiresAt) {
    await ref.delete();
    throw new HttpsError("deadline-exceeded", "انتهت صلاحية الرمز");
  }
  if (otp.attempts >= MAX_ATTEMPTS) {
    await ref.delete();
    throw new HttpsError("resource-exhausted", "تجاوزت عدد المحاولات، اطلب رمزاً جديداً");
  }

  const matches = hashCode(code, otp.salt) === otp.codeHash;
  if (!matches) {
    await ref.update({ attempts: admin.firestore.FieldValue.increment(1) });
    const left = MAX_ATTEMPTS - (otp.attempts + 1);
    throw new HttpsError("permission-denied", `الرمز غير صحيح. المحاولات المتبقية: ${Math.max(left, 0)}`);
  }

  // Success — consume the code and apply the side effect.
  await ref.delete();
  if (purpose === "email_verify") {
    await db.collection("users").doc(uid).update({ emailVerified: true });
  }
  return { verified: true };
});

function buildHtml(code: string): string {
  return `
  <!DOCTYPE html>
  <html dir="rtl" lang="ar">
  <body style="margin:0;padding:0;background:#f4f4f7;font-family:Arial,'Segoe UI',Tahoma,sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f4f7;padding:24px 0;">
      <tr><td align="center">
        <table width="440" cellpadding="0" cellspacing="0"
               style="background:#ffffff;border-radius:16px;overflow:hidden;
                      box-shadow:0 2px 10px rgba(0,0,0,0.06);max-width:440px;width:100%;">
          <tr><td style="background:#FF6B35;padding:28px;text-align:center;">
            <div style="font-size:26px;font-weight:bold;color:#ffffff;">يلّا خِدمة</div>
            <div style="font-size:13px;color:#ffe6db;margin-top:4px;">خِدمَتُك بضغطة زِر</div>
          </td></tr>
          <tr><td style="padding:32px 28px;text-align:center;color:#1a1a1a;">
            <h1 style="font-size:20px;margin:0 0 8px;">رمز التحقق</h1>
            <p style="font-size:15px;color:#64748b;margin:0 0 24px;">
              استخدم الرمز التالي لتأكيد بريدك الإلكتروني
            </p>
            <div style="display:inline-block;background:#FFF1EB;border:1px dashed #FF6B35;
                        border-radius:12px;padding:16px 28px;">
              <span style="font-size:36px;font-weight:bold;letter-spacing:14px;
                           color:#FF6B35;direction:ltr;">${code}</span>
            </div>
            <p style="font-size:13px;color:#94a3b8;margin:24px 0 0;line-height:20px;">
              الرمز صالح لمدة دقيقتين.<br>
              إذا لم تطلب هذا الرمز، يمكنك تجاهل هذه الرسالة بأمان.
            </p>
          </td></tr>
          <tr><td style="background:#faf8ff;padding:16px;text-align:center;
                         color:#94a3b8;font-size:12px;">© يلّا خِدمة</td></tr>
        </table>
      </td></tr>
    </table>
  </body>
  </html>`;
}
