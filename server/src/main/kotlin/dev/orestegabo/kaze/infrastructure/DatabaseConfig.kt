package dev.orestegabo.kaze.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application

internal data class DatabaseConfig(
    val driver: String,
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
    val maximumPoolSize: Int,
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$host:$port/$name"
}

internal fun Application.loadDatabaseConfig(): DatabaseConfig =
    DatabaseConfig(
        driver = environment.config.propertyOrNull("kaze.database.driver")?.getString() ?: "org.postgresql.Driver",
        host = environment.config.propertyOrNull("kaze.database.host")?.getString() ?: "localhost",
        port = environment.config.propertyOrNull("kaze.database.port")?.getString()?.toInt() ?: 5432,
        name = environment.config.propertyOrNull("kaze.database.name")?.getString() ?: "kaze",
        user = environment.config.propertyOrNull("kaze.database.user")?.getString() ?: "postgres",
        password = environment.config.propertyOrNull("kaze.database.password")?.getString() ?: "Muhirehonore@1*",
        maximumPoolSize = environment.config.propertyOrNull("kaze.database.maximumPoolSize")?.getString()?.toInt() ?: 10,
    )

internal fun DatabaseConfig.createDataSource(): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = this@createDataSource.jdbcUrl
            username = user
            password = this@createDataSource.password
            maximumPoolSize = this@createDataSource.maximumPoolSize
            isAutoCommit = true
            validate()
        },
    )
