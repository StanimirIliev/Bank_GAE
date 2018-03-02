package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.AccountsListResponse
import com.clouway.bank.core.Accounts
import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import spark.Request
import spark.Response

class GetAccounts(private val accounts: Accounts) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any =
            AccountsListResponse(accounts.getActiveAccounts(session.userId))
}

