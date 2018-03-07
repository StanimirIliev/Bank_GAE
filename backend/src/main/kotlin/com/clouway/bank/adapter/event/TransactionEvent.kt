package com.clouway.bank.adapter.event

import com.clouway.bank.core.Operation
import com.clouway.eventdispatch.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class TransactionEvent(val userId: Long, val accountId: Long, val operation: Operation, val amount: Float): Event