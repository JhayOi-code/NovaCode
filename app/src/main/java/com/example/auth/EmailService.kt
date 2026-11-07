package com.example.auth

import android.os.Build
import android.util.Log
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DispatchedEmail(
    val recipient: String,
    val sender: String = "LinqStudiosLLC <noreply@linqstudios.com>",
    val subject: String,
    val bodyHtml: String,
    val sentAtTimestamp: Long = System.currentTimeMillis()
)

object EmailService {

    private const val TAG = "LinqEmailService"
    private val sentLog = mutableListOf<DispatchedEmail>()
    private var lastWelcomeBackMs: Long = 0L

    // Brand logo banner for email templates: clean high-contrast card header
    private const val EMAIL_LOGO_BANNER = """
        <div style="background:#FFFFFF;border-radius:10px;padding:8px 18px;display:inline-block;margin-bottom:18px;text-align:center;">
          <h2 style="margin:0;color:#0F172A;font-family:sans-serif;font-size:22px;font-weight:bold;letter-spacing:-0.5px;">&lt;Linq Studio/&gt;</h2>
        </div>
    """

    fun getSentEmails(): List<DispatchedEmail> = sentLog.toList()

    fun sendWelcomeEmail(recipientEmail: String, name: String) {
        val subject = "Welcome to Linq Studio, $name!"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><style>body{font-family:sans-serif;background:#0F172A;color:#F8FAFC;padding:24px;}.card{background:#1E293B;padding:24px;border-radius:12px;border:1px solid #334155;}a{color:#8B5CF6;}</style></head>
            <body>
              <div class="card">
                $EMAIL_LOGO_BANNER
                <h2>Welcome to Linq Studio, $name!</h2>
                <p>Your full-stack mobile development studio is ready. Build, compile, and run Python, Java, Kotlin, C/C++, Rust, Go, JavaScript, and HTML directly on your device offline without external servers.</p>
                <p>Happy coding!<br/>— <strong>LinqStudiosLLC Team</strong></p>
              </div>
            </body>
            </html>
        """.trimIndent()

        dispatchEmail(recipientEmail, subject, html)
    }

    fun sendWelcomeBackEmail(recipientEmail: String, name: String, allowSend: Boolean = true) {
        if (!allowSend) return
        val now = System.currentTimeMillis()
        if (now - lastWelcomeBackMs < 30 * 60 * 1000L) {
            // Throttled: at most once per 30 minutes
            return
        }
        lastWelcomeBackMs = now

        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())
        val subject = "Welcome back to Linq Studio, $name!"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><style>body{font-family:sans-serif;background:#0F172A;color:#F8FAFC;padding:24px;}.card{background:#1E293B;padding:24px;border-radius:12px;border:1px solid #334155;}</style></head>
            <body>
              <div class="card">
                $EMAIL_LOGO_BANNER
                <h2>Welcome back, $name!</h2>
                <p>You signed in to Linq Studio on <strong>$dateStr</strong>.</p>
                <p>All your workspaces and files are synchronized and ready.</p>
                <p>— <strong>LinqStudiosLLC</strong></p>
              </div>
            </body>
            </html>
        """.trimIndent()

        dispatchEmail(recipientEmail, subject, html)
    }

    fun sendNewDeviceLoginEmail(recipientEmail: String, deviceId: String, deviceModel: String) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())
        val subject = "New device login to your Linq Studio account"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><style>body{font-family:sans-serif;background:#0F172A;color:#F8FAFC;padding:24px;}.card{background:#1E293B;padding:24px;border-radius:12px;border:1px solid #EF4444;}.warning{color:#EF4444;font-weight:bold;}</style></head>
            <body>
              <div class="card">
                $EMAIL_LOGO_BANNER
                <h2 class="warning">Security Alert: New Device Login</h2>
                <p>Your Linq Studio account was accessed from a new device:</p>
                <ul>
                  <li><strong>Device:</strong> $deviceModel</li>
                  <li><strong>Platform:</strong> Android (SDK ${Build.VERSION.SDK_INT})</li>
                  <li><strong>Time:</strong> $dateStr</li>
                  <li><strong>Session ID:</strong> $deviceId</li>
                </ul>
                <p>If this was you, you can ignore this email. If this wasn't you, secure your account immediately by signing out of all active sessions.</p>
                <p>— <strong>LinqStudiosLLC Security</strong></p>
              </div>
            </body>
            </html>
        """.trimIndent()

        dispatchEmail(recipientEmail, subject, html)
    }

    fun sendSecurityAlertEmail(recipientEmail: String, alertType: String, deviceModel: String) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())
        val subject = "Security Alert: Suspicious account activity"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><style>body{font-family:sans-serif;background:#0F172A;color:#F8FAFC;padding:24px;}.card{background:#1E293B;padding:24px;border-radius:12px;border:1px solid #EF4444;}.warning{color:#EF4444;font-weight:bold;}</style></head>
            <body>
              <div class="card">
                $EMAIL_LOGO_BANNER
                <h2 class="warning">Security Alert: Suspicious Activity</h2>
                <p>Suspicious activity was detected for <strong>$recipientEmail</strong> on <strong>$deviceModel</strong> at $dateStr ($alertType).</p>
                <p>If this was not you, your account may be targeted. We recommend reviewing your Linq Studio account security.</p>
                <p>— <strong>LinqStudiosLLC Security</strong></p>
              </div>
            </body>
            </html>
        """.trimIndent()

        dispatchEmail(recipientEmail, subject, html)
    }

    fun sendPasswordResetEmail(recipientEmail: String, name: String) {
        val subject = "Reset your Linq Studio password"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><style>body{font-family:sans-serif;background:#0F172A;color:#F8FAFC;padding:24px;}.card{background:#1E293B;padding:24px;border-radius:12px;border:1px solid #334155;}a{color:#00E5FF;font-weight:bold;}</style></head>
            <body>
              <div class="card">
                $EMAIL_LOGO_BANNER
                <h2>Password Reset Request</h2>
                <p>Hello $name,</p>
                <p>We received a request to reset your password for your Linq Studio Google account. Please use the secure link below to set a new password:</p>
                <p><a href="https://linqstudio.app/reset-password">Reset Linq Studio Password</a></p>
                <p>If you did not request this, you can safely ignore this email.</p>
                <p>— <strong>LinqStudiosLLC Security</strong></p>
              </div>
            </body>
            </html>
        """.trimIndent()
        dispatchEmail(recipientEmail, subject, html)
    }

    private fun dispatchEmail(recipient: String, subject: String, bodyHtml: String) {
        val record = DispatchedEmail(
            recipient = recipient,
            subject = subject,
            bodyHtml = bodyHtml
        )
        synchronized(sentLog) {
            sentLog.add(record)
            if (sentLog.size > 100) sentLog.removeAt(0)
        }
        Log.i(TAG, "Dispatched email to $recipient: '$subject' from LinqStudiosLLC")
    }
}
