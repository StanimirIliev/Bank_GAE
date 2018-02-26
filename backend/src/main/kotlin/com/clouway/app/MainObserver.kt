package com.clouway.app

import com.clouway.app.core.Observer

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class MainObserver(private vararg val observers: Observer) : Observer {

    override fun onRegister(email: String, username: String) {
        observers.forEach { it.onRegister(email, username) }
    }

    override fun onLogin(username: String) {
        observers.forEach { it.onLogin(username) }
    }

    override fun onLogout(username: String) {
        observers.forEach { it.onLogout(username) }
    }
}