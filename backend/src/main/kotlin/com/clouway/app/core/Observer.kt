package com.clouway.app.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface Observer {
    fun onRegister(email: String, username: String)
    fun onLogin(username: String)
    fun onLogout(username: String)
}