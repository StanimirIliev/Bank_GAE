package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import com.clouway.bank.core.Sessions
import org.apache.log4j.Logger
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class Secured(
        private val sessions: Sessions,
        private val securedRoute: SecuredRoute,
        private val logger: Logger
) : Route {
    override fun handle(req: Request, resp: Response): Any {
        lateinit var session: Session
        try {
            session = sessions.getSessionAvailableAt(req.cookie("sessionId")!!, LocalDateTime.now())!!
        } catch (e: NullPointerException) {
            logger.info("Invalid session, redirecting to index page")
            return resp.redirect("/index")
        } finally {
            resp.type("application/json")
        }
        return securedRoute.handle(req, resp, session)
    }
}