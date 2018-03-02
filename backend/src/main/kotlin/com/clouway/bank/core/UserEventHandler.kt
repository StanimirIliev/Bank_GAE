package com.clouway.bank.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface UserEventHandler {
    fun onRegister(email: String, username: String)
    fun onLogin(username: String)
    fun onLogout(username: String)
}