package com.clouway.bank.adapter.http.users

import com.clouway.bank.core.*
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime

class LoginUserHandlerRoute(
        private val users: Users,
        private val sessions: Sessions,
        private val userEventHandler: UserEventHandler,
        private val config: Configuration
) : Route {
    data class Params(val usernameOfEmail: String, val password: String)

    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("login.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0].replace("%40", "@"), values[1])
        var authentication = users.authenticateByUsername(params.usernameOfEmail, params.password)
        if (!authentication) {
            authentication = users.authenticateByEmail(params.usernameOfEmail, params.password)
        }
        if (!authentication) {
            template.process(Error("Wrong username/email or password"), out)
            return out.toString()
        }
        val userId = users.getUserId(params.usernameOfEmail)
        val sessionId = sessions.registerSession(Session(
                userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        ))
        resp.cookie("sessionId", sessionId)
        userEventHandler.onLogin(params.usernameOfEmail)
        return resp.redirect("/home")
    }
}