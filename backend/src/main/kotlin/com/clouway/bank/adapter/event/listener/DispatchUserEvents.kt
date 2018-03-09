package com.clouway.bank.adapter.event.listener

import com.clouway.bank.adapter.event.UserLoginEvent
import com.clouway.bank.adapter.event.UserLogoutEvent
import com.clouway.bank.adapter.event.UserRegistrationEvent
import com.clouway.bank.core.EventDispatcher
import com.clouway.bank.core.UserEventHandler


/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class DispatchUserEvents(private val eventDispatcher: EventDispatcher) : UserEventHandler {

    override fun onRegister(email: String, username: String) {
        eventDispatcher.publishEvent(UserRegistrationEvent(email, username))
    }

    override fun onLogin(username: String) {
        eventDispatcher.publishEvent(UserLoginEvent(username))
    }

    override fun onLogout(username: String) {
        eventDispatcher.publishEvent(UserLogoutEvent(username))
    }
}