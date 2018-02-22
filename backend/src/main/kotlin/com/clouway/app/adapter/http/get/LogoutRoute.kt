package com.clouway.app.adapter.http.get

import com.clouway.app.core.SessionRepository
import com.clouway.app.core.httpresponse.HttpError
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route

class LogoutRoute(
        private val sessionRepository: SessionRepository,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {
        return if (req.cookie("sessionId") == null) {
            resp.status(400)
            logger.error("Error occurred while getting the cookie sessionId")
            "{\"message\":\"Error occurred while getting the cookie sessionId\"}"
        } else {
            if (!sessionRepository.terminateSession(req.cookie("sessionId"))) {
                resp.type("application/json")
                resp.status(400)
                HttpError("Unable to terminate your session.")
            } else {
                req.session().invalidate()
                resp.redirect("/index")
            }
        }
    }
}