package com.clouway.app

import spark.servlet.SparkApplication

class AppBootstrap : SparkApplication {
    override fun init() {
        ConfiguredServer().start()
    }
}