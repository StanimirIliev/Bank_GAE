package com.clouway.app.adapter.http.get

import spark.Request
import spark.Response
import spark.Route
import java.nio.charset.Charset

class IndexPageRoute : Route {
    override fun handle(request: Request, resp: Response): Any {
        resp.type("text/html")
        return IndexPageRoute::class.java.getResourceAsStream("index.html").reader(Charset.defaultCharset()).readText()
    }

}