package com.clouway.app.adapter.validation

import com.clouway.app.adapter.datastore.DatastoreUserRepository
import com.clouway.app.core.Observer
import com.clouway.app.core.User
import com.clouway.app.core.UserRepository
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class ValidationUserRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    lateinit var userRepository: UserRepository
    private val fakeObserver = object : Observer {
        override fun onRegister(email: String, username: String) {}

        override fun onLogin(username: String) {}

        override fun onLogout(username: String) {}
    }

    @Before
    fun setUp() {
        userRepository = ValidationUserRepository(
                DatastoreUserRepository(dataStoreRule.datastore, fakeObserver), dataStoreRule.datastore
        )
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        userRepository.registerUser(User("someone@example.com", "user123", "password456"))
        Assert.assertThat(userRepository.registerUser(User("someone@example.com", "user123", "password456")),
                CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        Assert.assertThat(userRepository.authenticateByUsername("user123", "password456"), CoreMatchers.`is`(CoreMatchers.equalTo(false)))
    }

    @Test
    fun tryToGetUsernameWithWrongId() {
        Assert.assertThat(userRepository.getUsername(-1L), CoreMatchers.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun tryToGetIdOfUserWithWrongUsername() {
        Assert.assertThat(userRepository.getUserId("InvalidUsername"), CoreMatchers.`is`(CoreMatchers.equalTo(-1L)))
    }
}