package com.clouway.app.adapter.datastore

import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.datastore.core.EntityMapper
import com.google.appengine.api.datastore.*
import java.time.LocalDateTime
import java.util.*

class DatastoreSessionRepository(private val datastore: DatastoreService) : SessionRepository {

    private val fetchOptions = FetchOptions.Builder.withDefaults()

    override fun registerSession(session: Session): String? {
        val id = UUID.randomUUID().toString()
        val entity = Entity("Sessions", id)
        entity.setProperty("UserId", session.userId)
        entity.setProperty("CreatedOn", session.createdOn.toString())
        entity.setProperty("ExpiresAt", session.expiresAt.toString())
        return datastore.put(entity)?.name
    }

    override fun getSessionAvailableAt(sessionId: String, instant: LocalDateTime): Session? {
        val allEntities = datastore.prepare(Query("Sessions")).asList(fetchOptions)
        val desiredEntity = allEntities.find {
            it.key.name == sessionId && LocalDateTime.parse(it.getProperty("ExpiresAt").toString()).isAfter(instant)
        } ?: return null
        return Session(
                desiredEntity.getProperty("UserId").toString().toLong(),
                LocalDateTime.parse(desiredEntity.getProperty("CreatedOn").toString()),
                LocalDateTime.parse(desiredEntity.getProperty("ExpiresAt").toString())
        )
    }

    override fun getSessionsCount(instant: LocalDateTime): Int {
        val entityList = datastore.prepare(Query("Sessions")).asList(fetchOptions)
        val sessions = LinkedList<Session>()
        val sessionMapper =
                object : EntityMapper<Session> {
                    override fun fetch(entity: Entity): Session {
                        return Session(
                                entity.getProperty("UserId").toString().toLong(),
                                LocalDateTime.parse(entity.getProperty("CreatedOn").toString()),
                                LocalDateTime.parse(entity.getProperty("ExpiresAt").toString())
                        )
                    }
                }
        entityList.forEach {
            sessions.add(sessionMapper.fetch(it))
        }
        return sessions.filter { it.expiresAt.isAfter(instant) }.count()
    }

    @Throws(DatastoreFailureException::class, ConcurrentModificationException::class)
    override fun terminateSession(sessionId: String) {
        val key = KeyFactory.createKey("Sessions", sessionId)
        datastore.delete(key)
    }

    override fun terminateInactiveSessions(instant: LocalDateTime): Int {
        data class PairSessionSID(val session: Session, val sessionId: String)

        val pairSessionSIDMapper =
                object : EntityMapper<PairSessionSID> {
                    override fun fetch(entity: Entity): PairSessionSID {
                        return PairSessionSID(Session(
                                entity.getProperty("UserId").toString().toLong(),
                                LocalDateTime.parse(entity.getProperty("CreatedOn").toString()),
                                LocalDateTime.parse(entity.getProperty("ExpiresAt").toString())
                        ), entity.key.name)
                    }
                }
        val entityList = datastore.prepare(Query("Sessions")).asList(fetchOptions)
        val sessions = LinkedList<PairSessionSID>()
        entityList.forEach {
            sessions.add(pairSessionSIDMapper.fetch(it))
        }
        val inactiveSessions = sessions.filter { it.session.expiresAt.isBefore(instant) }
        inactiveSessions.forEach { terminateSession(it.sessionId) }
        return inactiveSessions.count()
    }
}