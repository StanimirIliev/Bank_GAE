package com.clouway.email.sender.adapter.sendgrid

import com.clouway.email.sender.core.SimpleEmailSender
import com.sendgrid.*
import java.io.IOException
import java.nio.charset.Charset


/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class Sendgrid : SimpleEmailSender {
    private lateinit var from: Email
    private lateinit var to: Email
    private lateinit var content: Content
    private var subject: String? = null
    private val apiKey = Sendgrid::class.java.getResourceAsStream("sendgrid.env")
            .reader(Charset.defaultCharset())
            .readText()

    override fun setTo(email: String): SimpleEmailSender {
        to = Email(email)
        return this
    }

    override fun setFrom(email: String): SimpleEmailSender {
        from = Email(email)
        return this
    }

    override fun setSubject(subject: String): SimpleEmailSender {
        this.subject = subject
        return this
    }

    override fun setContent(content: String, type: String?): SimpleEmailSender {
        this.content = Content(type ?: "text/plain", content)
        return this
    }

    override fun send() {
        val mail = Mail(from, subject, to, content)

        val sg = SendGrid(apiKey)
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sg.api(request)
            println(response.statusCode)
            println(response.body)
            println(response.headers)
        } catch (ex: IOException) {
            println("Unable to send email")
            throw ex
        }
    }
}