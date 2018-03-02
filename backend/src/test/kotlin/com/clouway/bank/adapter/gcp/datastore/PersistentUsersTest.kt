package com.clouway.bank.adapter.gcp.datastore

import com.clouway.bank.core.User
import com.clouway.bank.core.UserEventHandler
import com.clouway.bank.core.Users
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

class PersistentUsersTest {

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
        users = PersistentUsers(dataStoreRule.datastore, fakeObserver)
    }

    @Test
    fun authenticateUserThatWasRegisteredByItsUsername() {
        users.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(users.authenticateByUsername("user123", "password456"), `is`(equalTo(true)))
    }

    @Test
    fun assertThatEmailIsSentOnRegistration() {
        users.registerUser(User("someone@example.com", "user123", "password456"))
    }

    @Test
    fun getUsernameOfRegisteredUserByItsId() {
        val userId = users.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(users.getUsername(userId), `is`(equalTo("user123")))
    }

    @Test
    fun getIdOfRegisteredUserByItsUsername() {
        val userId = users.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(users.getUserId("user123"), `is`(equalTo(userId)))
    }
}
