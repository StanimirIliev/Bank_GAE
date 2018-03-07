package com.clouway.eventbus

import com.clouway.eventbus.core.Event
import com.clouway.eventbus.core.EventBus
import com.clouway.eventbus.core.Handler
import com.google.gson.GsonBuilder

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class SimpleEventBus : EventBus {
    private val gson = GsonBuilder().create()

    private val handlers = mutableMapOf<String, Handler<Any>>()
    private val classes = mutableMapOf<String, Class<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerHandler(event: Class<T>, handler: Handler<*>) {
        handlers[event.simpleName] = handler as Handler<Any>
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