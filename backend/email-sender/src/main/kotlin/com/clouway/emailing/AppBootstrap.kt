package com.clouway.emailing

import com.clouway.email.sender.adapter.sendgrid.Sendgrid
import com.clouway.emailing.adapter.event.UserRegistrationEvent
import com.clouway.emailing.adapter.event.bus.SimpleEventBus
import com.clouway.emailing.adapter.handler.emailsend.UserRegistrationHandler
import com.clouway.emailing.adapter.http.EmailSenderRoute
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import org.apache.log4j.Logger
import spark.Spark

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class AppBootstrap {
    fun start() {
        val logger = Logger.getLogger("AppBootstrap")
        // create pubsub subscriptions
        val topic = TopicName.of("bank-e-corp", "user")
        val subscriptionName = SubscriptionName.of("bank-e-corp", "email-sender")
        SubscriptionAdminClient.create().use {
            val pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint("https://email-sender-dot-bank-e-corp.appspot.com/")
                    .build()
            val ackDeadlineInSeconds = 10
            try {
                it.createSubscription(subscriptionName, topic, pushConfig, ackDeadlineInSeconds)
            } catch (e: Exception) {
                logger.warn("Probably this subscription exists already", e)
            }
        }
        // setup eventBus
        val sender = Sendgrid()
        val eventBus = SimpleEventBus()
        eventBus.registerHandler(UserRegistrationEvent::class.java, UserRegistrationHandler(sender))
        // setup spark
        Spark.post("/", EmailSenderRoute(eventBus))
    }
}