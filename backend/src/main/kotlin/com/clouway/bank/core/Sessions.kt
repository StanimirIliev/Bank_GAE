package com.clouway.bank.core

import com.google.appengine.api.datastore.DatastoreFailureException
import java.time.LocalDateTime
import java.util.*

interface Sessions {

    /**
     * Registers session in the DB
     * @param session the DTO that would be registered
     * @return the session id if the operation was successful, null if it was not
     */
    fun registerSession(session: Session): String?

    /**
     * Gets session from the DB if it it not expired
     * @param sessionId the id of the session
     * @param instant the current DateTime
     * @return Session DTO if there is a match with this id or null if there is not
     */
    fun getSessionAvailableAt(sessionId: String, instant: LocalDateTime): Session?

    /**
     * Gets the count of all active sessions
     * @param instant the current DateTime
     * @return the count of all active sessions
     */
    fun getSessionsCount(instant: LocalDateTime): Int

    /**
     * Deletes session from the DB
     * @param sessionId the id of the session which should be deleted
     */
    @Throws(DatastoreFailureException::class, ConcurrentModificationException::class)
    fun terminateSession(sessionId: String)

    /**
     * Deletes all inactive sessions from the DB
     * @param instant the current DateTime
     * @return the count of deleted sessions
     */
    fun terminateInactiveSessions(instant: LocalDateTime): Int
}