package com.clouway.bank.adapter.http.transactions.dto

import com.clouway.bank.core.Account
import com.clouway.bank.core.Transaction

data class AccountTransactions(val account: Account, val transactions: List<Transaction>)