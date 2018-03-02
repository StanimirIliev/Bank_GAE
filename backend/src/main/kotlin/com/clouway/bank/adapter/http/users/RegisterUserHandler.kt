package com.clouway.bank.adapter.http.users

import com.clouway.bank.core.*
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

class RegisterUserHandler(private val users: Users,
                          private val sessions: Sessions,
                          private val userEventHandler: UserEventHandler,
                          private val validator: RequestValidator,
                          private val config: Configuration) : Route {
    data class Params(val email: String, val username: String, val password: String, val confirmPassword: String)

    override fun handle(req: Request, resp: Response): Any {
        val dataModel = HashMap<String, List<Error>>()
        val values = req.body().split('&').map { it.substring(it.indexOf('=') + 1) }
        val params = Params(values[0].replace("%40", "@"), values[1], values[2], values[3])
        val template = config.getTemplate("registration.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        val errorList = validator.validate(mapOf(
                "email" to params.email,
                "username" to params.username,
                "password" to params.password
        ))
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
            errorList.isEmpty() && users.registerUser(User(params.email, params.username, params.password)) == -1L -> {
                template.process(dataModel.apply {
                    put("errors",
                            listOf(Error("This username or email is already taken")))
                }, out)
                return out.toString()
            }
            else -> {
                val userId = users.getUserId(params.username)
                val sessionId = sessions.registerSession(Session(
                        userId,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2)
                ))
                resp.cookie("sessionId", sessionId)
                userEventHandler.onLogin(params.username)
                return resp.redirect("/home")
            }
        }
    }
}