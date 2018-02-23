package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.User
import com.clouway.app.core.httpresponse.GetAccountsListResponseDto
import com.google.api.client.http.*
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test


class RemoveAccountTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun removeAccountAsAuthorizedUser() {
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user", "password")
        )
        // create new account
        val requestBody = """{"params":{"title":"Fund for something","currency":"BGN"}}"""
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
        var accountsList = gson.fromJson(
                resp.content.reader().readText(),
                GetAccountsListResponseDto::class.java
        ).content
        val id = accountsList.first().id// there is only one account in the datastore no need to search for it
        // delete created account
        req = requestFactory.buildDeleteRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionId)
        req.execute()
        // assert that there is no account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        accountsList = gson.fromJson(
                resp.content.reader().readText(),
                GetAccountsListResponseDto::class.java
        ).content
        assertThat(accountsList, `is`(emptyList()))
    }

    @Test
    fun tryToRemoveAccountAsUnauthorizedUser() {
        val sessionIdOfUser1 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", "user1", "password")
        )
        val sessionIdOfUser2 = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("sometwo@example.com", "user2", "password")
        )
        // Create account authorized by user1
        val requestBody = """{"params":{"title":"Fund for something","currency":"BGN"}}"""
        req = requestFactory.buildPostRequest(
                GenericUrl("$primaryUrl/v1/accounts"),
                ByteArrayContent.fromString("application/json", requestBody)
        )
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        req.execute()
        // get id of the new account
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        var accountsList = gson.fromJson(
                resp.content.reader().readText(),
                GetAccountsListResponseDto::class.java
        ).content
        val id = accountsList.first().id// there is only one account in the datastore no need to search for it
        // try to delete account authorized by user1 as user2
        req = requestFactory.buildDeleteRequest(GenericUrl("$primaryUrl/v1/accounts/$id"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser2)
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        // assert that the response code is 404
        assertThat(resp.statusCode, `is`(equalTo(404)))
        // assert that the account is not deleted
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/accounts"))
        req.headers = HttpHeaders().setCookie(sessionIdOfUser1)
        resp = req.execute()
        accountsList = gson.fromJson(
                resp.content.reader().readText(),
                GetAccountsListResponseDto::class.java
        ).content
        assertThat(accountsList, `is`(not(emptyList())))
    }
}