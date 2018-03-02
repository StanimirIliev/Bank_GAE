package e2e

import com.clouway.bank.AppBootstrap
import com.clouway.bank.adapter.http.accounts.dto.AccountsListResponse
import com.clouway.bank.core.Currency
import com.clouway.bank.core.User
import com.google.api.client.http.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import rules.E2ERule
import java.nio.charset.Charset

class GetAllAccountsTest {

    @Rule
    @JvmField
    val helper = E2ERule(AppBootstrap(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getAllAccountsOfUserThatWasRegistered() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // create account
        val accountTitle = "Fund for something"
        val accountCurrency = Currency.BGN
        val requestBody = """{"params":{"title":"$accountTitle","currency":"$accountCurrency"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        req.execute()
        // common all accounts of this user
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val accountsList = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                AccountsListResponse::class.java
        ).content
        assertThat(accountsList.size, `is`(equalTo(1)))
        assertThat(accountsList.first().title, `is`(equalTo(accountTitle)))
        assertThat(accountsList.first().currency, `is`(equalTo(accountCurrency)))
    }
}
