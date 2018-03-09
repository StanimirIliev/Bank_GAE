package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.core.Event
import com.clouway.emailing.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserLoginHandler(private val sender: SimpleEmailSender) : Handler {
    override fun handle(event: Event) {
        val username = event.getAttributes()["username"]
        sender
                .setFrom("e.corp@bank.com")
                .setTo("stanimir.iliev.21@abv.bg")
                .setSubject("Someone just logged into your app")
                .setContent("$username just logged into bank of E corp.", null)
                .send()
    }
}