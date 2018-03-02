package com.clouway.bank.adapter.gcp.datastore

import com.clouway.bank.core.Session
import com.clouway.bank.core.Sessions
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule
import java.time.LocalDateTime

class PersistentSessionsTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var sessions: Sessions
    val userId = 1L// random value

    @Before
    fun setUp() {
        sessions = PersistentSessions(dataStoreRule.datastore)
    }

    @Test
    fun getSessionThatWasRegistered() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessions.registerSession(session)
        assertThat(sessions.getSessionAvailableAt(sessionId!!, instant), `is`(equalTo(session)))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 15, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessions.registerSession(session)
        assertThat(sessions.getSessionAvailableAt(sessionId!!, instant), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessions.getSessionAvailableAt("notExistingId", instant), `is`(nullValue()))
    }

    @Test
    fun getSessionsCount() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessions.getSessionsCount(instant), `is`(equalTo(0)))
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        sessions.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(sessions.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun getSessionsCountOfOnlyNotExpiredSessions() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessions.getSessionsCount(instant), `is`(equalTo(0)))
        val createdOn1 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt1 = LocalDateTime.of(2018, 1, 12, 15, 10)
        val createdOn2 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt2 = LocalDateTime.of(2018, 1, 12, 14, 15)
        sessions.registerSession(Session(userId, createdOn1, expiresAt1))
        sessions.registerSession(Session(userId, createdOn2, expiresAt2))
        assertThat(sessions.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun terminateSessionThatWasRegistered() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val sessionId = sessions.registerSession(Session(userId, createdOn, expiresAt))
        sessions.terminateSession(sessionId!!)
        assertThat(sessions.getSessionAvailableAt(sessionId, instant), `is`(nullValue()))
    }

    @Test
    fun terminateInactiveSession() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val activeTime = LocalDateTime.of(2018, 1, 12, 14, 30)
        val instant = LocalDateTime.of(2018, 1, 12, 15, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessions.registerSession(session)!!
        assertThat(sessions.getSessionAvailableAt(sessionId, activeTime), `is`(equalTo(session)))
        assertThat(sessions.terminateInactiveSessions(instant), `is`(equalTo(1)))
        assertThat(sessions.getSessionAvailableAt(sessionId, activeTime), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateInactiveSessionsWhenAllSessionsAreActive() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 30)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessions.registerSession(session)!!
        assertThat(sessions.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
        assertThat(sessions.terminateInactiveSessions(instant), `is`(equalTo(0)))
        assertThat(sessions.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
    }
}
