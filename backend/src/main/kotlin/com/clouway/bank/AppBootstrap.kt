package com.clouway.bank

import com.clouway.bank.adapter.event.listener.CompositeUserEventHandler
import com.clouway.bank.adapter.event.listener.DispatchUserEvents
import com.clouway.bank.adapter.event.listener.TransactionEventHandler
import com.clouway.bank.adapter.event.listener.UserEventLogger
import com.clouway.bank.adapter.gcp.datastore.PersistentAccounts
import com.clouway.bank.adapter.gcp.datastore.PersistentSessions
import com.clouway.bank.adapter.gcp.datastore.PersistentTransactions
import com.clouway.bank.adapter.gcp.datastore.PersistentUsers
import com.clouway.bank.adapter.gcp.memcache.CachedSessions
import com.clouway.bank.adapter.gcp.transaction.SafeAccountsProxy
import com.clouway.bank.adapter.gcp.transaction.SafeTransactionsProxy
import com.clouway.bank.adapter.http.accounts.*
import com.clouway.bank.adapter.http.common.*
import com.clouway.bank.adapter.http.transactions.GetTransactions
import com.clouway.bank.adapter.http.users.*
import com.clouway.bank.adapter.spark.transformer.JsonTransformer
import com.clouway.bank.adapter.validation.ValidationAccountsProxy
import com.clouway.bank.adapter.validation.ValidationUsersProxy
import com.clouway.bank.adapter.validator.CompositeValidator
import com.clouway.bank.adapter.validator.regex.RegexValidationRule
import com.clouway.eventdispatch.adapter.gcp.pubsub.PubsubDispatcher
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark.*
import java.io.File

class AppBootstrap {
    fun start() {
        val logger = Logger.getLogger("AppBootstrap")
        // config freemarker
        val config = Configuration(Configuration.VERSION_2_3_23)
        val file = File(AppBootstrap::class.java.getResource("freemarker/templates/").file)
        config.setDirectoryForTemplateLoading(file)
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false
        // create pubsub topic
        val pubsubDispatcher = PubsubDispatcher("bank-e-corp", "user")
        // init observer
        val mainObserver = CompositeUserEventHandler(
                DispatchUserEvents(pubsubDispatcher),
                UserEventLogger(logger)
        )
        val transactionListener = TransactionEventHandler(pubsubDispatcher)
        // init repositories
        val datastore = DatastoreServiceFactory.getDatastoreService()
        val sessionRepository = CachedSessions(
                PersistentSessions(datastore),
                MemcacheServiceFactory.getMemcacheService()
        )
        val transactionRepository = SafeTransactionsProxy(
                PersistentTransactions(datastore),
                datastore
        )
        val accountRepository = ValidationAccountsProxy(
                SafeAccountsProxy(
                        PersistentAccounts(datastore, transactionRepository),
                        datastore,
                        transactionRepository
                ),
                datastore
        )
        val userRepository = ValidationUsersProxy(
                PersistentUsers(datastore, mainObserver),
                datastore
        )
        // init validator
        val compositeValidator = CompositeValidator(
                RegexValidationRule(
                        "email",
                        "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
                        "Incorrect email.\n"
                ),
                RegexValidationRule(
                        "username",
                        "[a-zA-Z\\d]{4,15}",
                        "Incorrect username.\nShould be between 4 and 15 characters long " +
                                "and to not contain special symbols.\n"
                ),
                RegexValidationRule("password",
                        "^[a-zA-Z\\d]{6,30}\$",
                        "Incorrect password.\nShould be between 6 and 30 characters long, " +
                                "and to not contain special symbols."
                ))
        // init transformer
        val transformer = JsonTransformer()
        // config spark
        initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
        internalServerError { _, res ->
            res.redirect("/static/index/images/500-wallpaper.jpg")
        }

        get("/index", ShowIndexPage())
        post("/login", LoginUserHandlerRoute(userRepository, sessionRepository, mainObserver, config))
        get("/login", ShowLoginPage(config))
        get("/registration", ShowRegisterPage(config))
        post("/registration", RegistrationUserHandlerRoute(userRepository, sessionRepository, mainObserver, compositeValidator, config))
        get("/home", Secured(sessionRepository, ShowHomePage(), logger))
        get("/logout", Logout(sessionRepository, userRepository, mainObserver, logger), transformer)
        path("/v1") {
            path("/accounts") {
                get("", Secured(sessionRepository, GetAccounts(accountRepository), logger), transformer)
                get("/:id", Secured(sessionRepository, GetAccountDetails(accountRepository), logger), transformer)
                post("", Secured(sessionRepository, CreateAccount(accountRepository), logger), transformer)
                post("/:id/deposit", Secured(sessionRepository, Deposit(accountRepository, transactionListener),  logger), transformer)
                post("/:id/withdraw", Secured(sessionRepository, Withdraw(accountRepository, transactionListener), logger), transformer)
                delete("/:id", Secured(sessionRepository, RemoveAccount(accountRepository, logger), logger), transformer)
            }
            get("/activity", GetActivity(sessionRepository), transformer)
            get("/username", Secured(sessionRepository, GetUsername(userRepository), logger), transformer)
            get("/transactions/:param", Secured(sessionRepository, GetTransactions(transactionRepository,
                    accountRepository), logger), transformer)
        }
        path("/cron") {
            get("/remove-expired-sessions", RemoveExpiredSessions(sessionRepository, logger))
        }
        get("/*") { _, res -> res.redirect("/home") }
    }

}