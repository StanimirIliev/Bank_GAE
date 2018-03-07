package com.clouway.emailing.adapter.handler.emailsend

import com.clouway.email.sender.core.SimpleEmailSender
import com.clouway.emailing.adapter.event.TransactionEvent
import com.clouway.eventbus.core.Handler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class TransactionHandler(private val sender: SimpleEmailSender) : Handler<TransactionEvent> {
    override fun handle(event: TransactionEvent) {
        sender
                .setTo("stanimir.iliev@clouway.com")
                .setFrom("e.corp@bank.com")
                .setSubject("Someone made a ${event.operation} in your bank")
                .setContent("User with ID ${event.userId} made a ${event.operation} " +
                        "to its account with id ${event.accountId} worth ${event.amount}", null)
                .send()
    }
}