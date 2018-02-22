package com.clouway.app.adapter.datastore

import com.clouway.app.core.*
import rules.DataStoreRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class DatastoreTransactionRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()
    private lateinit var transactionRepository: TransactionRepository
    private val userId = 1L// random value
    private val accountId = 1L// random value
    
    @Before
    fun setUp() {
        transactionRepository = DatastoreTransactionRepository(dataStoreRule.datastoreTemplate)
    }

    @Test
    fun getTransactionThatWasRegistered() {
        val transactionCreatedOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val transaction = Transaction(userId, accountId, transactionCreatedOn, Operation.DEPOSIT, 5f)
        assertThat(transactionRepository.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(transactionRepository.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(transactionRepository.getTransactions(1), `is`(equalTo(emptyList())))
    }
}