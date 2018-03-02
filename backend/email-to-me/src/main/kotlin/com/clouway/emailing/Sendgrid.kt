package com.clouway.emailing

import com.google.gson.GsonBuilder
import org.apache.commons.codec.binary.Base64
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

private const val SEND_ENDPOINT = "/v3/mail/send"

/**
 * Sendgrid is an API Wrapper of the SendGrid which uses HTTP for sending of email messages.
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
data class Sendgrid(private val targetHost: String, private val apiKey: String) {

    private var from: String? = null
    private var replyTo: String? = null
    private var subject: String? = null
    private val recipients = mutableSetOf<Email>()
    private var ccs: List<Email>? = null
    private var bccs: List<Email>? = null
    private val content = mutableListOf<Content>()
    private val attachments: MutableList<Attachments> = mutableListOf<Attachments>()


    /**
     *  Sets all email addresses which need to receive this message.
     *
     * @param emails recipient email addressses
     * @return the sendgrid object
     */
    fun addTo(vararg emails: String): Sendgrid {
        recipients.addAll(emails.map(::Email))
        return this
    }

    /**
     * Sets all emails which need to be added in the bcc section
     * of the message.
     *
     * @param emails bcc email addresses
     * @return the sendgrid object
     */
    fun bccTo(vararg emails: String): Sendgrid {
        bccs = emails.map(::Email)
        return this
    }

    /**
     * Sets all emails which need to be added in the cc section
     * of the message.
     *
     * @param emails bcc email addresses
     * @return the sendgrid object
     */
    fun ccTo(vararg emails: String): Sendgrid {
        ccs = emails.map(::Email)
        return this
    }

    /**
     * setFrom - Set the from email

     * @param email An email address
     * *
     * @return The SendGrid object.
     */
    fun setFrom(email: String): Sendgrid {
        this.from = email
        return this
    }

    /**
     * setReplyTo - set the reply-to address

     * @param email the email to reply to
     * *
     * @return the SendGrid object.
     */
    fun setReplyTo(email: String): Sendgrid {
        this.replyTo = email
        return this
    }

    /**
     * setSubject - Set the email subject

     * @param subject The email subject
     * *
     * @return The SendGrid object
     */
    fun setSubject(subject: String): Sendgrid {
        this.subject = subject

        return this
    }

    /**
     * setText - Set the plain text part of the email

     * @param text The plain text of the email
     * *
     * @return The SendGrid object.
     */
    fun setText(text: String): Sendgrid {
        content.add(Content("text/plain", text))
        return this
    }

    /**
     * setHTML - Set the HTML part of the email

     * @param html The HTML part of the email
     * *
     * @return The SendGrid object.
     */
    fun setHtml(html: String): Sendgrid {
        content.add(Content("text/html", html))
        return this
    }

    /**
     *  Sets all attachmentFiles that need to be send
     *
     * @param filename the name of the attachment file
     * @param content the content of the attachment file
     *
     * @return the sendgrid object
     */
    fun addAttachment(filename: String, content: ByteArray): Sendgrid {
        val encoded = String(Base64.encodeBase64(content))
        this.attachments.add(Attachments(encoded, filename))
        return this
    }

    /**
     * Send sends an email to the target email addresses.
     *
     * @throws IOException in case of IO failures
     */
    @Throws(IOException::class)
    fun send() {
        val gson = GsonBuilder().create()

        val personalizations = listOf(Personalization(recipients, ccs, bccs))

        var message = SGMailV3(personalizations, Email(from), subject, content, null)

        if (attachments.isNotEmpty()) {
            message = SGMailV3(personalizations, Email(from), subject, content, attachments)
        }

        val connection = URL(targetHost + SEND_ENDPOINT).openConnection() as HttpURLConnection
        try {
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.addRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.addRequestProperty("Authorization", "Bearer " + apiKey)

            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            gson.toJson(message, writer)
            println(gson.toJson(message))
            writer.flush()

            // Get the response
            val stream = connection.inputStream
            stream.readBytes()

            stream.close()
            writer.close()

            if (connection.responseCode != HttpURLConnection.HTTP_ACCEPTED) {
                throw IOException("could not send email")
            }

        } catch (e: MalformedURLException) {
            throw IOException("Malformed URL Exception", e)
        } catch (e: IOException) {
            throw IOException("IO Exception", e)
        } finally {
            // It's not sure that GAE will close the connection, but give it a try
            connection.disconnect()
        }
    }
}

private data class SGMailV3(val personalizations: List<Personalization>, val from: Email, val subject: String?, val content: List<Content>, val attachments: List<Attachments>?) {
}

private data class Personalization(val to: Set<Email>, val cc: List<Email>?, val bcc: List<Email>?)

private data class Email(val email: String?)

private data class Content(val type: String, val value: String)

private data class Attachments(val content: String, val filename: String)

data class SendGridHost(val value: String)