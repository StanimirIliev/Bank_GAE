package com.clouway.app.adapter.datastore

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.google.appengine.api.memcache.MemcacheServiceFactory
import org.apache.log4j.Logger
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule
import java.time.LocalDateTime

class DatastoreSessionRepositoryTest {

    @Rule
    @JvmField
    val dataStoreRule = DatastoreRule()
    private lateinit var sessionRepository: SessionRepository
    val userId = 1L// random value

    @Before
    fun setUp() {
        sessionRepository = DatastoreSessionRepository(dataStoreRule.datastoreTemplate)
    }

    @Test
    fun getSessionThatWasRegistered() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessionRepository.registerSession(session)
        assertThat(sessionRepository.getSessionAvailableAt(sessionId!!, instant), `is`(equalTo(session)))
    }

    @Test
    fun tryToGetSessionThatWasExpired() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 15, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessionRepository.registerSession(session)
        assertThat(sessionRepository.getSessionAvailableAt(sessionId!!, instant), `is`(nullValue()))
    }

    @Test
    fun tryToGetSessionWithWrongId() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionAvailableAt("notExistingId", instant), `is`(nullValue()))
    }

    @Test
    fun getSessionsCount() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun getSessionsCountOfOnlyNotExpiredSessions() {
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(0)))
        val createdOn1 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt1 = LocalDateTime.of(2018, 1, 12, 15, 10)
        val createdOn2 = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt2 = LocalDateTime.of(2018, 1, 12, 14, 15)
        sessionRepository.registerSession(Session(userId, createdOn1, expiresAt1))
        sessionRepository.registerSession(Session(userId, createdOn2, expiresAt2))
        assertThat(sessionRepository.getSessionsCount(instant), `is`(equalTo(1)))
    }

    @Test
    fun terminateSessionThatWasRegistered() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 25)
        val sessionId = sessionRepository.registerSession(Session(userId, createdOn, expiresAt))
        assertThat(sessionRepository.terminateSession(sessionId!!), `is`(equalTo(true)))
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateSessionThatWasNotRegistered() {
        assertThat(sessionRepository.terminateSession("notExistingId"), `is`(equalTo(false)))
    }

    @Test
    fun terminateInactiveSession() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val activeTime = LocalDateTime.of(2018, 1, 12, 14, 30)
        val instant = LocalDateTime.of(2018, 1, 12, 15, 25)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessionRepository.registerSession(session)!!
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, activeTime), `is`(equalTo(session)))
        assertThat(sessionRepository.terminateInactiveSessions(instant), `is`(equalTo(1)))
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, activeTime), `is`(nullValue()))
    }

    @Test
    fun tryToTerminateInactiveSessionsWhenAllSessionsAreActive() {
        val createdOn = LocalDateTime.of(2018, 1, 12, 14, 10)
        val expiresAt = LocalDateTime.of(2018, 1, 12, 15, 10)
        val instant = LocalDateTime.of(2018, 1, 12, 14, 30)
        val session = Session(userId, createdOn, expiresAt)
        val sessionId = sessionRepository.registerSession(session)!!
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
        assertThat(sessionRepository.terminateInactiveSessions(instant), `is`(equalTo(0)))
        assertThat(sessionRepository.getSessionAvailableAt(sessionId, instant), `is`(equalTo(session)))
    }
}
