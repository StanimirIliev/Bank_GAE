package com.clouway.bank.adapter.gcp.transaction

import com.clouway.bank.core.Transaction
import com.clouway.bank.core.Transactions
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class SafeTransactionsProxy(
        private val origin: Transactions,
        private val datastore: DatastoreService
) : Transactions {
    override fun registerTransaction(transaction: Transaction): Boolean {
        val datastoreTransaction = datastore.currentTransaction
        val entity = Entity("Transactions")
        entity.setProperty("UserId", transaction.userId)
        entity.setProperty("AccountId", transaction.accountId)
        entity.setProperty("OnDate", transaction.onDate.toString())
        entity.setProperty("Operation", transaction.operation.toString())
        entity.setProperty("Amount", transaction.amount)
        try {
            val result = datastore.put(datastoreTransaction, entity) != null
            if (result) {
                datastoreTransaction.commit()
                return true
            }
            return false
        } catch (e: Exception) {
            datastoreTransaction.rollback()
            return false
        }

    }

    override fun getTransactions(userId: Long): List<Transaction> {
        return origin.getTransactions(userId)
    }
}