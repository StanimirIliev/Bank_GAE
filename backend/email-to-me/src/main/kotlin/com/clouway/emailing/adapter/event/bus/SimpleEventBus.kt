package com.clouway.emailing.adapter.event.bus

import com.clouway.emailing.core.Event
import com.clouway.emailing.core.EventBus
import com.clouway.emailing.core.Handler
import com.google.gson.GsonBuilder

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class SimpleEventBus : EventBus {
    private val gson = GsonBuilder().create()

    private val handlers = mutableMapOf<String, Handler>()
    private val classes = mutableMapOf<String, Class<*>>()

    override fun <T> registerHandler(event: Class<T>, handler: Handler) {
        handlers[event.simpleName] = handler
        classes[event.simpleName] = event
    }

    override fun fireEvent(rawEvent: String, simpleClassName: String): Boolean {
        if(classes[simpleClassName] == null || handlers[simpleClassName] == null) {
            return false
        }
        val event = gson.fromJson(rawEvent, classes[simpleClassName]) as Event
        handlers[simpleClassName]!!.handle(event)
        return true
    }

    override fun getSupportedEvents(): Set<String> {
        return handlers.keys
    }
}