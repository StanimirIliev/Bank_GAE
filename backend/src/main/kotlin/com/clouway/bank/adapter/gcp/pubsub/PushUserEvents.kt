package com.clouway.bank.adapter.gcp.pubsub

import com.clouway.bank.core.UserEventHandler
import com.google.api.core.ApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import org.apache.log4j.Logger


/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PushUserEvents(private val logger: Logger) : UserEventHandler {
    override fun onRegister(email: String, username: String) {
        val topicName = TopicName.of("bank-e-corp", "emailing")
        TopicAdminClient.create().use {
            try {
                it.createTopic(topicName)
            } catch (e: Exception) {
                logger.warn("Exception handled from creating topic", e)
                // This topic exists already
            }
        }
        val subscriptionName = SubscriptionName.of("bank-e-corp", "email-sender")
        SubscriptionAdminClient.create().use {
            val pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint("https://bank-e-corp.appspot.com/emailSender")
                    .build()
            val ackDeadlineInSeconds = 10
            try {
                it.createSubscription(subscriptionName, topicName, pushConfig, ackDeadlineInSeconds)
            } catch (e: Exception) {
                logger.warn("Exception handled from creating subscription", e)
                // This subscriber exists already
            }
        }

        lateinit var messageIdFuture: ApiFuture<String>
        var publisher: Publisher? = null
        try {
            publisher = Publisher.newBuilder(topicName).build()
            val pubsubMessage = PubsubMessage
                    .newBuilder()
                    .putAttributes("email", email)
                    .putAttributes("username", username)
                    .build()
            messageIdFuture = publisher.publish(pubsubMessage)
        } finally {
            val messageId = messageIdFuture.get()
            logger.info("Published with message ID: $messageId")
            if (publisher != null) {
                publisher.shutdown()
            }
        }
    }

    override fun onLogin(username: String) {}

    override fun onLogout(username: String) {}
}