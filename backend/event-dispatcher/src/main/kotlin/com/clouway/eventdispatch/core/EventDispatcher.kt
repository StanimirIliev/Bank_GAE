package com.clouway.eventdispatch.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface EventDispatcher {
    /**
     * Dispatch event
     * @param event the event which will be dispatched
     */
    fun publishEvent(event: Event)
}