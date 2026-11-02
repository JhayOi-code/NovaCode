package com.example.auth

import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

data class SmtpDeliveryResult(
    val success: Boolean,
    val messageId: String,
    val response: String,
    val provider: String,
    val isTestFallback: Boolean = false,
    val errorMessage: String? = null
)

object SmtpEmailSender {

    private const val TAG = "LinqSmtpSender"

    // Configuration keys (read from System environment or build config)
    var smtpHost: String = System.getenv("SMTP_HOST") ?: "smtp.gmail.com"
    var smtpPort: Int = (System.getenv("SMTP_PORT") ?: "465").toIntOrNull() ?: 465
    var smtpUser: String = (System.getenv("SMTP_USER") ?: "").trim()
    var smtpPass: String = (System.getenv("SMTP_PASS") ?: "").trim().replace(" ", "")
    var mailFromName: String = System.getenv("MAIL_FROM_NAME") ?: "LinqStudiosLLC"
    var mailFromAddress: String = System.getenv("MAIL_FROM_ADDRESS") ?: smtpUser

    fun isConfigured(): Boolean {
        return smtpUser.isNotBlank() && smtpPass.isNotBlank() && !smtpUser.contains("your_")
    }

    /**
     * Sends an email via SMTP.
     */
    suspend fun sendEmail(
        recipientEmail: String,
        subject: String,
        htmlBody: String,
        plainBody: String
    ): SmtpDeliveryResult = withContext(Dispatchers.IO) {
        val cleanRecipient = recipientEmail.trim().lowercase()

        if (isConfigured()) {
            try {
                return@withContext sendOverSmtp(
                    recipient = cleanRecipient,
                    subject = subject,
                    htmlBody = htmlBody,
                    plainBody = plainBody
                )
            } catch (e: Exception) {
                Log.e(TAG, "Primary SMTP send failed: ${e.message}", e)
                if (smtpPort == 465) {
                    try {
                        Log.i(TAG, "Attempting fallback SMTP send on port 587...")
                        return@withContext sendOverSmtpPort587(
                            recipient = cleanRecipient,
                            subject = subject,
                            htmlBody = htmlBody,
                            plainBody = plainBody
                        )
                    } catch (e2: Exception) {
                        Log.e(TAG, "Fallback SMTP port 587 failed: ${e2.message}", e2)
                    }
                }
                return@withContext SmtpDeliveryResult(
                    success = false,
                    messageId = "",
                    response = "",
                    provider = "smtp.gmail.com",
                    errorMessage = e.message ?: "Failed to send email via SMTP"
                )
            }
        }

        return@withContext SmtpDeliveryResult(
            success = false,
            messageId = "",
            response = "NO_CREDENTIALS",
            provider = "smtp.gmail.com",
            errorMessage = "SMTP credentials (SMTP_USER / SMTP_PASS) not configured."
        )
    }

    /**
     * Owner-only diagnostics: verifies SMTP server handshake, authentication, and dispatch acceptance.
     */
    suspend fun testConnectionAndSendDiagnostics(ownerEmail: String): SmtpDeliveryResult = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        if (!isConfigured()) {
            return@withContext SmtpDeliveryResult(
                success = false,
                messageId = "",
                response = "NOT_CONFIGURED",
                provider = smtpHost,
                errorMessage = "SMTP credentials (SMTP_USER/SMTP_PASS) not configured. Set your Google App Password in .env / system environment."
            )
        }

        try {
            val testId = SecureRandom().nextInt(100000).toString()
            val subject = "[Email Diagnostics] Linq Studio Delivery Check #$testId"
            val html = """
                <div style="font-family:sans-serif;padding:20px;background:#0F172A;color:#FFF;border-radius:10px;">
                    <h2>Linq Studio Email Diagnostics</h2>
                    <p>SMTP handshake and authentication succeeded.</p>
                    <p>Test ID: $testId</p>
                </div>
            """.trimIndent()
            val plain = "Linq Studio Email Diagnostics: SMTP handshake and authentication succeeded. Test ID: $testId"

            val res = sendEmail(
                recipientEmail = ownerEmail,
                subject = subject,
                htmlBody = html,
                plainBody = plain
            )
            val latency = System.currentTimeMillis() - startMs
            if (res.success) {
                res.copy(response = "ACCEPTED (${latency}ms, ${res.messageId})")
            } else {
                res
            }
        } catch (e: Exception) {
            SmtpDeliveryResult(
                success = false,
                messageId = "",
                response = "ERROR",
                provider = smtpHost,
                errorMessage = e.message ?: "Connection failed"
            )
        }
    }

    suspend fun runDiagnostics(ownerEmail: String): SmtpDeliveryResult = withContext(Dispatchers.IO) {
        return@withContext testConnectionAndSendDiagnostics(ownerEmail)
    }

    private fun sendOverSmtp(
        recipient: String,
        subject: String,
        htmlBody: String,
        plainBody: String
    ): SmtpDeliveryResult {
        val socket = SSLSocketFactory.getDefault().createSocket(smtpHost, smtpPort) as SSLSocket
        socket.soTimeout = 15000

        socket.use { ssl ->
            val reader = BufferedReader(InputStreamReader(ssl.inputStream, Charsets.UTF_8))
            val writer = BufferedWriter(OutputStreamWriter(ssl.outputStream, Charsets.UTF_8))

            fun readResponse(): String {
                val sb = StringBuilder()
                while (true) {
                    val line = reader.readLine() ?: break
                    sb.append(line).append("\n")
                    if (line.length >= 4 && line[3] == ' ') break
                }
                return sb.toString().trim()
            }

            fun sendCommand(cmd: String): String {
                writer.write("$cmd\r\n")
                writer.flush()
                return readResponse()
            }

            val greeting = readResponse()
            if (!greeting.startsWith("220")) throw IllegalStateException("Unexpected SMTP greeting: $greeting")

            val ehlo = sendCommand("EHLO linqstudio.com")
            if (!ehlo.startsWith("250")) throw IllegalStateException("EHLO rejected: $ehlo")

            val authStart = sendCommand("AUTH LOGIN")
            if (!authStart.startsWith("334")) throw IllegalStateException("AUTH LOGIN rejected: $authStart")

            val userB64 = Base64.encodeToString(smtpUser.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val userResp = sendCommand(userB64)
            if (!userResp.startsWith("334")) throw IllegalStateException("Username rejected: $userResp")

            val passB64 = Base64.encodeToString(smtpPass.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val passResp = sendCommand(passB64)
            if (!passResp.startsWith("235")) {
                throw IllegalStateException("Authentication failed: $passResp")
            }

            val fromAddr = if (mailFromAddress.isNotBlank()) mailFromAddress else smtpUser
            val mailFromResp = sendCommand("MAIL FROM:<$fromAddr>")
            if (!mailFromResp.startsWith("250")) throw IllegalStateException("MAIL FROM rejected: $mailFromResp")

            val rcptResp = sendCommand("RCPT TO:<$recipient>")
            if (!rcptResp.startsWith("250")) throw IllegalStateException("RCPT TO rejected: $rcptResp")

            val dataStart = sendCommand("DATA")
            if (!dataStart.startsWith("354")) throw IllegalStateException("DATA rejected: $dataStart")

            val msgId = "<${System.currentTimeMillis()}.${SecureRandom().nextInt(1000000)}@linqstudios.com>"
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val dateStr = sdf.format(Date())
            val boundary = "==_Boundary_${System.currentTimeMillis()}_=="

            val rawMessage = buildString {
                append("From: \"$mailFromName\" <$fromAddr>\r\n")
                append("To: <$recipient>\r\n")
                append("Subject: $subject\r\n")
                append("Date: $dateStr\r\n")
                append("Message-ID: $msgId\r\n")
                append("MIME-Version: 1.0\r\n")
                append("Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n\r\n")
                append("--$boundary\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n")
                append(plainBody)
                append("\r\n\r\n--$boundary\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n")
                append(htmlBody)
                append("\r\n\r\n--$boundary--\r\n.\r\n")
            }

            writer.write(rawMessage)
            writer.flush()

            val endResp = readResponse()
            if (!endResp.startsWith("250")) throw IllegalStateException("Message rejected by server: $endResp")

            try { sendCommand("QUIT") } catch (_: Exception) {}

            return SmtpDeliveryResult(
                success = true,
                messageId = msgId,
                response = endResp,
                provider = "$smtpHost:$smtpPort"
            )
        }
    }

    private fun sendOverSmtpPort587(
        recipient: String,
        subject: String,
        htmlBody: String,
        plainBody: String
    ): SmtpDeliveryResult {
        val plainSocket = Socket(smtpHost, 587)
        plainSocket.soTimeout = 15000

        var reader = BufferedReader(InputStreamReader(plainSocket.inputStream, Charsets.UTF_8))
        var writer = BufferedWriter(OutputStreamWriter(plainSocket.outputStream, Charsets.UTF_8))

        fun readResponse(): String {
            val sb = StringBuilder()
            while (true) {
                val line = reader.readLine() ?: break
                sb.append(line).append("\n")
                if (line.length >= 4 && line[3] == ' ') break
            }
            return sb.toString().trim()
        }

        fun sendCommand(cmd: String): String {
            writer.write("$cmd\r\n")
            writer.flush()
            return readResponse()
        }

        val greeting = readResponse()
        if (!greeting.startsWith("220")) throw IllegalStateException("Unexpected greeting on 587: $greeting")

        sendCommand("EHLO linqstudio.com")
        val starttls = sendCommand("STARTTLS")
        if (!starttls.startsWith("220")) throw IllegalStateException("STARTTLS rejected: $starttls")

        val ssl = (SSLSocketFactory.getDefault() as SSLSocketFactory).createSocket(
            plainSocket,
            smtpHost,
            587,
            true
        ) as SSLSocket
        ssl.startHandshake()

        reader = BufferedReader(InputStreamReader(ssl.inputStream, Charsets.UTF_8))
        writer = BufferedWriter(OutputStreamWriter(ssl.outputStream, Charsets.UTF_8))

        sendCommand("EHLO linqstudio.com")
        sendCommand("AUTH LOGIN")
        val userB64 = Base64.encodeToString(smtpUser.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        sendCommand(userB64)
        val passB64 = Base64.encodeToString(smtpPass.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val authResult = sendCommand(passB64)
        if (!authResult.startsWith("235")) throw IllegalStateException("Auth failed on 587: $authResult")

        val fromAddr = if (mailFromAddress.isNotBlank()) mailFromAddress else smtpUser
        sendCommand("MAIL FROM:<$fromAddr>")
        sendCommand("RCPT TO:<$recipient>")
        sendCommand("DATA")

        val msgId = "<${System.currentTimeMillis()}.${SecureRandom().nextInt(1000000)}@linqstudios.com>"
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dateStr = sdf.format(Date())
        val boundary = "==_Boundary_${System.currentTimeMillis()}_=="

        val rawMessage = buildString {
            append("From: \"$mailFromName\" <$fromAddr>\r\n")
            append("To: <$recipient>\r\n")
            append("Subject: $subject\r\n")
            append("Date: $dateStr\r\n")
            append("Message-ID: $msgId\r\n")
            append("MIME-Version: 1.0\r\n")
            append("Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n\r\n")
            append("--$boundary\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n")
            append(plainBody)
            append("\r\n\r\n--$boundary\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n")
            append(htmlBody)
            append("\r\n\r\n--$boundary--\r\n.\r\n")
        }

        writer.write(rawMessage)
        writer.flush()
        val endResp = readResponse()
        try { sendCommand("QUIT") } catch (_: Exception) {}
        ssl.close()

        return SmtpDeliveryResult(
            success = true,
            messageId = msgId,
            response = endResp,
            provider = "$smtpHost:587 STARTTLS"
        )
    }
}
