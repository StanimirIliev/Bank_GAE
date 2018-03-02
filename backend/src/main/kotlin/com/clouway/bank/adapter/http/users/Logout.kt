package com.clouway.bank.adapter.http.users

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.core.Sessions
import com.clouway.bank.core.UserEventHandler
import com.clouway.bank.core.Users
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class Logout(
        private val sessions: Sessions,
        private val users: Users,
        private val userEventHandler: UserEventHandler,
        private val logger: Logger
) : Route {

    override fun handle(req: Request, resp: Response): Any {
        val sessionId = req.cookie("sessionId")
        return if (sessionId == null) {
            resp.status(400)
            logger.error("Error occurred while getting the cookie sessionId")
            "{\"message\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            try {
                val session = sessions.getSessionAvailableAt(sessionId, LocalDateTime.now())
                val username = users.getUsername(session?.userId ?: -1L) ?: "Someone"
                sessions.terminateSession(req.cookie("sessionId"))
                req.session().invalidate()
                userEventHandler.onLogout(username)
                resp.redirect("/index")
            } catch (e: Exception) {
                logger.error("Unable to terminate session", e)
                resp.type("application/json")
                resp.status(400)
                ErrorResponse("Unable to terminate your session.")
            }
        }
    }
}