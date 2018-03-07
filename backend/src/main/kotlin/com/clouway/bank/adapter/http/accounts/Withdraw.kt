package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.accounts.dto.MessageResponse
import com.clouway.bank.core.*
import com.clouway.bank.core.ErrorType.*
import com.google.gson.Gson
import spark.Request
import spark.Response

class Withdraw(private val accounts: Accounts, private val transactionListener: TransactionListener) : SecuredRoute {

    data class Params(val value: Float?)
    data class Wrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), Wrapper::class.java)
        val accountId = req.params("id").toLongOrNull()
        val amount = data.params.value
        if (accountId == null || amount == null) {
            resp.status(400)
            return ErrorResponse("Error. Id or amount parameter not passed.")
        }
        val operationResponse = accounts.updateBalance(accountId, session.userId, amount * -1f)
        if (operationResponse.isSuccessful) {
            resp.status(201)
            transactionListener.onTransaction(
                    session.userId,
                    accountId,
                    Operation.WITHDRAW,
                    amount
            )
            return MessageResponse("Withdraw successful.")
        }
        return when (operationResponse.error) {
            INCORRECT_ID -> {
                resp.status(404)
                ErrorResponse("Account not found.")
            }
            INVALID_REQUEST -> {
                resp.status(400)
                ErrorResponse("Invalid request.")
            }
            LOW_BALANCE -> {
                resp.status(400)
                ErrorResponse("Cannot execute this withdraw. Not enough balance.")
            }
            ACCESS_DENIED -> {
                resp.status(401)
                ErrorResponse("Cannot execute this withdraw. Access denied.")
            }
            else -> {
                resp.status(500)
                ErrorResponse("Error occurred while executing the deposit.")
            }
        }
    }
}