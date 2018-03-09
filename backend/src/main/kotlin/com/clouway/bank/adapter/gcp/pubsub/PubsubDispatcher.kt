package com.clouway.bank.adapter.gcp.pubsub

import com.clouway.bank.core.Event
import com.clouway.bank.core.EventDispatcher
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.api.gax.rpc.ApiException
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.gson.GsonBuilder
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import org.apache.log4j.Logger


/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class PubsubDispatcher(private val topicName: TopicName): EventDispatcher {

    private val logger = Logger.getLogger("PubsubDispatcher")
    private val gson = GsonBuilder().create()

    init {
        createTopic(topicName)
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
            publisher = Publisher.newBuilder(topicName).build()
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