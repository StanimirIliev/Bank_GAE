package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.accounts.dto.MessageResponse
import com.clouway.bank.core.*
import com.clouway.bank.core.ErrorType.*
import com.google.gson.Gson
import spark.Request
import spark.Response

class Deposit(private val accounts: Accounts, private val transactionListener: TransactionListener) : SecuredRoute {

    data class Params(val value: Float?)
    data class ParamsWrapper(val params: Params)

    override fun handle(req: Request, resp: Response, session: Session): Any {
        val data = Gson().fromJson(req.body(), ParamsWrapper::class.java)
        val accountId = req.params("id").toLongOrNull()
        val amount = data?.params?.value
        if (accountId == null || amount == null) {
            resp.status(400)
            return ErrorResponse("Cannot execute this deposit. No account id or value passed with the request.")
        }
        val operationResponse = accounts.updateBalance(accountId, session.userId, amount)
        if (operationResponse.isSuccessful) {
            resp.status(201)
            transactionListener.onTransaction(
                    session.userId,
                    accountId,
                    Operation.DEPOSIT,
                    amount
            )
            return MessageResponse("Deposit successful.")
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
            ACCESS_DENIED -> {
                resp.status(401)
                ErrorResponse("Cannot execute this deposit. Access denied.")
            }
            else -> {
                resp.status(500)
                ErrorResponse("Error occurred while executing the deposit.")
            }
        }
    }
}