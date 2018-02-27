package com.clouway.app.adapter.http.post

import com.clouway.app.core.*
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.httpresponse.HttpError
import com.google.gson.Gson
import spark.Request
import spark.Response

class NewAccountRoute(private val accountRepository: AccountRepository) : SecuredRoute {

    data class Params(val title: String?, val currency: Currency?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val title = data.params.title
        val currency = data.params.currency
        val accounts = accountRepository.getActiveAccounts(session.userId)
        when {
            title == null || currency == null -> {
                resp.status(400)
                return HttpError("Cannot open new account. No title or currency passed with the request.")
            }
            accounts.any { it.title == title } -> {
                resp.status(400)
                return HttpError("You have already account with such a title.")
            }
            accountRepository.registerAccount(Account(title, session.userId, currency, 0f)) != -1L -> {
                resp.status(201)
                return GetMessageResponseDto("New account opened successful.")
            }
            else -> {
                resp.status(500)
                return HttpError("Unable to open new account.")
            }
        }
    }
}