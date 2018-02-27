package com.clouway.app.adapter.validation

import com.clouway.app.adapter.datastore.DatastoreAccountRepository
import com.clouway.app.adapter.datastore.DatastoreTransactionRepository
import com.clouway.app.core.Account
import com.clouway.app.core.AccountRepository
import com.clouway.app.core.Currency
import com.clouway.app.core.TransactionRepository
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class ValidationAccountRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var accountRepository: AccountRepository
    private val userId = 1L// random value


    @Before
    fun setUp() {
        transactionRepository = DatastoreTransactionRepository(dataStoreRule.datastore)
        accountRepository = ValidationAccountRepository(
                DatastoreAccountRepository(dataStoreRule.datastore, transactionRepository),
                dataStoreRule.datastore
        )
    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        val accountId = accountRepository.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        Assert.assertThat(accountRepository.updateBalance(accountId, userId, -20f).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
        Assert.assertThat(accountRepository.getUserAccount(userId, accountId)!!.balance, CoreMatchers.`is`(CoreMatchers.equalTo(0f)))
    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        Assert.assertThat(accountRepository.updateBalance(-1L, -1L, 20f).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        accountRepository.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        Assert.assertThat(accountRepository.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)), CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }

    @Test
    fun tryToGetAllAccountsByUnregisteredUserId() {
        Assert.assertThat(accountRepository.getAllAccounts(1L), CoreMatchers.`is`(CoreMatchers.equalTo(emptyList())))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        Assert.assertThat(accountRepository.getUserAccount(1L, 1L), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToGetRegisteredAccountOfOtherUser() {
        val accountId = accountRepository.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        Assert.assertThat(accountRepository.getUserAccount(userId + 1L, accountId), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        Assert.assertThat(accountRepository.removeAccount(-1L, -1L).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }
}