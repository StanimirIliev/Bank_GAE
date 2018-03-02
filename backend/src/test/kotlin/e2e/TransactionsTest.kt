package e2e

import com.clouway.bank.AppBootstrap
import com.clouway.bank.adapter.http.accounts.dto.AccountsListResponse
import com.clouway.bank.adapter.http.transactions.dto.ListAccountTransactionsResponse
import com.clouway.bank.adapter.http.transactions.dto.TransactionCountResponse
import com.clouway.bank.core.User
import com.google.api.client.http.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import rules.E2ERule
import java.nio.charset.Charset

class TransactionsTest {

    @Rule
    @JvmField
    val helper = E2ERule(AppBootstrap(), true)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getTransactionsCount() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // assert that there is no transactions yet
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/transactions/count"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        var transactionsCount = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                TransactionCountResponse::class.java
        ).transactionsCount
        assertThat(transactionsCount, `is`(equalTo(0)))
        // register transaction
        registerTransaction(sessionId)
        // check transactions count again
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/transactions/count"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        transactionsCount = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                TransactionCountResponse::class.java
        ).transactionsCount
        assertThat(transactionsCount, `is`(equalTo(1)))
    }

    @Test
    fun getTransactionsFromSpecificPage() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // register transaction
        registerTransaction(sessionId)
        // common 1 transaction from page 1
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/transactions/1?pageSize=1"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val listAccountTransactions = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                ListAccountTransactionsResponse::class.java
        ).list
        assertThat(listAccountTransactions.first().transactions.size, `is`(equalTo(1)))
    }

    private fun registerTransaction(sessionId: String) {
        // first create account
        var requestBody = """{"params":{"title":"Some fund","currency":"BGN"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        req.execute()
        // common id of the new account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val accountsList = gson.fromJson(
                resp.content.reader().readText(),
                AccountsListResponse::class.java
        ).content
        val id = accountsList.first().id// there is only one account in the datastore no need to search for it
        // then create transaction
        requestBody = """{"params":{"value":50}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/deposit"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
    }
}