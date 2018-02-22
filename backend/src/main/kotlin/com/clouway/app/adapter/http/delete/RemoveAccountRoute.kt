package com.clouway.app.adapter.http.delete

import com.clouway.app.core.AccountRepository
import com.clouway.app.core.ErrorType.ACCOUNT_NOT_FOUND
import com.clouway.app.core.SecuredRoute
import com.clouway.app.core.Session
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.httpresponse.HttpError
import org.apache.log4j.Logger
import spark.Request
import spark.Response

class RemoveAccountRoute(
        private val accountRepository: AccountRepository,
        private val logger: Logger
) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val accountId = req.params("id")
        if (accountId == null) {
            resp.status(400)
            return HttpError("Cannot remove this account. No account id passed with the request.")
        } else {
            val operationResponse = accountRepository.removeAccount(accountId.toLong(), session.userId)

            if (operationResponse.isSuccessful) {
                resp.status(200)
                return GetMessageResponseDto("This account has been removed successfully.")
            }
            if (operationResponse.error == ACCOUNT_NOT_FOUND) {
                resp.status(404)
                return HttpError("Account not found.")
            }
            resp.status(500)
            logger.fatal("Error occurred while removing account $accountId, requested by user ${session.userId}")
            return HttpError("Error occurred while removing this account.")
        }
    }
}