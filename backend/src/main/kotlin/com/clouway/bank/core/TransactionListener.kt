package com.clouway.bank.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface TransactionListener {
    fun onTransaction(userId: Long, accountId: Long, operation: Operation, amount: Float)
}