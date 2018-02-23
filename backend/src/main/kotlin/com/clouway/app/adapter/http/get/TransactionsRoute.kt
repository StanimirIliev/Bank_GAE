package com.clouway.app.adapter.http.get

import com.clouway.app.core.*
import com.clouway.app.core.httpresponse.AccountTransactions
import com.clouway.app.core.httpresponse.GetListAccountTransactionsResponseDto
import com.clouway.app.core.httpresponse.GetTransactionsCountResponseDto
import com.clouway.app.core.httpresponse.HttpError
import spark.Request
import spark.Response
import java.util.*

class TransactionsRoute(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository
) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val parameter = req.params("param")
        if (parameter == "count") {
            return GetTransactionsCountResponseDto(transactionRepository.getTransactions(session.userId).count())
        } else {//    use parameter to show from which page to get transactions
            val page = parameter.toIntOrNull()
            val pageSize = req.queryParams("pageSize").toIntOrNull()
            if (page == null || pageSize == null) {
                resp.status(400)
                return HttpError("pageSize or page parameter is missing.")
            }
            val transactions = transactionRepository.getTransactions(session.userId).getFromPage(pageSize, page)
            val accountIds = transactions.map { it.accountId }
            val accounts = accountRepository.getAllAccounts(session.userId).filter { accountIds.contains(it.id) }
            val output = LinkedList<AccountTransactions>()
            accounts.forEach {
                val account = it
                output.add(AccountTransactions(
                        account, transactions.filter { it.accountId == account.id }
                ))
            }
            return GetListAccountTransactionsResponseDto(output)
        }
    }

    private fun List<Transaction>.getFromPage(pageSize: Int, page: Int): List<Transaction> {
        if (pageSize <= 0 || page <= 0) {
            return emptyList()
        }
        val elements = this.count()
        val fromIndex = (page - 1) * pageSize
        val toIndex = if (fromIndex + pageSize > elements) elements else fromIndex + pageSize
        if (fromIndex > toIndex || toIndex > elements) {
            return emptyList()
        }
        return this.subList(fromIndex, toIndex)
    }
}