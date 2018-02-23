package com.clouway.app.adapter.datastore

import com.clouway.app.core.UserRepository
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DataStoreRule

class DatastoreUserRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DataStoreRule()
    lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        userRepository = DatastoreUserRepository(dataStoreRule.datastoreTemplate)
    }

    @Test
    fun tryToRegisterAlreadyRegisteredUser() {
        userRepository.registerUser("user123", "password456")
        assertThat(userRepository.registerUser("user123", "password789"),
                `is`(equalTo(-1L)))
    }

    @Test
    fun authenticateUserThatWasRegistered() {
        userRepository.registerUser("user123", "password789")
        assertThat(userRepository.authenticate("user123", "password789"), `is`(equalTo(true)))
    }

    @Test
    fun tryToAuthenticateUnregisteredUser() {
        assertThat(userRepository.authenticate("user123", "password456"), `is`(equalTo(false)))
    }

    @Test
    fun getUsernameOfRegisteredUserByItsId() {
        val userId = userRepository.registerUser("user123", "password789")
        assertThat(userRepository.getUsername(userId), `is`(equalTo("user123")))
    }

    @Test
    fun tryToGetUsernameWithWrongId() {
        assertThat(userRepository.getUsername(-1L), `is`(nullValue()))
    }

    @Test
    fun getIdOfRegisteredUserByItsUsername() {
        val userId = userRepository.registerUser("user123", "password789")
        assertThat(userRepository.getUserId("user123"), `is`(equalTo(userId)))
    }

    @Test
    fun tryToGetIdOfUserWithWrongUsername() {
        assertThat(userRepository.getUserId("InvalidUsername"), `is`(equalTo(-1L)))
    }
}
