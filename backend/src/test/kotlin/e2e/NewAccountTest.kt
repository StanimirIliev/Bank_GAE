package e2e

import com.clouway.bank.AppBootstrap
import com.clouway.bank.adapter.http.accounts.dto.AccountDetailsResponse
import com.clouway.bank.adapter.http.accounts.dto.AccountsListResponse
import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.adapter.http.accounts.dto.MessageResponse
import com.clouway.bank.core.Currency
import com.clouway.bank.core.User
import com.google.api.client.http.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import rules.E2ERule
import java.nio.charset.Charset

class NewAccountTest {

    @Rule
    @JvmField
    val helper = E2ERule(AppBootstrap(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun addNewAccount() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // assert that there is no accounts
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        var accountsList = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                AccountsListResponse::class.java
        ).content
        assertThat(accountsList.size, `is`(equalTo(0)))
        // create new account
        val accountTitle = "Some fund"
        val accountCurrency = Currency.BGN
        val requestBody = """{"params":{"title":"$accountTitle","currency":"$accountCurrency"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(201)))
        val message = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                MessageResponse::class.java
        ).message
        assertThat(message, `is`(equalTo("New account opened successful.")))
        // common id of the new account and assert that there is one account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        accountsList = gson.fromJson(
                resp.content.reader().readText(),
                AccountsListResponse::class.java
        ).content
        assertThat(accountsList.size, `is`(equalTo(1)))
        val id = accountsList.first().id
        // assert account details
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                AccountDetailsResponse::class.java
        ).account
        MatcherAssert.assertThat(account.title, `is`(equalTo(accountTitle)))
        MatcherAssert.assertThat(account.currency, `is`(equalTo(accountCurrency)))
    }

    @Test
    fun tryToAddTwoAccountsWithTheSameName() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // create new account
        val requestBody = """{"params":{"title":"Some fund","currency":"BGN"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        req.execute()
        // try to add the same account(duplicating name of accounts for one user)
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(400)))
        val error = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                ErrorResponse::class.java
        ).error
        assertThat(error, `is`(equalTo("You have already account with such a title.")))
    }

}
