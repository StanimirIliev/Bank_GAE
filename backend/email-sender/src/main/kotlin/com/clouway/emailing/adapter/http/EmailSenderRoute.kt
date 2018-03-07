package com.clouway.emailing.adapter.http

import com.clouway.emailing.core.EventBus
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.gson.GsonBuilder
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSenderRoute(private val eventBus: EventBus) : Route {

    data class Params(val message: PubsubMessage, val subscription: String)

    private val logger = Logger.getLogger("EmailSenderRoute")
    private val gson = GsonBuilder().create()
    private val supportedEvents = eventBus.getSupportedEvents()

    override fun handle(req: Request, resp: Response): Any {
        val message = gson.fromJson(req.body(), Params::class.java).message
        if (supportedEvents.contains(message.attributes["event"])) {
            try {
                if (!eventBus.fireEvent(message.attributes["data"]!!, message.attributes["event"]!!)) {
                    return resp.status(200)
                }
                return resp.status(204)
            } catch (e: Exception) {
                logger.warn("Error occurred while handling event", e)
                return resp.status(500)
            }
        }
        return resp.status(200)
    }
}