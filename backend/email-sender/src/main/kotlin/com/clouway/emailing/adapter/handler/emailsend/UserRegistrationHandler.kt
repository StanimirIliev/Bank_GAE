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
                .setTo(event.email)
                .setSubject("Registration in bank of E corp")
                .setContent("Hello ${event.username}. Welcome to bank of E corp. Thank you for choosing us.", null)
                .send()
    }
}