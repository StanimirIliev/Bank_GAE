package com.clouway.app.adapter.validation

import com.clouway.app.core.*
import com.clouway.app.core.Currency
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import java.util.*

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class ValidationAccountRepository(
        private val origin: AccountRepository,
        private val datastore: DatastoreService
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
        val query = Query("Accounts")
        query.filter = Query.FilterPredicate("UserId", Query.FilterOperator.EQUAL, account.userId)
        val entities = datastore.prepare(query).asList(fetchOptions)
        val list = LinkedList<Account>()
        entities.forEach {
            list.add(accountMapper.fetch(it))
        }
        if (list.any { it.title == account.title }) {
            return -1L// Duplicate titles found
        }
        return origin.registerAccount(account)
    }

    override fun updateBalance(accountId: Long, userId: Long, amount: Float): OperationResponse {
        val balance = getBalance(accountId) ?: return OperationResponse(false, ErrorType.INCORRECT_ID)
        if (amount + balance < 0) {
            return OperationResponse(false, ErrorType.LOW_BALANCE)
        }
        if (amount == 0f) {
            return OperationResponse(false, ErrorType.INVALID_REQUEST)
        }
        val entitiesList = datastore.prepare(Query("Accounts")).asList(fetchOptions)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        if (oldEntity.getProperty("UserId").toString().toLong() != userId) {
            return OperationResponse(false, ErrorType.ACCESS_DENIED)
        }
        if (oldEntity.getProperty("DeletedOn") != null) {
            return OperationResponse(false, ErrorType.INCORRECT_ID)
        }
        return origin.updateBalance(accountId, userId, amount)
    }

    override fun getAllAccounts(userId: Long): List<Account> {
        return origin.getAllAccounts(userId)
    }

    override fun removeAccount(accountId: Long, userId: Long): OperationResponse {
        if (getUserAccount(userId, accountId) == null) {
            return OperationResponse(false, ErrorType.ACCOUNT_NOT_FOUND)
        }
        return origin.removeAccount(accountId, userId)
    }

    override fun getUserAccount(userId: Long, accountId: Long): Account? {
        return origin.getUserAccount(userId, accountId)
    }

    override fun getActiveAccounts(userId: Long): List<Account> {
        return origin.getActiveAccounts(userId)
    }

    private fun getBalance(accountId: Long): Float? {
        val filter = Query.FilterPredicate("DeletedOn", Query.FilterOperator.EQUAL, null)
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