package com.clouway.app.adapter.memcache

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.google.appengine.api.memcache.MemcacheServiceFactory
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.jmock.Expectations
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import rules.DatastoreRule
import java.time.LocalDateTime

class CachedSessionsTest {

    @Rule
    @JvmField
    val datastoreRule = DatastoreRule()

    @Rule
    @JvmField
    val context = JUnitRuleMockery()

    private val sessionRepository = context.mock(SessionRepository::class.java)
    private val instant = LocalDateTime.now()
    private val session = Session(1L, instant.minusHours(1), instant.plusHours(1))

    @Test
    fun getSessionThatWasCached() {
        context.checking(object : Expectations() {
            init {
                exactly(1).of(sessionRepository).registerSession(session)
                will(returnValue("SID"))
                never(sessionRepository).getSessionAvailableAt("SID", instant)
            }
        })
        val chainedSessionRepository = CachedSessions(sessionRepository, MemcacheServiceFactory.getMemcacheService())
        chainedSessionRepository.registerSession(session)
        assertThat(chainedSessionRepository.getSessionAvailableAt("SID", instant), `is`(equalTo(session)))
    }

    @Test
    fun getSessionThatIsNotCached() {
        context.checking(object : Expectations() {
            init {
                exactly(1).of(sessionRepository).getSessionAvailableAt("SID", instant)
                will(returnValue(session))
            }
        })
        val chainedSessionRepository = CachedSessions(sessionRepository, MemcacheServiceFactory.getMemcacheService())
        assertThat(chainedSessionRepository.getSessionAvailableAt("SID", instant), `is`(equalTo(session)))
    }

    @Test
    fun ensureThatTerminatingSessionWipesItFromCache() {
        context.checking(object : Expectations() {
            init {
                exactly(1).of(sessionRepository).registerSession(session)
                will(returnValue("SID"))
                exactly(1).of(sessionRepository).getSessionAvailableAt("SID", instant)
                will(returnValue(session))
                exactly(1).of(sessionRepository).terminateSession("SID")
            }
        })
        val chainedSessionRepository = CachedSessions(sessionRepository, MemcacheServiceFactory.getMemcacheService())
        chainedSessionRepository.registerSession(session)
        chainedSessionRepository.terminateSession("SID")
        assertThat(chainedSessionRepository.getSessionAvailableAt("SID", instant), `is`(equalTo(session)))
    }

    @Test
    fun tryToGetSessionThatWasCachedButWasExpired() {
        val expiredSession = Session(1L, instant.minusHours(2), instant.minusHours(1))
        context.checking(object : Expectations() {
            init {
                exactly(1).of(sessionRepository).registerSession(expiredSession)
                will(returnValue("SID"))
                never(sessionRepository).getSessionAvailableAt("SID", instant)
            }
        })
        val chainedSessionRepository = CachedSessions(sessionRepository, MemcacheServiceFactory.getMemcacheService())
        chainedSessionRepository.registerSession(expiredSession)
        assertThat(chainedSessionRepository.getSessionAvailableAt("SID", instant), `is`(nullValue()))
    }
}