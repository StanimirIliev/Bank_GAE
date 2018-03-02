package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.Error
import freemarker.template.Configuration
import spark.Request
import spark.Response
import spark.Route
import java.io.StringWriter

class ShowRegisterPage(private val config: Configuration) : Route {
    override fun handle(req: Request, resp: Response): Any {
        val template = config.getTemplate("registration.ftlh")
        resp.type("text/html")
        val out = StringWriter()
        template.process(mapOf(Pair<String, List<Error>>("errors", emptyList())), out)
        return out.toString()
    }
}