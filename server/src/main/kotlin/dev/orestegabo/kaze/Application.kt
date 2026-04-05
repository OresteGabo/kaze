package dev.orestegabo.kaze

import dev.orestegabo.kaze.api.configureHttp
import dev.orestegabo.kaze.api.registerApiRoutes
import dev.orestegabo.kaze.application.createServerDependencies
import dev.orestegabo.kaze.infrastructure.loadDatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val databaseConfig = loadDatabaseConfig()
    val dependencies = createServerDependencies()
    configureHttp()
    registerApiRoutes(dependencies)
    environment.log.info("Kaze database configured for {}", databaseConfig.jdbcUrl)
}
