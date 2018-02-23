package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.User
import com.clouway.app.core.httpresponse.GetAccountResponseDto
import com.clouway.app.core.httpresponse.GetAccountsListResponseDto
import com.clouway.app.core.httpresponse.GetMessageResponseDto
import com.clouway.app.core.httpresponse.HttpError
import com.google.api.client.http.*
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.nio.charset.Charset

class WithdrawTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun executeWithdrawAsAuthorizedUser() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        val id = createAccountAndDeposit50Bgn(sessionId)
        // execute withdraw
        val requestBody = """{"params":{"value":30}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/withdraw"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(201)))
        val message = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetMessageResponseDto::class.java
        ).message
        assertThat(message, `is`(equalTo("Withdraw successful.")))
        // assert that the balance has been decremented
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(20f)))
    }

    @Test
    fun tryToExecuteWithdrawBiggerThanBalance() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        val id = createAccountAndDeposit50Bgn(sessionId)
        // try to execute withdraw bigger than the balance
        val requestBody = """{"params":{"value":80}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/withdraw"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(400)))
        val error = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                HttpError::class.java
        ).error
        assertThat(error, `is`(equalTo("Cannot execute this withdraw. Not enough balance.")))
        // assert that the balance has not been changed
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        val account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(50f)))
    }

    @Test
    fun tryToExecuteWithdrawAsUnauthorizedUser() {
        val sessionIdOfUser1 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user1", "password")
        )
        val sessionIdOfUser2 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("sometwo@example.com", "user2", "password")
        )
        // create new account authorized by user1
        val id = createAccountAndDeposit50Bgn(sessionIdOfUser1)
        // assert that balance of the account of user1 is 50
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        var account = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetAccountResponseDto::class.java
        ).account
        assertThat(account.balance, `is`(equalTo(50f)))
        // try to execute withdraw to account authorized of user1 as user2
        val requestBody = """{"params":{"value":10}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/withdraw"),
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
        assertThat(account.balance, `is`(equalTo(50f)))
    }

    /**
     * @return the id of the new created account
     */
    private fun createAccountAndDeposit50Bgn(sessionId: String): Long {
        // create account
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
        // deposit 50 BGN
        requestBody = """{"params":{"value":50}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts/$id/deposit"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        return id
    }
}
