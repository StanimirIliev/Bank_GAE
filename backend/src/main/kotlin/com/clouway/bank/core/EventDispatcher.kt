package com.clouway.bank.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface EventDispatcher {
    fun publishEvent(event: Event)
}