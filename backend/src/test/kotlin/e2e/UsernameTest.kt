package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.httpresponse.GetUsernameResponseDto
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpResponse
import helpers.E2EHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import java.nio.charset.Charset


class UsernameTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getUsernameAsAuthenticatedUser() {
        val username = "user"
        val sessionId = helper.registerUserAndGetSessionId("$primaryUrl/registration", username, "password")
        // get username
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/username"))
        req.headers = HttpHeaders().setCookie(sessionId)
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        val fetchedUsername = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetUsernameResponseDto::class.java
        ).username
        assertThat(fetchedUsername, `is`(equalTo(username)))
    }
}
