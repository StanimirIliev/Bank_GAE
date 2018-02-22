package com.clouway.app.core.httpresponse

import com.clouway.app.core.Account
import com.clouway.app.core.Transaction

data class AccountTransactions(val account: Account, val transactions: List<Transaction>)