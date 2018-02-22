package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.Currency
import com.clouway.app.core.httpresponse.GetAccountsListResponseDto
import com.google.api.client.http.*
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.nio.charset.Charset

class GetAllAccountsTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getAllAccountsOfUserThatWasRegistered() {
        val sessionId = helper.registerUserAndGetSessionId("$primaryUrl/registration", "user", "password")
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
        // get all accounts of this user
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val accountsList = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountsListResponseDto::class.java
        ).content
        assertThat(accountsList.size, `is`(equalTo(1)))
        assertThat(accountsList.first().title, `is`(equalTo(accountTitle)))
        assertThat(accountsList.first().currency, `is`(equalTo(accountCurrency)))
    }
}
