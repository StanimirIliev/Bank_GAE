package com.clouway.app.adapter.datastore

import com.clouway.app.datastore.core.DatastoreTemplate
import com.clouway.app.datastore.core.EntityMapper
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.KeyFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class DatastoreSessionRepository(private val datastoreTemplate: DatastoreTemplate) : SessionRepository {
    override fun registerSession(session: Session): String? {
        val id = UUID.randomUUID().toString()
        val entity = Entity("Sessions", id)
        entity.setProperty("UserId", session.userId)
        entity.setProperty("CreatedOn", session.createdOn.toString())
        entity.setProperty("ExpiresAt", session.expiresAt.toString())
        return datastoreTemplate.insert(entity)?.name
    }

    override fun getSessionAvailableAt(sessionId: String, instant: LocalDateTime): Session? {
        val allEntities = datastoreTemplate.fetch("Sessions", null, object : EntityMapper<Entity> {
            override fun fetch(entity: Entity): Entity = entity
        })
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
        val sessions = datastoreTemplate.fetch("Sessions", null, object : EntityMapper<Session> {
            override fun fetch(entity: Entity): Session {
                return Session(
                        entity.getProperty("UserId").toString().toLong(),
                        LocalDateTime.parse(entity.getProperty("CreatedOn").toString()),
                        LocalDateTime.parse(entity.getProperty("ExpiresAt").toString())
                )
            }
        })
        return sessions.filter { it.expiresAt.isAfter(instant) }.count()
    }

    override fun terminateSession(sessionId: String): Boolean {
        val key = KeyFactory.createKey("Sessions", sessionId)
        return datastoreTemplate.delete(key)
    }

    override fun terminateInactiveSessions(instant: LocalDateTime): Int {
        val sessions = datastoreTemplate.fetch("Sessions", null, object : EntityMapper<Pair<Session, String>> {
            override fun fetch(entity: Entity): Pair<Session, String> {
                return Pair(Session(
                        entity.getProperty("UserId").toString().toLong(),
                        LocalDateTime.parse(entity.getProperty("CreatedOn").toString()),
                        LocalDateTime.parse(entity.getProperty("ExpiresAt").toString())
                ), entity.key.name)
            }
        })
        val inactiveSessions = sessions.filter { it.first.expiresAt.isBefore(instant) }
        inactiveSessions.forEach { terminateSession(it.second) }
        return inactiveSessions.count()
    }
}