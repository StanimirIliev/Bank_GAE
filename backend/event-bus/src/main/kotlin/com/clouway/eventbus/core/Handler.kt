package com.clouway.eventbus.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface Handler<T> {

    /**
     * Handle event
     * @param event the event which have to handle
     */
    @Throws(Exception::class)
    fun handle(event: T)
}