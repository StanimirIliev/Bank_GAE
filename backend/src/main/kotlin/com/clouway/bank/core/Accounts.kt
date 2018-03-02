package com.clouway.bank.core

interface Accounts {

    /**
     * Registers account linked with userId in the DB
     * @param account the DTO to register
     * @return the id of the account or -1 it the registration was not successful
     */
    fun registerAccount(account: Account): Long

    /**
     * Updates the balance of specific account
     * @param accountId the id of the account which balance to update
     * @param userId the id of the user which request this update
     * @param amount the amount to add to the balance of the account
     * For withdraw amount should be less than zero
     * For deposit amount should be greater than zero
     * @return OperationResponse object
     * OperationResponse messages:
     * incorrect-id
     * low-balance
     * invalid-request
     * access-denied
     * successful
     */
    fun updateBalance(accountId: Long, userId: Long, amount: Float): OperationResponse

    /**
     * Gets all accounts that has not been deleted by userId
     * @param userId the id of the user
     * @return list with all accounts registered on this userId
     */
    fun getActiveAccounts(userId: Long): List<Account>

    /**
     * Gets all accounts including these that was deleted
     * @param userId the id of the user
     * @return list with all accounts registered on this userId
     */
    fun getAllAccounts(userId: Long): List<Account>

    /**
     * Removes account from the DB
     * @param userId the id of the user (Used for authorization)
     * @param accountId the id of the account
     * @return OperationResponse object
     * OperationResponse messages:
     * account-not-found
     * successful
     * error
     */
    fun removeAccount(accountId: Long, userId: Long): OperationResponse

    /**
     * Gets user account
     * @param userId the id of the user
     * @param accountId the id of the account
     * @return Account DTO or null if there was not match in the DB
     */
    fun getUserAccount(userId: Long, accountId: Long): Account?
}
