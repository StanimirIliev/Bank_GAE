package com.clouway.emailing.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

interface Handler {

    /**
     * Handle event
     * @param event the event which have to handle
     */
    @Throws(Exception::class)
    fun handle(event: Event)
}