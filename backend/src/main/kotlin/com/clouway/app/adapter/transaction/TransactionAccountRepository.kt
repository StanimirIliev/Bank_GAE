package com.clouway.app.adapter.transaction

import com.clouway.app.core.*
import com.clouway.app.core.Currency
import com.clouway.app.core.Transaction
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.*
import java.time.LocalDateTime
import java.util.*

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class TransactionAccountRepository(
        private val origin: AccountRepository,
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
        return origin.registerAccount(account)
    }

    override fun updateBalance(accountId: Long, userId: Long, amount: Float): OperationResponse {
        val transaction = datastore.beginTransaction(TransactionOptions.Builder.withXG(true))
        val balance = getBalance(accountId) ?: return OperationResponse(false, ErrorType.INCORRECT_ID)
        val entitiesList = datastore.prepare(Query("Accounts")).asList(fetchOptions)
        val oldEntity = entitiesList.find { it.key.id == accountId }!!
        val entity = Entity("Accounts", accountId)
        entity.setPropertiesFrom(oldEntity)
        entity.setProperty("Balance", amount + balance)
        try {
            datastore.put(transaction, entity)
            if (!transactionRepository.registerTransaction(Transaction(userId, accountId, LocalDateTime.now(),
                            if (amount < 0) Operation.WITHDRAW else Operation.DEPOSIT, amount))) {
                return OperationResponse(false, ErrorType.INTERNAL_ERROR)
            }
            return OperationResponse(true, null)
        } catch (e: Exception) {
            transaction.rollback()
            return OperationResponse(false, ErrorType.INTERNAL_ERROR)
        }
    }

    override fun getAllAccounts(userId: Long): List<Account> {
        return origin.getAllAccounts(userId)
    }

    override fun removeAccount(accountId: Long, userId: Long): OperationResponse {
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