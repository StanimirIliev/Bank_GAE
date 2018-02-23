package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.User
import com.clouway.app.core.httpresponse.GetAccountResponseDto
import com.clouway.app.core.httpresponse.GetAccountsListResponseDto
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.google.api.client.http.*
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.nio.charset.Charset

class DepositTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun executeDepositAsAuthorizedUser() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        val id = createAccount(sessionId)
        // assert that the account balance is 0
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        var account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(0f)))
        // make a deposit
        val requestBody = """{"params":{"value":50}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/deposit"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(201)))
        val message = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetMessageResponseDto::class.java
        ).message
        assertThat(message, `is`(equalTo("Deposit successful.")))
        // assert that the balance is incremented
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(50f)))
    }

    @Test
    fun tryToExecuteDepositAsUnauthorizedUser() {
        val sessionIdOfUser1 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user1", "password")
        )
        val sessionIdOfUser2 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("sometwo@example.com", "user2", "password")
        )
        // create new account authorized by user1
        val id = createAccount(sessionIdOfUser1)
        // assert that balance of the account of user1 is 0
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        var account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(0f)))
        // try to execute deposit to account authorized of user1 as user2
        val requestBody = """{"params":{"value":50}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/deposit"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionIdOfUser2)
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(401)))
        // assertThat the balance of the account of user1 is not changed
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(0f)))
    }

    /**
     * @return the id of the new created account
     */
    private fun createAccount(sessionId: String): Long {
        val requestBody = """{"params":{"title":"Some fund","currency":"BGN"}}"""
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
        return accountsList.first().id// there is only one account in the datastore no need to search for it
    }
}
