package com.clouway.bank

import com.clouway.bank.adapter.email.sender.Sendgrid
import com.clouway.bank.adapter.eventhandler.user.CompositeUserEventHandler
import com.clouway.bank.adapter.eventhandler.user.UserEventLogger
import com.clouway.bank.adapter.gcp.datastore.PersistentAccounts
import com.clouway.bank.adapter.gcp.datastore.PersistentSessions
import com.clouway.bank.adapter.gcp.datastore.PersistentTransactions
import com.clouway.bank.adapter.gcp.datastore.PersistentUsers
import com.clouway.bank.adapter.gcp.memcache.CachedSessions
import com.clouway.bank.adapter.gcp.taskqueue.PushEventUserSender
import com.clouway.bank.adapter.gcp.transaction.SafeAccountsProxy
import com.clouway.bank.adapter.gcp.transaction.SafeTransactionsProxy
import com.clouway.bank.adapter.gcp.transaction.SafeUsersProxy
import com.clouway.bank.adapter.http.accounts.*
import com.clouway.bank.adapter.http.common.*
import com.clouway.bank.adapter.http.transactions.GetTransactions
import com.clouway.bank.adapter.http.users.*
import com.clouway.bank.adapter.spark.transformer.JsonTransformer
import com.clouway.bank.adapter.validation.ValidationAccountsProxy
import com.clouway.bank.adapter.validation.ValidationUsersProxy
import com.clouway.bank.adapter.validator.CompositeValidator
import com.clouway.bank.adapter.validator.regex.RegexValidationRule
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.memcache.MemcacheServiceFactory
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.apache.log4j.Logger
import spark.Spark.*
import java.io.File
import java.nio.charset.Charset

class AppBootstrap {
    fun start() {
        val sendgridApiKey = AppBootstrap::class.java.getResourceAsStream("sendgrid.env")
                .reader(Charset.defaultCharset())
                .readText()

        val logger = Logger.getLogger("AppBootstrap")
        val config = Configuration(Configuration.VERSION_2_3_23)
        val file = File(AppBootstrap::class.java.getResource("freemarker/templates/").file)
        config.setDirectoryForTemplateLoading(file)
        config.defaultEncoding = "UTF-8"
        config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        config.logTemplateExceptions = false

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
        val emailSender = Sendgrid("https://api.sendgrid.com", sendgridApiKey)
        val mainObserver = CompositeUserEventHandler(
                PushEventUserSender(),
                UserEventLogger(logger)
        )
        val userRepository = ValidationUsersProxy(
                SafeUsersProxy(
                        PersistentUsers(datastore, mainObserver),
                        datastore,
                        mainObserver
                ),
                datastore
        )
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

        initExceptionHandler { e -> logger.fatal("Unable to start the server", e) }
        internalServerError { _, res ->
            res.redirect("/static/index/images/500-wallpaper.jpg")
        }

        val transformer = JsonTransformer()
        post("/tasks/emailSender", EmailSender(emailSender, logger))
        get("/index", ShowIndexPage())
        post("/login", LoginUserHandler(userRepository, sessionRepository, mainObserver, config))
        get("/login", ShowLoginPage(config))
        get("/registration", ShowRegisterPage(config))
        post("/registration", RegisterUserHandler(userRepository, sessionRepository, mainObserver, compositeValidator, config))
        get("/home", Secured(sessionRepository, ShowHomePage(), logger))
        get("/logout", Logout(sessionRepository, userRepository, mainObserver, logger), transformer)
        path("/v1") {
            path("/accounts") {
                get("", Secured(sessionRepository, GetAccounts(accountRepository), logger), transformer)
                get("/:id", Secured(sessionRepository, GetAccountDetails(accountRepository), logger), transformer)
                post("", Secured(sessionRepository, CreateAccount(accountRepository), logger), transformer)
                post("/:id/deposit", Secured(sessionRepository, Deposit(accountRepository), logger), transformer)
                post("/:id/withdraw", Secured(sessionRepository, Withdraw(accountRepository), logger), transformer)
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
