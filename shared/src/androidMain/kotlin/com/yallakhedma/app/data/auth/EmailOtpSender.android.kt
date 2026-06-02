package com.yallakhedma.app.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Sends the OTP over SMTP using JavaMail. The SMTP host/port are derived from
 * the sender email's domain so the same code works for Gmail, Mail.ru, etc.
 * [senderAppPassword] must be a provider "app password", not the account's
 * normal login password.
 */
actual class EmailOtpSender(
    private val senderEmail: String,
    private val senderAppPassword: String,
) {

    private data class SmtpConfig(val host: String, val port: String, val ssl: Boolean)

    private fun configFor(email: String): SmtpConfig {
        val domain = email.substringAfter('@', "").lowercase()
        return when {
            domain == "mail.ru" || domain.endsWith(".mail.ru") ||
                domain == "inbox.ru" || domain == "list.ru" || domain == "bk.ru" ->
                SmtpConfig("smtp.mail.ru", "465", ssl = true)
            domain == "gmail.com" || domain == "googlemail.com" ->
                SmtpConfig("smtp.gmail.com", "587", ssl = false)
            domain == "outlook.com" || domain == "hotmail.com" || domain == "live.com" ->
                SmtpConfig("smtp-mail.outlook.com", "587", ssl = false)
            domain == "yandex.com" || domain == "yandex.ru" ->
                SmtpConfig("smtp.yandex.com", "465", ssl = true)
            else -> SmtpConfig("smtp.$domain", "587", ssl = false)
        }
    }

    actual suspend fun sendCode(toEmail: String, code: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val cfg = configFor(senderEmail)
                Log.i(
                    "EmailOtpSender",
                    "Sending OTP from $senderEmail to $toEmail via ${cfg.host}:${cfg.port} ssl=${cfg.ssl}",
                )
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.host", cfg.host)
                    put("mail.smtp.port", cfg.port)
                    if (cfg.ssl) {
                        // Implicit TLS (port 465).
                        put("mail.smtp.ssl.enable", "true")
                        put("mail.smtp.socketFactory.port", cfg.port)
                        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    } else {
                        // STARTTLS (port 587).
                        put("mail.smtp.starttls.enable", "true")
                    }
                }
                val session = Session.getInstance(
                    props,
                    object : Authenticator() {
                        override fun getPasswordAuthentication() =
                            PasswordAuthentication(senderEmail, senderAppPassword)
                    },
                )
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail, "يلّا خِدمة"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    subject = "رمز التحقق - يلّا خِدمة"
                    setContent(buildHtml(code), "text/html; charset=UTF-8")
                }
                Transport.send(message)
                Log.i("EmailOtpSender", "OTP email sent to $toEmail via ${cfg.host}")
                true
            }.getOrElse { e ->
                // Put the reason on the main line so a simple filter catches it.
                Log.e("EmailOtpSender", "Failed to send OTP email: ${e::class.simpleName}: ${e.message}", e)
                false
            }
        }

    private fun buildHtml(code: String): String = """
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
                                 color:#FF6B35;direction:ltr;">$code</span>
                  </div>
                  <p style="font-size:13px;color:#94a3b8;margin:24px 0 0;line-height:20px;">
                    الرمز صالح لمدة دقيقتين.<br>
                    إذا لم تطلب هذا الرمز، يمكنك تجاهل هذه الرسالة بأمان.
                  </p>
                </td></tr>
                <tr><td style="background:#faf8ff;padding:16px;text-align:center;
                               color:#94a3b8;font-size:12px;">
                  © يلّا خِدمة
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
    """.trimIndent()
}
