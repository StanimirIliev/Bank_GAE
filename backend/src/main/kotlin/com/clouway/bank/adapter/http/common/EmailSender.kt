package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.EmailSender
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.io.IOException

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSender(private val sender: EmailSender, private val logger: Logger) : Route {

    data class Params(val email: String, val username: String)

    override fun handle(req: Request, resp: Response): Any {
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0], values[1])
        try {
            sender
                    .setFrom("e.corp@bank.com")
                    .addTo(params.email)
                    .setSubject("Registration in bank of E corp")
                    .setText("Hello ${params.username}. Welcome to bank of E corp. Thank you for choosing us.")
                    .send()
            logger.info("Successfully sent email to ${params.email} with username ${params.username}")
        } catch (e: IOException) {
            logger.error("Unable to send email to ${params.email} with username ${params.username}")
        }
        return resp.status(200)
    }
}