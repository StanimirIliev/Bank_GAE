package com.clouway.app.adapter.http.post

import com.clouway.app.core.EmailSender
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.pubsub.model.PubsubMessage
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.io.IOException

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSenderRoute(private val sender: EmailSender, private val logger: Logger) : Route {

    override fun handle(req: Request, resp: Response): Any {
        val parser = JacksonFactory.getDefaultInstance()
                .createJsonParser(req.body())
        parser.skipToKey("message")
        val message = parser.parseAndClose(PubsubMessage::class.java)
        val email = message.attributes["email"].toString()
        val username = message.attributes["username"].toString()
        try {
            sender
                    .setFrom("e.corp@bank.com")
                    .addTo(email)
                    .setSubject("Registration in bank of E corp")
                    .setText("Hello $username. Welcome to bank of E corp. Thank you for choosing us.")
                    .send()
            logger.info("Successfully sent email to $email with username $username")
            return resp.status(200)
        } catch (e: IOException) {
            logger.error("Unable to send email to $email with username $username")
            return resp.status(500)
        }
    }

}