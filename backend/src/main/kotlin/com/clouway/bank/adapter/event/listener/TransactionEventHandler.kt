package com.clouway.bank.adapter.event.listener

import com.clouway.bank.adapter.event.TransactionEvent
import com.clouway.bank.core.Operation
import com.clouway.bank.core.TransactionListener
import com.clouway.eventdispatch.core.EventDispatcher

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class TransactionEventHandler(private val eventDispatcher: EventDispatcher): TransactionListener {
    override fun onTransaction(userId: Long, accountId: Long, operation: Operation, amount: Float) {
        eventDispatcher.publishEvent(TransactionEvent(userId, accountId, operation, amount))
    }
}