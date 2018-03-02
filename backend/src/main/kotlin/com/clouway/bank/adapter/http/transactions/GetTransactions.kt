package com.clouway.bank.adapter.http.transactions

import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.transactions.dto.AccountTransactions
import com.clouway.bank.adapter.http.transactions.dto.ListAccountTransactionsResponse
import com.clouway.bank.adapter.http.transactions.dto.TransactionCountResponse
import com.clouway.bank.core.*
import spark.Request
import spark.Response
import java.util.*

class GetTransactions(
        private val transactions: Transactions,
        private val accounts: Accounts
) : SecuredRoute {
    override fun handle(req: Request, resp: Response, session: Session): Any {
        val parameter = req.params("param")
        if (parameter == "count") {
            return TransactionCountResponse(transactions.getTransactions(session.userId).count())
        } else {//    use parameter to show from which page to common transactions
            val page = parameter.toIntOrNull()
            val pageSize = req.queryParams("pageSize").toIntOrNull()
            if (page == null || pageSize == null) {
                resp.status(400)
                return ErrorResponse("pageSize or page parameter is missing.")
            }
            val transactions = transactions.getTransactions(session.userId).getFromPage(pageSize, page)
            val accountIds = transactions.map { it.accountId }
            val accounts = accounts.getAllAccounts(session.userId).filter { accountIds.contains(it.id) }
            val output = LinkedList<AccountTransactions>()
            accounts.forEach {
                val account = it
                output.add(AccountTransactions(
                        account, transactions.filter { it.accountId == account.id }
                ))
            }
            return ListAccountTransactionsResponse(output)
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