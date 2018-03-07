package com.clouway.emailing.adapter.http

import com.clouway.email.sender.adapter.sendgrid.Sendgrid
import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.UserLoginEvent
import com.clouway.emailing.adapter.event.UserLogoutEvent
import com.clouway.emailing.adapter.event.UserRegistrationEvent
import com.clouway.emailing.adapter.event.bus.SimpleEventBus
import com.clouway.emailing.adapter.handler.emailsend.UserLoginHandler
import com.clouway.emailing.adapter.handler.emailsend.UserLogoutHandler
import com.clouway.emailing.adapter.handler.emailsend.UserRegistrationHandler
import com.clouway.emailing.core.EventBus
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import org.apache.log4j.Logger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class EmailSenderServlet : HttpServlet() {

    data class Params(val message: PubsubMessage, val subscription: String)

    lateinit var sender: SimpleEmailSender
    lateinit var supportedEvents: Set<String>
    lateinit var eventBus: EventBus
    private val logger = Logger.getLogger("EmailSenderServlet")
    private val gson = GsonBuilder().create()

    override fun init() {
        logger.info("Initialization")
        val topic = TopicName.of("bank-e-corp", "user")
        val subscriptionName = SubscriptionName.of("bank-e-corp", "email-to-me")
        SubscriptionAdminClient.create().use {
            val pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint("https://email-to-me-dot-bank-e-corp.appspot.com/")
                    .build()
            val ackDeadlineInSeconds = 10
            try {
                it.createSubscription(subscriptionName, topic, pushConfig, ackDeadlineInSeconds)
            } catch (e: Exception) {
                logger.warn("Probably this subscription exists already", e)
            }
        }

        sender = Sendgrid()
        eventBus = SimpleEventBus()
        // defining supported events
        eventBus.registerHandler(UserRegistrationEvent::class.java, UserRegistrationHandler(sender))
        eventBus.registerHandler(UserLoginEvent::class.java, UserLoginHandler(sender))
        eventBus.registerHandler(UserLogoutEvent::class.java, UserLogoutHandler(sender))
        supportedEvents = eventBus.getSupportedEvents()
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val message = gson.fromJson(req.reader.readText(), Params::class.java).message
        if (supportedEvents.contains(message.attributes["event"])) {
            try {
                if (!eventBus.fireEvent(message.attributes["data"]!!, message.attributes["event"]!!)) {
                    resp.setStatus(200)
                }
                resp.setStatus(204)
            } catch (e: Exception) {
                logger.warn("Error occurred while handling event", e)
                resp.setStatus(500)
            }
        }
        resp.setStatus(200)
    }
}