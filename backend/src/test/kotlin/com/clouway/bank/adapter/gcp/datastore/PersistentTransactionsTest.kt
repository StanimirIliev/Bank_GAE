package com.clouway.bank.adapter.gcp.datastore

import com.clouway.bank.core.Operation
import com.clouway.bank.core.Transaction
import com.clouway.bank.core.Transactions
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule
import java.time.LocalDateTime

class PersistentTransactionsTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var transactions: Transactions
    private val userId = 1L// random value
    private val accountId = 1L// random value

    @Before
    fun setUp() {
        transactions = PersistentTransactions(dataStoreRule.datastore)
    }

    @Test
    fun getTransactionThatWasRegistered() {
        val transactionCreatedOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val transaction = Transaction(userId, accountId, transactionCreatedOn, Operation.DEPOSIT, 5f)
        assertThat(transactions.registerTransaction(transaction), `is`(equalTo(true)))
        assertThat(transactions.getTransactions(transaction.userId), `is`(equalTo(listOf(transaction))))
    }

    @Test
    fun tryToGetTransactionWithUserIdThatDoesNotExists() {
        assertThat(transactions.getTransactions(1), `is`(equalTo(emptyList())))
    }
}