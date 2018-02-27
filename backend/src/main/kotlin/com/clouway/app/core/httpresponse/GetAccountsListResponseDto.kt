package com.clouway.app.core.httpresponse

import com.clouway.app.core.Account
import java.io.Serializable

data class GetAccountsListResponseDto(val content: List<Account>): Serializable