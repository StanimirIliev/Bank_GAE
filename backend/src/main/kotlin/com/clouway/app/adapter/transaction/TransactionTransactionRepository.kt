package com.clouway.app.adapter.transaction

import com.clouway.app.core.Transaction
import com.clouway.app.core.TransactionRepository
import com.google.appengine.api.datastore.DatastoreFailureException
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class TransactionTransactionRepository(
        private val origin: TransactionRepository,
        private val datastore: DatastoreService
) : TransactionRepository {
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