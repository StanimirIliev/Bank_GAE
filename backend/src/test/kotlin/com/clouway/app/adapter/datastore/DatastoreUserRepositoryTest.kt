package com.clouway.app.adapter.datastore

import com.clouway.app.core.User
import com.clouway.app.core.UserRepository
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule

class DatastoreUserRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        userRepository = DatastoreUserRepository(dataStoreRule.datastoreTemplate)
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        userRepository.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(userRepository.registerUser(User("someone@example.com", "user123", "password456")),
                `is`(equalTo(-1L)))
    }

    @Test
    fun authenticateUserThatWasRegisteredByItsUsername() {
        userRepository.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(userRepository.authenticateByUsername("user123", "password456"), `is`(equalTo(true)))
    }

    @Test
    fun assertThatEmailIsSentOnRegistration() {
        userRepository.registerUser(User("someone@example.com", "user123", "password456"))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        assertThat(userRepository.authenticateByUsername("user123", "password456"), `is`(equalTo(false)))
    }

    @Test
    fun getUsernameOfRegisteredUserByItsId() {
        val userId = userRepository.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(userRepository.getUsername(userId), `is`(equalTo("user123")))
    }

    @Test
    fun tryToGetUsernameWithWrongId() {
        assertThat(userRepository.getUsername(-1L), `is`(nullValue()))
    }

    @Test
    fun getIdOfRegisteredUserByItsUsername() {
        val userId = userRepository.registerUser(User("someone@example.com", "user123", "password456"))
        assertThat(userRepository.getUserId("user123"), `is`(equalTo(userId)))
    }

    @Test
    fun tryToGetIdOfUserWithWrongUsername() {
        assertThat(userRepository.getUserId("InvalidUsername"), `is`(equalTo(-1L)))
    }
}
