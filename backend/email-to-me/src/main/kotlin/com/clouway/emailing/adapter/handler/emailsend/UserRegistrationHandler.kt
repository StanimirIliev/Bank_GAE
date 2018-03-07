package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.UserRegistrationEvent
import com.clouway.eventbus.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserRegistrationHandler(private val sender: SimpleEmailSender) : Handler<UserRegistrationEvent> {
    override fun handle(event: UserRegistrationEvent) {
        sender
                .setFrom("e.corp@bank.com")
                .setTo("stanimir.iliev@clouway.com")
                .setSubject("Someone registered in your app")
                .setContent("Hello Stanimir, ${event.username} with email ${event.email} just registered in " +
                        "Bank of E corp", null)
                .send()
    }
}