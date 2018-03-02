package com.clouway.bank.adapter.http.common

import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import spark.Request
import spark.Response
import java.nio.charset.Charset

class ShowHomePage : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        resp.type("text/html")
        return ShowHomePage::class.java.getResourceAsStream("homePage.html").reader(Charset.defaultCharset()).readText()
    }
}