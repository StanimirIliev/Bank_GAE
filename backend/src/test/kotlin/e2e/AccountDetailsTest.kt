package e2e

import com.clouway.bank.AppBootstrap
import com.clouway.bank.adapter.http.accounts.dto.AccountDetailsResponse
import com.clouway.bank.adapter.http.accounts.dto.AccountsListResponse
import com.clouway.bank.adapter.http.accounts.dto.ErrorResponse
import com.clouway.bank.core.Currency
import com.clouway.bank.core.User
import com.google.api.client.http.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import rules.E2ERule
import java.nio.charset.Charset

class AccountDetailsTest {

    @Rule
    @JvmField
    val helper = E2ERule(AppBootstrap(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getAccountDetailsAsAuthorizedUser() {
        val accountTitle = "Fund for something"
        val accountCurrency = Currency.BGN

        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // create new account
        val requestBody = """{"params":{"title":"$accountTitle","currency":"$accountCurrency"}}"""
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
        // assert account details
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                AccountDetailsResponse::class.java
        ).account
        assertThat(account.title, `is`(equalTo(accountTitle)))
        assertThat(account.currency, `is`(equalTo(accountCurrency)))
    }

    @Test
    fun tryToGetAccountDetailsAsUnauthorizedUser() {
        val sessionIdOfUser1 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user1", "password")
        )
        val sessionIdOfUser2 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("sometwo@example.com", "user2", "password")
        )
        // create new account authorized by user1
        val requestBody = """{"params":{"title":"Some fund","currency":"BGN"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        req.execute()
        // common id of the new account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        val accountsList = gson.fromJson(
                resp.content.reader().readText(),
                AccountsListResponse::class.java
        ).content
        val id = accountsList.first().id// there is only one account in the datastore no need to search for it
        // try to common details of the account authorized by user1 as user2
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser2)
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(404)))
        val error = gson.fromJson(resp.content.reader(Charset.defaultCharset()), ErrorResponse::class.java).error
        assertThat(error, `is`(equalTo("Account not found.")))
    }
}
