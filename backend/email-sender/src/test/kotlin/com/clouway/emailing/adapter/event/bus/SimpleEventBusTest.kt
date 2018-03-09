package com.clouway.emailing.adapter.event.bus

import com.clouway.emailing.core.Event
import com.clouway.emailing.core.Handler
import com.google.gson.GsonBuilder
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class SimpleEventBusTest {
    data class Event1(val id: Long) : Event {
        override fun getAttributes(): Map<String, String> {
            return mapOf("id" to id.toString())
        }
    }

    @Rule
    @JvmField
    val context = JUnitRuleMockery()

    val eventBus = SimpleEventBus()
    val simpleEvent = Event1(21L)
    val handler = context.mock(Handler::class.java, "Handler1")
    val gson = GsonBuilder().create()


    @Test
    fun getSupportedEvents() {
        eventBus.registerHandler(simpleEvent::class.java, handler)
        assertThat(eventBus.getSupportedEvents(),
                `is`(equalTo(setOf(simpleEvent::class.java.simpleName))))
    }

    @Test
    fun fireEventsThatHasRegisteredHandlers() {
        eventBus.registerHandler(simpleEvent::class.java, handler)
        context.checking(object : Expectations() {
            init {
                oneOf(handler).handle(simpleEvent)
            }
        })
        assertThat(eventBus.fireEvent(gson.toJson(simpleEvent), simpleEvent::class.java.simpleName),
                `is`(equalTo(true)))
    }

    @Test
    fun tryToFireEventWithoutRegisteringHandlerForIt() {
        assertThat(eventBus.fireEvent(gson.toJson(simpleEvent), simpleEvent::class.java.simpleName),
                `is`(equalTo(false)))
    }
}