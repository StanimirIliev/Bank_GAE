package com.clouway.bank.adapter.event.listener

import com.clouway.bank.core.UserEventHandler

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class CompositeUserEventHandler(private vararg val userEventHandlers: UserEventHandler) : UserEventHandler {

    override fun onRegister(email: String, username: String) {
        userEventHandlers.forEach { it.onRegister(email, username) }
    }

    override fun onLogin(username: String) {
        userEventHandlers.forEach { it.onLogin(username) }
    }

    override fun onLogout(username: String) {
        userEventHandlers.forEach { it.onLogout(username) }
    }
}