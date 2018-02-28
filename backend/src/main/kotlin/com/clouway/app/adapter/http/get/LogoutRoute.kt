package com.clouway.app.adapter.http.get

import com.clouway.app.core.Observer
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import com.clouway.app.core.httpresponse.HttpError
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class LogoutRoute(
        private val sessionRepository: SessionRepository,
        private val userRepository: UserRepository,
        private val observer: Observer,
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
                val session = sessionRepository.getSessionAvailableAt(sessionId, LocalDateTime.now())
                val username = userRepository.getUsername(session?.userId ?: -1L) ?: "Someone"
                sessionRepository.terminateSession(req.cookie("sessionId"))
                req.session().invalidate()
                observer.onLogout(username)
                resp.redirect("/index")
            } catch (e: Exception) {
                logger.error("Unable to terminate session", e)
                resp.type("application/json")
                resp.status(400)
                HttpError("Unable to terminate your session.")
            }
        }
    }
}