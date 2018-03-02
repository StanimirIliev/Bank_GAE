package com.clouway.bank.adapter.http.accounts

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.accounts.dto.MessageResponse
import com.clouway.bank.core.Accounts
import com.clouway.bank.core.ErrorType.ACCOUNT_NOT_FOUND
import com.clouway.bank.core.SecuredRoute
import com.clouway.bank.core.Session
import org.apache.log4j.Logger
import spark.Request
import spark.Response

class RemoveAccount(
        private val accounts: Accounts,
        private val logger: Logger
) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(400)
            return ErrorResponse("Cannot remove this account. No account id passed with the request.")
        } else {
            val operationResponse = accounts.removeAccount(accountId.toLong(), session.userId)

            if (operationResponse.isSuccessful) {
                resp.status(200)
                return MessageResponse("This account has been removed successfully.")
            }
            if (operationResponse.error == ACCOUNT_NOT_FOUND) {
                resp.status(404)
                return ErrorResponse("Account not found.")
            }
            resp.status(500)
            logger.fatal("Error occurred while removing account $accountId, requested by user ${session.userId}")
            return ErrorResponse("Error occurred while removing this account.")
        }
    }
}