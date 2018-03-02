package com.clouway.bank.adapter.http.users

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.users.dto.UsernameResponse
import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import com.clouway.bank.core.Users
import spark.Request
import spark.Response

class GetUsername(private val users: Users) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val username = users.getUsername(session.userId)
        return if (username == null) {
            resp.status(400)
            ErrorResponse("Cannot common username. Invalid userId.")
        } else {
            UsernameResponse(username)
        }
    }
}