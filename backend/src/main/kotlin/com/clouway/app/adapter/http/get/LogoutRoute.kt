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
import java.util.*

class LogoutRoute(
        private val sessionRepository: SessionRepository,
        private val userRepository: UserRepository,
        private val logger: Logger
) : Route {

    private val observers = LinkedList<Observer>()

    override fun handle(req: Request, resp: Response): Any {
        val sessionId = req.cookie("sessionId")
        return if (sessionId == null) {
            resp.status(400)
            logger.error("Error occurred while getting the cookie sessionId")
            "{\"message\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            val session = sessionRepository.getSessionAvailableAt(sessionId, LocalDateTime.now())
            var username = userRepository.getUsername(session?.userId ?: -1L) ?: "Someone"
            if (!sessionRepository.terminateSession(sessionId)) {
                resp.type("application/json")
                resp.status(400)
                HttpError("Unable to terminate your session.")
            } else {
                req.session().invalidate()
                notifyAllObservers(username)
                resp.redirect("/index")
            }
        }
    }

    fun attachObserver(observer: Observer) {
        observers.add(observer)
    }

    private fun notifyAllObservers(username: String) {
        observers.forEach { it.onLogout(username) }
    }
}