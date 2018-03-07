package com.clouway.eventdispatch.adapter.gcp.pubsub

import com.clouway.eventdispatch.core.Event
import com.clouway.eventdispatch.core.EventDispatcher
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.api.gax.rpc.ApiException
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.gson.GsonBuilder
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import org.apache.log4j.Logger

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PubsubDispatcher(val projectName: String, val topicName: String) : EventDispatcher {

    private val logger = Logger.getLogger("PubsubDispatcher")
    private val gson = GsonBuilder().create()
    private val topic = TopicName.of(projectName, topicName)

    init {
        createTopic(topic)
    }

    override fun publishEvent(event: Event) {
        val message = PubsubMessage
                .newBuilder()
                .putAllAttributes(
                        mapOf(
                                "event" to event::class.java.simpleName,
                                "data" to gson.toJson(event)
                        )
                )
                .build()
        lateinit var messageIdFuture: ApiFuture<String>
        var publisher: Publisher? = null
        try {
            publisher = Publisher.newBuilder(topic).build()
            messageIdFuture = publisher.publish(message)
            ApiFutures.addCallback(messageIdFuture, object : ApiFutureCallback<String> {
                override fun onSuccess(messageId: String?) {
                    logger.info("Published message with ID $messageId")
                }

                override fun onFailure(t: Throwable?) {
                    logger.warn("Unable to publish message", t)
                }
            })
        } catch (e: Exception) {
            logger.warn("Error occurred while publish message $message", e)
        } finally {
            if (publisher != null) {
                publisher.shutdown()
            }
        }
    }

    /**
     * Creates subscription for specific topic if there is no one created yet
     * @param subscriptionName name of the subscription
     * @param endpoint the endpoint on which to send events
     * @param topicName name of the topic - e.g. user-events. Default value for it is the value passed in the constructor
     * @param deadline the deadline (in seconds) to acknowledge the event before it is retried
     */
    fun createSubscription(subscriptionName: String, endpoint: String, topicName: String = this.topicName, deadline: Int = 10) {
        val topic = if(this.topicName == topicName) topic else TopicName.of(projectName, topicName)
        val subscription = SubscriptionName.of(projectName, subscriptionName)
        SubscriptionAdminClient.create().use {
            val pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint(endpoint)
                    .build()
            val ackDeadlineInSeconds = 10
            try {
                it.createSubscription(subscription, topic, pushConfig, ackDeadlineInSeconds)
                logger.info("Successfully created new subscription")
            } catch (e: Exception) {
                logger.warn("Probably this subscription exists already", e)
            }
        }
    }

    private fun createTopic(topicName: TopicName) {
        TopicAdminClient.create().use {
            try {
                it.createTopic(topicName)
                logger.info("New topic $topicName is created")
            } catch (e: ApiException) {
                if (e.statusCode.code.httpStatusCode == 409) {
                    logger.info("Topic $topicName already created")
                } else {
                    logger.warn("Error occurred while creating topic", e)
                }
            } catch (e: Exception) {
                logger.warn("Error occurred while creating topic", e)
            }
        }
    }
}