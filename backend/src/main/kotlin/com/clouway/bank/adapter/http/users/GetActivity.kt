package com.clouway.bank.adapter.http.users

import com.clouway.bank.adapter.http.users.dto.ActivityResponse
import com.clouway.bank.core.Sessions
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class GetActivity(
        private val sessions: Sessions
) : Route {
    override fun handle(req: Request, resp: Response): Any =
            ActivityResponse(sessions.getSessionsCount(LocalDateTime.now()))
}