package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.UserLogoutEvent
import com.clouway.eventbus.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class UserLogoutHandler(private val sender: SimpleEmailSender) : Handler<UserLogoutEvent> {
    override fun handle(event: UserLogoutEvent) {
        sender
                .setFrom("e.corp@bank.com")
                .setTo("stanimir.iliev@clouway.com")
                .setSubject("Someone just logged out of your app")
                .setContent("${event.username} just logged out from bank of E corp.", null)
                .send()
    }
}