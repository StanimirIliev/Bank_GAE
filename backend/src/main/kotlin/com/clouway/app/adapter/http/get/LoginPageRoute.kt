package com.clouway.app.adapter.http.get

import com.clouway.app.core.Error
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter

class LoginPageRoute(private val config: Configuration) : Route {
    override fun handle(request: Request, response: Response): Any {
        val template = config.getTemplate("login.ftlh")
        response.type("text/html")
        val out = StringWriter()
        template.process(Error(""), out)
        return out.toString()
    }
}