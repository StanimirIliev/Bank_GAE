package com.clouway.app.adapter.http.get

import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class RemoveExpiredSessionsRoute(private val sessionRepository: SessionRepository, private val logger: Logger) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val removedSessions = sessionRepository.terminateInactiveSessions(LocalDateTime.now())
        if(removedSessions > 0) {
            logger.info("$removedSessions sessions was terminated because was expired.")
        }
        return resp.status(204)
    }
}