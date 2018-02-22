package com.clouway.app.adapter.http

import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class Secured(
        private val sessionRepository: SessionRepository,
        private val securedRoute: SecuredRoute,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {
        lateinit var session: Session
        try {
            session = sessionRepository.getSessionAvailableAt(req.cookie("sessionId")!!, LocalDateTime.now())!!
        } catch (e: NullPointerException) {
            logger.info("Invalid session, redirecting to index page")
            return resp.redirect("/index")
        } finally {
            resp.type("application/json")
        }
        return securedRoute.handle(req, resp, session)
    }
}