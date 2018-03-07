package com.clouway.emailing.adapter.http

import com.clouway.email.sender.adapter.sendgrid.Sendgrid
import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.TransactionEvent
import com.clouway.emailing.adapter.event.UserLoginEvent
import com.clouway.emailing.adapter.event.UserLogoutEvent
import com.clouway.emailing.adapter.event.UserRegistrationEvent
import com.clouway.emailing.adapter.handler.emailsend.TransactionHandler
import com.clouway.emailing.adapter.handler.emailsend.UserLoginHandler
import com.clouway.emailing.adapter.handler.emailsend.UserLogoutHandler
import com.clouway.emailing.adapter.handler.emailsend.UserRegistrationHandler
import com.clouway.eventbus.SimpleEventBus
import com.clouway.eventbus.core.EventBus
import com.clouway.eventdispatch.adapter.gcp.pubsub.PubsubDispatcher
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.gson.GsonBuilder
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PubsubReceiverServlet : HttpServlet() {

    data class Params(val message: PubsubMessage, val subscription: String)

    lateinit var sender: SimpleEmailSender
    lateinit var supportedEvents: Set<String>
    lateinit var eventBus: EventBus
    private val gson = GsonBuilder().create()

    override fun init() {
        val pubsubDispatcher = PubsubDispatcher("bank-e-corp", "user")
        pubsubDispatcher.createSubscription(
                "email-to-me",
                "https://email-to-me-dot-bank-e-corp.appspot.com/_ah/pubsub"
        )
        sender = Sendgrid()
        eventBus = SimpleEventBus()
        // defining supported events
        eventBus.registerHandler(UserRegistrationEvent::class.java, UserRegistrationHandler(sender))
        eventBus.registerHandler(UserLoginEvent::class.java, UserLoginHandler(sender))
        eventBus.registerHandler(UserLogoutEvent::class.java, UserLogoutHandler(sender))
        eventBus.registerHandler(TransactionEvent::class.java, TransactionHandler(sender))
        supportedEvents = eventBus.getSupportedEvents()
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val message = gson.fromJson(req.reader.readText(), Params::class.java).message
        if (supportedEvents.contains(message.attributes["event"])) {
            val event = message.attributes["event"]
            val data = message.attributes["data"]
            if (event == null || data == null) {
                resp.setStatus(500)
            } else {
                if (!eventBus.fireEvent(data, event)) {
                    resp.setStatus(200)
                }
                resp.setStatus(204)
            }
        }
        resp.setStatus(200)
    }
}