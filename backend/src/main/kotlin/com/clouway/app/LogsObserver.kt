package com.clouway.app

import com.clouway.app.core.Observer
import org.apache.log4j.Logger

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */

class LogsObserver(private val logger: Logger) : Observer {
    override fun onRegister(email: String, username: String) {}

    override fun onLogin(username: String) {
        logger.info("$username has logged in.")
    }

    override fun onLogout(username: String) {
        logger.info("$username has logged out.")
    }
}