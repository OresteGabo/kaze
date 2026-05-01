package dev.orestegabo.kaze

import dev.orestegabo.kaze.api.configureHttp
import dev.orestegabo.kaze.api.registerApiRoutes
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.loadJwtConfig
import dev.orestegabo.kaze.application.ServerDependencies
import dev.orestegabo.kaze.application.validateProductionSafety
import dev.orestegabo.kaze.di.serverModule
import dev.orestegabo.kaze.infrastructure.initializeDatabaseFactory
import dev.orestegabo.kaze.infrastructure.initializeDatabaseSeed
import dev.orestegabo.kaze.infrastructure.initializeDatabaseSchema
import dev.orestegabo.kaze.infrastructure.loadDatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toIntOrNull() ?: SERVER_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val databaseConfig = loadDatabaseConfig()
    val jwtConfig = loadJwtConfig()
    validateProductionSafety(databaseConfig, jwtConfig)
    initializeDatabaseFactory(databaseConfig)
    initializeDatabaseSchema(databaseConfig)
    initializeDatabaseSeed(databaseConfig)
    install(Koin) {
        slf4jLogger()
        modules(serverModule(databaseConfig, jwtConfig))
    }
    val dependencies = get<ServerDependencies>()
    val authService = get<AuthService>()
    configureHttp(authService)
    registerApiRoutes(dependencies, authService)
    environment.log.info("Kaze database configured for {}", databaseConfig.jdbcUrl)
}
