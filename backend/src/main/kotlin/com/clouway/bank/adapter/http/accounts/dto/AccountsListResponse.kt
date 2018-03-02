package com.clouway.bank.adapter.http.accounts.dto

import com.clouway.bank.core.Account
import java.io.Serializable

data class AccountsListResponse(val content: List<Account>): Serializable