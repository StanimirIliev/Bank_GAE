package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.Sessions
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class RemoveExpiredSessions(private val sessions: Sessions, private val logger: Logger) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val removedSessions = sessions.terminateInactiveSessions(LocalDateTime.now())
        if (removedSessions > 0) {
            logger.info("$removedSessions sessions was terminated because was expired.")
        }
        return resp.status(204)
    }
}