package com.clouway.app.adapter.http.get

import com.clouway.app.core.SessionRepository
import com.clouway.app.core.httpresponse.GetActivityResponseDto
import spark.Request
import spark.Response
import spark.Route
import java.time.LocalDateTime

class ActivityRoute(
        private val sessionRepository: SessionRepository
) : Route {
    override fun handle(req: Request, resp: Response): Any =
            GetActivityResponseDto(sessionRepository.getSessionsCount(LocalDateTime.now()))
}