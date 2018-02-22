package com.clouway.app.adapter.http.post

import com.clouway.app.core.Error
import com.clouway.app.core.Session
import com.clouway.app.core.SessionRepository
import com.clouway.app.core.UserRepository
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime

class LoginUserHandler(private val userRepository: UserRepository, private val sessionRepository: SessionRepository,
                       private val config: Configuration) : Route {
    data class Params(val username: String, val password: String)

    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("login.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0], values[1])
        if (!userRepository.authenticate(params.username, params.password)) {
            template.process(Error("Wrong username or password"), out)
            return out.toString()
        }
        val userId = userRepository.getUserId(params.username)
        val sessionId = sessionRepository.registerSession(Session(
                userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        ))
        resp.cookie("sessionId", sessionId)
        return resp.redirect("/home")
    }
}