package com.clouway.emailing

import com.clouway.email.sender.adapter.sendgrid.Sendgrid
import com.clouway.emailing.adapter.event.UserRegistrationEvent
import com.clouway.emailing.adapter.handler.emailsend.UserRegistrationHandler
import com.clouway.emailing.adapter.http.PubsubReceiver
import com.clouway.eventbus.SimpleEventBus
import com.clouway.eventdispatch.adapter.gcp.pubsub.PubsubDispatcher
import spark.Spark
import spark.servlet.SparkApplication

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class Main : SparkApplication {
    override fun init() {
        // create pubsub subscriptions
        val pubsubDispatcher = PubsubDispatcher("bank-e-corp", "user")
        pubsubDispatcher.createSubscription(
                "email-sender",
                "https://email-sender-dot-bank-e-corp.appspot.com/_ah/pubsub"
        )
        // setup eventBus
        val sender = Sendgrid()
        val eventBus = SimpleEventBus()
        eventBus.registerHandler(UserRegistrationEvent::class.java, UserRegistrationHandler(sender))
        // setup spark
        Spark.post("/_ah/pubsub", PubsubReceiver(eventBus))
    }
}