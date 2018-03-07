package com.clouway.emailing.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface EventBus {

    /**
     * Register event and its handler
     * @param event the class of the event
     * @param handler implementation of Handler interface which will handle this event
     */
    fun <T>registerHandler(event: Class<T>, handler: Handler)

    /**
     * Executes event via its predefined handler
     * @param rawEvent the json representation of the event which will be executed
     * @param simpleClassName simpleClassName of the event
     * @return true if the class has registered handler, false if it has not
     */
    fun fireEvent(rawEvent: String, simpleClassName: String): Boolean

    /**
     * Gets all supported events
     * @return set with simpleName of the supported events classes
     */
    fun getSupportedEvents(): Set<String>
}