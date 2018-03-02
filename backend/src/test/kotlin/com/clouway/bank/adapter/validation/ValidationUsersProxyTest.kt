package com.clouway.bank.adapter.validation

import com.clouway.bank.adapter.gcp.datastore.PersistentUsers
import com.clouway.bank.core.User
import com.clouway.bank.core.UserEventHandler
import com.clouway.bank.core.Users
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class ValidationUsersProxyTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    lateinit var users: Users
    private val fakeObserver = object : UserEventHandler {
        override fun onRegister(email: String, username: String) {}

        override fun onLogin(username: String) {}

        override fun onLogout(username: String) {}
    }

    @Before
    fun setUp() {
        users = ValidationUsersProxy(
                PersistentUsers(dataStoreRule.datastore, fakeObserver), dataStoreRule.datastore
        )
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        users.registerUser(User("someone@example.com", "user123", "password456"))
        Assert.assertThat(users.registerUser(User("someone@example.com", "user123", "password456")),
                CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        Assert.assertThat(users.authenticateByUsername("user123", "password456"), CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }

    @Test
    fun tryToGetUsernameWithWrongId() {
        Assert.assertThat(users.getUsername(-1L), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToGetIdOfUserWithWrongUsername() {
        Assert.assertThat(users.getUserId("InvalidUsername"), CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }
}