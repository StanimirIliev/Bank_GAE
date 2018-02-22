package e2e

import com.clouway.app.ConfiguredServer
import com.clouway.app.core.httpresponse.GetActivityResponseDto
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


class ActivityTest {

    @Rule
    @JvmField
    val helper = E2EHelper(ConfiguredServer(), false)

    private val primaryUrl = helper.primaryUrl
    private val gson = helper.gson
    private val requestFactory = helper.requestFactory
    private lateinit var req: HttpRequest
    private lateinit var resp: HttpResponse

    @Test
    fun getActiveUsersCount() {
        // assert that there is no active users
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/activity"))
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        var activity = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetActivityResponseDto::class.java
        ).activity
        assertThat(activity, `is`(equalTo(0)))
        // register user and check again for activity
        val sessionId = helper.registerUserAndGetSessionId("$primaryUrl/registration", "user", "password")
        resp = req.execute()
        assertThat(resp.statusCode, `is`(equalTo(200)))
        activity = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetActivityResponseDto::class.java
        ).activity
        assertThat(activity, `is`(equalTo(1)))
        // log out user
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/logout"))
        req.headers = HttpHeaders().setCookie(sessionId)
        req.followRedirects = false
        req.throwExceptionOnExecuteError = false
        resp = req.execute()
        // check activity again
        req = requestFactory.buildGetRequest(GenericUrl("$primaryUrl/v1/activity"))
        resp = req.execute()
        activity = gson.fromJson(
                resp.content.reader(Charset.defaultCharset()),
                GetActivityResponseDto::class.java
        ).activity
        assertThat(activity, `is`(equalTo(0)))
    }
}
