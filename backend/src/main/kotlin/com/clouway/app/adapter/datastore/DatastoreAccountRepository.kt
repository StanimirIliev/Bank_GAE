package com.clouway.app.adapter.datastore

import com.clouway.app.core.*
import com.clouway.app.core.ErrorType.*
import com.clouway.app.datastore.core.DatastoreTemplate
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.Query.*
import java.time.LocalDateTime

class DatastoreAccountRepository(
        private val datastoreTemplate: DatastoreTemplate,
        private val transactionRepository: TransactionRepository
) : AccountRepository {

    private val entityMapper = object : EntityMapper<Entity> {
        override fun fetch(entity: Entity): Entity = entity
    }

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
        //Check for duplicating title for this user
        val filter = FilterPredicate("UserId", FilterOperator.EQUAL, account.userId)
        val list = datastoreTemplate.fetch("Accounts", filter, accountMapper)
        if (list.any { it.title == account.title }) {
            return -1L// Duplicate titles found
        }
        //Set account in the datastore
        val entity = Entity("Accounts")
        entity.setProperty("Title", account.title)
        entity.setProperty("UserId", account.userId)
        entity.setProperty("Currency", account.currency.toString())
        entity.setProperty("Balance", account.balance)
        entity.setProperty("DeletedOn", null)
        val key = datastoreTemplate.insert(entity)
        return key?.id ?: -1L
    }

    override fun updateBalance(accountId: Long, userId: Long, amount: Float): OperationResponse {
        val balance = getBalance(accountId) ?: return OperationResponse(false, INCORRECT_ID)
        if (amount + balance < 0) {
            return OperationResponse(false, LOW_BALANCE)
        }
        if (amount == 0f) {
            return OperationResponse(false, INVALID_REQUEST)
        }
        val entitiesList = datastoreTemplate.fetch("Accounts", null, entityMapper)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        if (oldEntity.getProperty("UserId").toString().toLong() != userId) {
            return OperationResponse(false, ACCESS_DENIED)
        }
        if (oldEntity.getProperty("DeletedOn") != null) {
            return OperationResponse(false, INCORRECT_ID)
        }
        val entity = Entity("Accounts", accountId)
        entity.setPropertiesFrom(oldEntity)
        entity.setProperty("Balance", amount + balance)
        return if (datastoreTemplate.update(entity) &&
                transactionRepository.registerTransaction(Transaction(userId, accountId, LocalDateTime.now(),
                        if (amount < 0) Operation.WITHDRAW else Operation.DEPOSIT, amount))) {
            OperationResponse(true, null)
        } else {
            OperationResponse(false, INTERNAL_ERROR)
        }
    }

    override fun getAllAccounts(userId: Long): List<Account> {
        val filter1 = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val filter2 = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val compositeFilter = Query.CompositeFilter(CompositeFilterOperator.AND, listOf(filter1, filter2))
        return datastoreTemplate.fetch(compositeFilter, "Accounts", accountMapper)
    }

    override fun removeAccount(accountId: Long, userId: Long): OperationResponse {
        if (getUserAccount(userId, accountId) == null) {
            return OperationResponse(false, ACCOUNT_NOT_FOUND)
        }
        val entitiesList = datastoreTemplate.fetch("Accounts", null, entityMapper)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        val entity = Entity("Accounts", accountId)
        entity.setPropertiesFrom(oldEntity)
        entity.setProperty("DeletedOn", LocalDateTime.now().toString())
        return if (datastoreTemplate.update(entity)) {
            OperationResponse(true, null)
        } else {
            OperationResponse(false, INTERNAL_ERROR)
        }
    }

    override fun getUserAccount(userId: Long, accountId: Long): Account? {
        val filter1 = FilterPredicate("UserId", FilterOperator.EQUAL, userId)
        val filter2 = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val compositeFilter = CompositeFilter(CompositeFilterOperator.AND, listOf(filter1, filter2))
        val userAccountsList = datastoreTemplate.fetch(compositeFilter, "Accounts", accountMapper)
        return userAccountsList.find { it.id == accountId }
    }

    private fun getBalance(accountId: Long): Float? {
        val filter = FilterPredicate("DeletedOn", FilterOperator.EQUAL, null)
        val accountsList = datastoreTemplate.fetch("Accounts", filter, accountMapper)
        return accountsList.find { it.id == accountId }?.balance
    }
}
