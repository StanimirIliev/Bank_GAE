package com.clouway.bank

import spark.servlet.SparkApplication

class Main : SparkApplication {
    override fun init() {
        AppBootstrap().start()
    }
}