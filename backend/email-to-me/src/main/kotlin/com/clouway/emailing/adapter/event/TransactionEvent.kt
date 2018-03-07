package com.clouway.emailing.adapter.event

import com.clouway.emailing.core.Operation
import com.clouway.eventbus.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class TransactionEvent(val userId: Long, val accountId: Long, val operation: Operation, val amount: Float) : Event