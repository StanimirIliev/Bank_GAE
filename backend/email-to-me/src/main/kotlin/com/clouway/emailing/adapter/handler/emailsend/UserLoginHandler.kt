package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.UserLoginEvent
import com.clouway.eventbus.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserLoginHandler(private val sender: SimpleEmailSender) : Handler<UserLoginEvent> {
    override fun handle(event: UserLoginEvent) {
        sender
                .setFrom("e.corp@bank.com")
                .setTo("stanimir.iliev@clouway.com")
                .setSubject("Someone just logged into your app")
                .setContent("${event.username} just logged into bank of E corp.", null)
                .send()
    }
}