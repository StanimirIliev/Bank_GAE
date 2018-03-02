package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.Error
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter

class ShowLoginPage(private val config: Configuration) : Route {
    override fun handle(request: Request, response: Response): Any {
        val template = config.getTemplate("login.ftlh")
        response.type("text/html")
        val out = StringWriter()
        template.process(Error(""), out)
        return out.toString()
    }
}