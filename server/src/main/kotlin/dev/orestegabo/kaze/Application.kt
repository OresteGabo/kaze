package dev.orestegabo.kaze

import dev.orestegabo.kaze.api.configureHttp
import dev.orestegabo.kaze.api.registerApiRoutes
import dev.orestegabo.kaze.application.ServerDependencies
import dev.orestegabo.kaze.di.serverModule
import dev.orestegabo.kaze.infrastructure.initializeDatabaseSchema
import dev.orestegabo.kaze.infrastructure.loadDatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val databaseConfig = loadDatabaseConfig()
    initializeDatabaseSchema(databaseConfig)
    install(Koin) {
        slf4jLogger()
        modules(serverModule)
    }
    val dependencies = get<ServerDependencies>()
    configureHttp()
    registerApiRoutes(dependencies)
    environment.log.info("Kaze database configured for {}", databaseConfig.jdbcUrl)
}
