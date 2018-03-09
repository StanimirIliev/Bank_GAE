package com.clouway.bank.adapter.event

import com.clouway.bank.core.Event

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

data class UserLoginEvent(val username: String) : Event {
    override fun getAttributes(): Map<String, String> {
        return mapOf("username" to username)
    }
}