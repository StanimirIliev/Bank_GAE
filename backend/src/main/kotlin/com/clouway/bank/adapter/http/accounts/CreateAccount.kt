package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.accounts.dto.MessageResponse
import com.clouway.bank.core.*
import com.google.gson.Gson
import spark.Request
import spark.Response

class CreateAccount(private val accounts: Accounts) : SecuredRoute {

    data class Params(val title: String?, val currency: Currency?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val title = data.params.title
        val currency = data.params.currency
        val accounts = accounts.getActiveAccounts(session.userId)
        when {
            title == null || currency == null -> {
                resp.status(400)
                return ErrorResponse("Cannot open new account. No title or currency passed with the request.")
            }
            accounts.any { it.title == title } -> {
                resp.status(400)
                return ErrorResponse("You have already account with such a title.")
            }
            this.accounts.registerAccount(Account(title, session.userId, currency, 0f)) != -1L -> {
                resp.status(201)
                return MessageResponse("New account opened successful.")
            }
            else -> {
                resp.status(500)
                return ErrorResponse("Unable to open new account.")
            }
        }
    }
}