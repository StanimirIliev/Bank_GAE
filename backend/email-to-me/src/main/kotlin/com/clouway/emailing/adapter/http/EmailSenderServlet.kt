package com.clouway.emailing.adapter.http

import com.clouway.emailing.Sendgrid
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.gson.Gson
import org.apache.log4j.Logger
import java.io.IOException
import java.nio.charset.Charset
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSenderServlet : HttpServlet() {

    data class Params(val message: PubsubMessage, val subscription: String)

    private val sendgridApiKey = EmailSenderServlet::class.java.getResourceAsStream("sendgrid.env")
            .reader(Charset.defaultCharset())
            .readText()
    private val logger = Logger.getLogger("EmailSenderServlet")
    private val sender = Sendgrid("https://api.sendgrid.com", sendgridApiKey)

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val message = Gson().fromJson(req.reader.readText(), Params::class.java).message
        val username = message.attributes["username"].toString()
        try {
            sender
                    .setFrom("e.corp@bank.com")
                    .addTo("stanimir.iliev.21@abv.bg")
                    .setSubject("Someone just registered in your app")
                    .setText("$username is registered in your app")
                    .send()
            logger.info("Successfully sent email to me about registration of $username")
            resp.setStatus(204)
        } catch (e: IOException) {
            logger.error("Unable to send email to me about registration of $username")
            resp.setStatus(500)
        }
    }
}