package com.clouway.bank.adapter.validation

import com.clouway.bank.adapter.gcp.datastore.PersistentAccounts
import com.clouway.bank.adapter.gcp.datastore.PersistentTransactions
import com.clouway.bank.core.Account
import com.clouway.bank.core.Accounts
import com.clouway.bank.core.Currency
import com.clouway.bank.core.Transactions
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
class ValidationAccountsProxyTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var transactions: Transactions
    private lateinit var accounts: Accounts
    private val userId = 1L// random value


    @Before
    fun setUp() {
        transactions = PersistentTransactions(dataStoreRule.datastore)
        accounts = ValidationAccountsProxy(
                PersistentAccounts(dataStoreRule.datastore, transactions),
                dataStoreRule.datastore
        )
    }

    @Test
    fun tryToMakeWithdrawGreaterThanBalance() {
        val accountId = accounts.registerAccount(Account("some fund", userId, Currency.BGN, 0f))
        Assert.assertThat(accounts.updateBalance(accountId, userId, -20f).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
        Assert.assertThat(accounts.getUserAccount(userId, accountId)!!.balance, CoreMatchers.`is`(CoreMatchers.equalTo(0f)))
    }

    @Test
    fun tryToMakeTransactionWithUnregisteredAccountId() {
        Assert.assertThat(accounts.updateBalance(-1L, -1L, 20f).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }

    @Test
    fun tryToAddTwoAccountsForOneUserWithTheSameTitles() {
        accounts.registerAccount(Account("Fund for something", userId, Currency.BGN, 0f))
        Assert.assertThat(accounts.registerAccount(
                Account("Fund for something", userId, Currency.BGN, 0f)), CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }

    @Test
    fun tryToGetAllAccountsByUnregisteredUserId() {
        Assert.assertThat(accounts.getAllAccounts(1L), CoreMatchers.`is`(CoreMatchers.equalTo(emptyList())))
    }

    @Test
    fun tryToGetUnregisteredAccount() {
        Assert.assertThat(accounts.getUserAccount(1L, 1L), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToGetRegisteredAccountOfOtherUser() {
        val accountId = accounts.registerAccount(Account("Some fund", userId, Currency.BGN, 0f))
        Assert.assertThat(accounts.getUserAccount(userId + 1L, accountId), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToRemoveUnregisteredAccount() {
        Assert.assertThat(accounts.removeAccount(-1L, -1L).isSuccessful, CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }
}