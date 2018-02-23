package com.clouway.app.adapter.datastore

import com.clouway.app.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DataStoreRule

class DatastoreAccountRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var accountRepository: AccountRepository
    private val userId = 1L// random value


    @Before
    fun setUp() {
        transactionRepository = DatastoreTransactionRepository(dataStoreRule.datastoreTemplate)
        accountRepository = DatastoreAccountRepository(dataStoreRule.datastoreTemplate, transactionRepository)
    }

    @Test
    fun makeADeposit() {
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(accountRepository.updateBalance(accountId, userId, 30f).isSuccessful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun makeAValidWithdraw() {
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 50f))
        assertThat(accountRepository.updateBalance(accountId, userId, -20f).isSuccessful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(30f)))
    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        assertThat(accountRepository.updateBalance(accountId, userId, -20f).isSuccessful, `is`(equalTo(false)))
        assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, `is`(equalTo(0f)))
    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        assertThat(accountRepository.updateBalance(-1L, -1L, 20f).isSuccessful, `is`(equalTo(false)))
    }

    @Test
    fun checkIfTransactionRepositoryIsCalledOnUpdateBalance() {
        val mockTransactionRepository = object : TransactionRepository {
            override fun registerTransaction(transaction: Transaction): Boolean {
                assertThat(transaction.userId, `is`(equalTo(userId)))
                return true
            }

            override fun getTransactions(userId: Long): List<Transaction> = emptyList()
        }
        val fakeAccountRepository = DatastoreAccountRepository(dataStoreRule.datastoreTemplate, mockTransactionRepository)
        val accountId = fakeAccountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        fakeAccountRepository.updateBalance(accountId, userId, 30f)
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)), `is`(equalTo(-1L)))
    }

    @Test
    fun getAllAccountsByUserId() {
        val account1 = Account("Fund for something", userId, Currency.BGN, 0f)
        val account2 = Account("Fund for other something", userId, Currency.BGN, 0f)
        val accountId1 = accountRepository.registerAccount(account1)
        val accountId2 = accountRepository.registerAccount(account2)
        assertThat(accountRepository.getAllAccounts(userId), `is`(equalTo(listOf(
                account1.apply { id = accountId1 },
                account2.apply { id = accountId2 }
        ))))
    }

    @Test
    fun tryToGetAllAccountsByUnregisteredUserId() {
        assertThat(accountRepository.getAllAccounts(1L), `is`(equalTo(emptyList())))
    }

    @Test
    fun getAccountThatWasRegistered() {
        val account = Account("Fund for something", userId, Currency.BGN, 0f)
        val accountId = accountRepository.registerAccount(account)
        assertThat(accountRepository.getUserAccount(userId, accountId), `is`(equalTo(account.apply { id = accountId })))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        assertThat(accountRepository.getUserAccount(1L, 1L), `is`(nullValue()))
    }

    @Test
    fun tryToGetRegisteredAccountOfOtherUser() {
        val accountId = accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        assertThat(accountRepository.getUserAccount(userId + 1L, accountId), `is`(nullValue()))
    }

    @Test
    fun removeAccountThatWasRegistered() {
        val accountId = accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        assertThat(accountRepository.removeAccount(accountId, userId).isSuccessful, `is`(equalTo(true)))
        assertThat(accountRepository.getUserAccount(userId, accountId), `is`(nullValue()))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        assertThat(accountRepository.removeAccount(-1L, -1L).isSuccessful, `is`(equalTo(false)))
    }
}
