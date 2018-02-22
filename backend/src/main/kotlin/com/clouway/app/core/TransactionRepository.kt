package com.clouway.app.core

interface TransactionRepository {

    /**
     * Registers transaction in the DB
     * @param transaction the DTO which would be registered
     * @return true if the operation was successful, false if problem occurs
     */
    fun registerTransaction(transaction: Transaction): Boolean

    /**
     * Gets list with all transactions made of specific userId
     * @param userId the id of the user which was made these transactions
     * @return sorted list with the transactions of the specific user (ascending order)
     */
    fun getTransactions(userId: Long): List<Transaction>
}