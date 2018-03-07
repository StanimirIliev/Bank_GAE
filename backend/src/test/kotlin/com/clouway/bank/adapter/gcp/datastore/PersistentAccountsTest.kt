package com.clouway.bank.adapter.gcp.datastore

import com.clouway.bank.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

class PersistentAccountsTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var transactions: Transactions
    private lateinit var accounts: Accounts
    private val userId = 1L// random value


    @Before
    fun setUp() {
        transactions = PersistentTransactions(dataStoreRule.datastore)
        accounts = PersistentAccounts(dataStoreRule.datastore, transactions)
    }

    @Test
    fun makeADeposit() {
        val accountId = accounts.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(accounts.updateBalance(accountId, userId, 30f).isSuccessful, `is`(equalTo(true)))
        assertThat(accounts.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun makeAWithdraw() {
        val accountId = accounts.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        assertThat(accounts.updateBalance(accountId, userId, -20f).isSuccessful, `is`(equalTo(true)))
        assertThat(accounts.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun checkIfTransactionRepositoryIsCalledOnUpdateBalance() {
        val mockTransactionRepository = object : Transactions {
            override fun registerTransaction(transaction: Transaction): Boolean {
                assertThat(transaction.userId, `is`(equalTo(userId)))
                return true
            }

            override fun getTransactions(userId: Long): List<Transaction> = emptyList()
        }
        val fakeAccountRepository = PersistentAccounts(dataStoreRule.datastore, mockTransactionRepository)
        val accountId = fakeAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        fakeAccountRepository.updateBalance(accountId, userId, 30f)
    }

    @Test
    fun getAllActiveAccountsByUserId() {
        val account1 = Account("Fund for something", userId, Currency.BGN, 0f)
        val account2 = Account("Fund for other something", userId, Currency.BGN, 0f)
        val accountId1 = accounts.registerAccount(account1)
        val accountId2 = accounts.registerAccount(account2)
        assertThat(accounts.getActiveAccounts(userId), `is`(equalTo(listOf(
                account1.apply { id = accountId1 },
                account2.apply { id = accountId2 }
        ))))
    }

    @Test
    fun getAllAccountsByUserId() {
        val account1 = Account("Fund for something", userId, Currency.BGN, 0f)
        val account2 = Account("Fund for other something", userId, Currency.BGN, 0f)
        val accountId1 = accounts.registerAccount(account1)
        val accountId2 = accounts.registerAccount(account2)
        accounts.removeAccount(accountId1, userId)
        assertThat(accounts.getAllAccounts(userId), `is`(equalTo(listOf(
                account1.apply { id = accountId1 },
                account2.apply { id = accountId2 }
        ))))
    }

    @Test
    fun getAccountThatWasRegistered() {
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = accounts.registerAccount(account)
        assertThat(accounts.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun removeAccountThatWasRegistered() {
        val accountId = accounts.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accounts.removeAccount(accountId, userId).isSuccessful, `is`(equalTo(true)))
        assertThat(accounts.getUserAccount(userId, accountId), `is`(nullValue()))
    }
}
