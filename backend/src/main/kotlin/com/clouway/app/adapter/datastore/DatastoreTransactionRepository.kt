package com.clouway.app.adapter.datastore

import com.clouway.app.core.Operation
import com.clouway.app.core.Transaction
import com.clouway.app.core.TransactionRepository
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Query.FilterOperator
import com.google.appengine.api.datastore.Query.FilterPredicate
import java.time.LocalDateTime
import java.util.*

class DatastoreTransactionRepository(private val datastore: DatastoreService) : TransactionRepository {

    override fun registerTransaction(transaction: Transaction): Boolean {
        val entity = Entity("Transactions")
        entity.setProperty("UserId", transaction.userId)
        entity.setProperty("AccountId", transaction.accountId)
        entity.setProperty("OnDate", transaction.onDate.toString())
        entity.setProperty("Operation", transaction.operation.toString())
        entity.setProperty("Amount", transaction.amount)
        return datastore.put(entity) != null
    }

    override fun getTransactions(userId: Long): List<Transaction> {
        val transactionMapper =
                object : EntityMapper<Transaction> {
                    override fun fetch(entity: Entity): Transaction {
                        return Transaction(
                                entity.getProperty("UserId").toString().toLong(),
                                entity.getProperty("AccountId").toString().toLong(),
                                LocalDateTime.parse(entity.getProperty("OnDate").toString()),
                                Operation.valueOf(entity.getProperty("Operation").toString()),
                                entity.getProperty("Amount").toString().toFloat()
                        )
                    }
                }
        val filter = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val query = Query("Transactions")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults())
        val list = LinkedList<Transaction>()
        entityList.forEach {
            list.add(transactionMapper.fetch(it))
        }
        return list.sortedBy { it.onDate }
    }
}