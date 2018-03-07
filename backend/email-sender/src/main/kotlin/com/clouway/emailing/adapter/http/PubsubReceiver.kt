package com.clouway.emailing.adapter.http

import com.clouway.eventbus.core.EventBus
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.gson.GsonBuilder
import spark.Request
import spark.Response
import spark.Route

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PubsubReceiver(private val eventBus: EventBus) : Route {

    data class Params(val message: PubsubMessage, val subscription: String)

    private val gson = GsonBuilder().create()
    private val supportedEvents = eventBus.getSupportedEvents()

    override fun handle(req: Request, resp: Response): Any {
        val message = gson.fromJson(req.body(), Params::class.java).message
        if (supportedEvents.contains(message.attributes["event"])) {
            val event = message.attributes["event"] ?: return resp.status(500)
            val data = message.attributes["data"] ?: return resp.status(500)
            if (!eventBus.fireEvent(data, event)) {
                return resp.status(200)
            }
            return resp.status(204)
        }
        return resp.status(200)
    }
}