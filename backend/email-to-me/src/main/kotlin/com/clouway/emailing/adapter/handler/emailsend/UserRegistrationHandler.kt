package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.core.Event
import com.clouway.emailing.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserRegistrationHandler(private val sender: SimpleEmailSender) : Handler {
    override fun handle(event: Event) {
        val email = event.getAttributes()["email"]!!
        val username = event.getAttributes()["username"]
        sender
                .setFrom("e.corp@bank.com")
                .setTo(email)
                .setSubject("Registration in bank of E corp")
                .setContent("Hello $username. Welcome to bank of E corp. Thank you for choosing us.", null)
                .send()
    }
}