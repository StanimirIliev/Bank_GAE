package com.clouway.app.adapter.http.post

import com.clouway.app.core.*
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

class RegisterUserHandler(private val userRepository: UserRepository,
                          private val sessionRepository: SessionRepository,
                          private val validator: RequestValidator,
                          private val config: Configuration) : Route {
    data class Params(val username: String, val password: String, val confirmPassword: String)

    override fun handle(req: Request, resp: Response): Any {
        val dataModel = HashMap<String, List<Error>>()
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0], values[1], values[2])
        val template = config.getTemplate("registration.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val errorList = validator.validate(req.queryMap().toMap())
        when {
            !errorList.isEmpty() -> {
                dataModel["errors"] = errorList
                template.process(dataModel.apply { put("errors", errorList) }, out)
                return out.toString()
            }
            params.password != params.confirmPassword -> {
                dataModel["errors"] = listOf(Error("The password and the confirm password does not match."))
                template.process(dataModel.apply {
                    put("errors", listOf(Error("The password and the " +
                            "confirm password does not match.")))
                }, out)
                return out.toString()
            }
            userRepository.registerUser(params.username, params.password) == -1L && errorList.isEmpty() -> {
                template.process(dataModel.apply {
                    put("errors",
                            listOf(Error("This username is already taken")))
                }, out)
                return out.toString()
            }
            else -> {
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
    }
}