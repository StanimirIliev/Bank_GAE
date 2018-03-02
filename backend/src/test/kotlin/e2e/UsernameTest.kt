package e2e

import com.clouway.bank.AppBootstrap
import com.clouway.bank.adapter.http.users.dto.UsernameResponse
import com.clouway.bank.core.User
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpResponse
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import rules.E2ERule
import java.nio.charset.Charset


class UsernameTest {

    @Rule
    @JvmField
    val helper = E2ERule(AppBootstrap(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getUsernameAsAuthenticatedUser() {
        val username = "user"
        val sessionId = helper.registerUserAndGetSessionId(
                "$primaryUrl/registration",
                User("someone@example.com", username, "password")
        )
        // common username
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/username"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val fetchedUsername = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                UsernameResponse::class.java
        ).username
        assertThat(fetchedUsername, `is`(equalTo(username)))
    }
}
