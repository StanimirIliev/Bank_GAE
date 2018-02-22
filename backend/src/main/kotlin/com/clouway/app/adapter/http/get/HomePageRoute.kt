package com.clouway.app.adapter.http.get

import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import spark.Request
import spark.Response
import java.nio.charset.Charset

class HomePageRoute : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        resp.type("text/html")
        return HomePageRoute::class.java.getResourceAsStream("homePage.html").reader(Charset.defaultCharset()).readText()
    }
}