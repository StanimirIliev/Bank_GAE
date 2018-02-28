package com.clouway.app.adapter.http.post

import com.clouway.app.core.*
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime

class LoginUserHandler(
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository,
        private val observer: Observer,
        private val config: Configuration
) : Route {
    data class Params(val usernameOfEmail: String, val password: String)

    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("login.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0].replace("%40", "@"), values[1])
        var authentication = userRepository.authenticateByUsername(params.usernameOfEmail, params.password)
        if (!authentication) {
            authentication = userRepository.authenticateByEmail(params.usernameOfEmail, params.password)
        }
        if (!authentication) {
            template.process(Error("Wrong username/email or password"), out)
            return out.toString()
        }
        val userId = userRepository.getUserId(params.usernameOfEmail)
        val sessionId = sessionRepository.registerSession(Session(
                userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        ))
        resp.cookie("sessionId", sessionId)
        observer.onLogin(params.usernameOfEmail)
        return resp.redirect("/home")
    }
}