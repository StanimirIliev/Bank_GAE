package com.clouway.bank.core

interface Users {
    /**
     * Registers users in the DB
     * @param user credentials of the user
     * @return the id of the user if the operation was successful,
     * -1 if there is already registered user with that username
     */
    fun registerUser(user: User): Long

    /**
     * Authenticates the user with username and password
     * @param username the username of the user
     * @param password the password of the user
     * @return true if there is match with these parameters in the DB, false if there is not
     */
    fun authenticateByUsername(username: String, password: String): Boolean

    /**
     * Authenticate the user with email and password
     * @param email the email of the user
     * @param password the password of the user
     * @return true if there is match with these parameters in the DB, false if there is not
     */
    fun authenticateByEmail(email: String, password: String): Boolean


    /**
     * Gets username by id
     * @param id the id of the user
     * @return the username of this user or null if there is no match in the DB
     */
    fun getUsername(id: Long): String?

    /**
     * Gets id by usernameOrEmail
     * @param usernameOrEmail the usernameOrEmail of the user
     * @return the id of the user or -1 it there is no match in the DB
     */
    fun getUserId(usernameOrEmail: String): Long
}
