package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.User
import com.clouway.app.core.httpresponse.GetAccountsListResponseDto
import com.clouway.app.core.httpresponse.GetListAccountTransactionsResponseDto
import com.clouway.app.core.httpresponse.GetTransactionsCountResponseDto
import com.google.api.client.http.*
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.nio.charset.Charset

class TransactionsTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

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
                GetTransactionsCountResponseDto::class.java
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
                GetTransactionsCountResponseDto::class.java
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
        // get 1 transaction from page 1
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/transactions/1?pageSize=1"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val listAccountTransactions = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetListAccountTransactionsResponseDto::class.java
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
        // get id of the new account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val accountsList = gson.fromJson(
                resp.content.reader().readText(),
                GetAccountsListResponseDto::class.java
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