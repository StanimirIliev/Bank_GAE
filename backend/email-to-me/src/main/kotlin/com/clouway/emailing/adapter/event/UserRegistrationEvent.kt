package com.clouway.emailing.adapter.event

import com.clouway.emailing.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class UserRegistrationEvent(val email: String, val username: String) : Event {
    override fun getAttributes(): Map<String, String> {
        return mapOf("email" to email, "username" to username)
    }
}