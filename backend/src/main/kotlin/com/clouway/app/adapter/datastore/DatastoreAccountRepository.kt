package com.clouway.app.adapter.datastore

import com.clouway.app.core.*
import com.clouway.app.core.Currency
import com.clouway.app.core.ErrorType.INCORRECT_ID
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Query.*
import java.time.LocalDateTime
import java.util.*

class DatastoreAccountRepository(
        private val datastore: DatastoreService,
        private val transactionRepository: TransactionRepository
) : AccountRepository {

    private val fetchOptions = FetchOptions.Builder.withDefaults()

    private val accountMapper = object : EntityMapper<Account> {
        override fun fetch(entity: Entity): Account {
            return Account(
                    entity.getProperty("Title").toString(),
                    entity.getProperty("UserId").toString().toLong(),
                    Currency.valueOf(entity.getProperty("Currency").toString()),
                    entity.getProperty("Balance").toString().toFloat(),
                    entity.key.id
            )
        }
    }

    override fun registerAccount(account: Account): Long {
        val entity = Entity("Accounts")
        entity.setProperty("Title", account.title)
        entity.setProperty("UserId", account.userId)
        entity.setProperty("Currency", account.currency.toString())
        entity.setProperty("Balance", account.balance)
        entity.setProperty("DeletedOn", null)
        return datastore.put(entity)?.id ?: -1L
    }

    override fun updateBalance(accountId: Long, userId: Long, amount: Float): OperationResponse {
        val balance = getBalance(accountId) ?: return OperationResponse(false, INCORRECT_ID)
        val entitiesList = datastore.prepare(Query("Accounts")).asList(fetchOptions)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        val entity = Entity("Accounts", accountId)
        entity.setPropertiesFrom(oldEntity)
        entity.setProperty("Balance", amount + balance)
        datastore.put(entity)
        transactionRepository.registerTransaction(Transaction(userId, accountId, LocalDateTime.now(),
                if (amount < 0) Operation.WITHDRAW else Operation.DEPOSIT, amount))
        return OperationResponse(true, null)
    }

    override fun getActiveAccounts(userId: Long): List<Account> {
        val filter1 = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val filter2 = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val compositeFilter = Query.CompositeFilter(CompositeFilterOperator.AND, listOf(filter1, filter2))
        val query = Query("Accounts")
        query.filter = compositeFilter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        val list = LinkedList<Account>()
        entityList.forEach {
            list.add(accountMapper.fetch(it))
        }
        return list
    }

    override fun getAllAccounts(userId: Long): List<Account> {
        val filter = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val query = Query("Accounts")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        val list = LinkedList<Account>()
        entityList.forEach {
            list.add(accountMapper.fetch(it))
        }
        return list
    }

    override fun removeAccount(accountId: Long, userId: Long): OperationResponse {
        val entitiesList = datastore.prepare(Query("Accounts")).asList(fetchOptions)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        val entity = Entity("Accounts", accountId)
        entity.setPropertiesFrom(oldEntity)
        entity.setProperty("DeletedOn", LocalDateTime.now().toString())
        datastore.put(entity)
        return OperationResponse(true, null)
    }

    override fun getUserAccount(userId: Long, accountId: Long): Account? {
        val filter1 = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val filter2 = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val compositeFilter = CompositeFilter(CompositeFilterOperator.AND, listOf(filter1, filter2))
        val query = Query("Accounts")
        query.filter = compositeFilter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        val userAccountsList = LinkedList<Account>()
        entityList.forEach {
            userAccountsList.add(accountMapper.fetch(it))
        }
        return userAccountsList.find { it.id == accountId }
    }

    private fun getBalance(accountId: Long): Float? {
        val filter = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val query = Query("Accounts")
        query.filter = filter
        val entityList = datastore.prepare(query).asList(fetchOptions)
        val accountsList = LinkedList<Account>()
        entityList.forEach {
            accountsList.add(accountMapper.fetch(it))
        }
        return accountsList.find { it.id == accountId }?.balance
    }
}

