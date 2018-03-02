package com.clouway.bank.adapter.http.common

import spark.Request
import spark.Response
import spark.Route
import java.nio.charset.Charset

class ShowIndexPage : Route {
    override fun handle(request: Request, resp: Response): Any {
        resp.type("text/html")
        return ShowIndexPage::class.java.getResourceAsStream("index.html").reader(Charset.defaultCharset()).readText()
    }

}