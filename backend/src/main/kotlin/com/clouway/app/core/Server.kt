package com.clouway.app.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface Server {
    /**
     * Start the server
     */
    fun start()

    /**
     * Wait until the server starts
     */
    fun awaitInitialization()

    /**
     * Stop the server
     */
    fun stop()
}