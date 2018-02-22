package com.clouway.app.adapter.http.get

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.httpresponse.GetAccountResponseDto
import com.clouway.app.core.httpresponse.HttpError
import spark.Request
import spark.Response

class AccountDetailsRoute(private val accountRepository: AccountRepository) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(400)
            return HttpError("Cannot get account. No account id passed with the request.")
        } else {
            val account = accountRepository.getUserAccount(session.userId, accountId.toLong())
            if (account == null) {
                resp.status(404)
                return HttpError("Account not found.")
            }
            return GetAccountResponseDto(account)
        }
    }
}