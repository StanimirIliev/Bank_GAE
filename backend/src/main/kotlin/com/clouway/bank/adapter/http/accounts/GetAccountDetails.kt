package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.AccountDetailsResponse
import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.core.Accounts
import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import spark.Request
import spark.Response

class GetAccountDetails(private val accounts: Accounts) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(400)
            return ErrorResponse("Cannot common account. No account id passed with the request.")
        } else {
            val account = accounts.getUserAccount(session.userId, accountId.toLong())
            if (account == null) {
                resp.status(404)
                return ErrorResponse("Account not found.")
            }
            return AccountDetailsResponse(account)
        }
    }
}