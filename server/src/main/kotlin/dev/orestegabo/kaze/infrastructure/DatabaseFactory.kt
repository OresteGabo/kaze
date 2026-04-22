package dev.orestegabo.kaze.infrastructure

import com.zaxxer.hikari.HikariDataSource
import dev.orestegabo.kaze.auth.AppUsersTable
import dev.orestegabo.kaze.auth.AuthRefreshTokensTable
import dev.orestegabo.kaze.auth.UserAuthProvidersTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.slf4j.LoggerFactory
import javax.sql.DataSource

internal object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    private var hikariDataSource: HikariDataSource? = null
    private var exposedDatabase: Database? = null
    private val managedTables: List<Table> = listOf(
        AppUsersTable,
        UserAuthProvidersTable,
        AuthRefreshTokensTable,
    )

    val dataSource: DataSource
        get() = requireNotNull(hikariDataSource) {
            "DatabaseFactory has not been initialized yet"
        }

    fun init(config: DatabaseConfig): Database {
        exposedDatabase?.let { return it }

        val jdbcUrl = config.jdbcUrl
        if ("neon.tech" in jdbcUrl && "sslmode=" !in jdbcUrl.lowercase()) {
            logger.warn("DATABASE_URL appears to target Neon without sslmode. Add sslmode=require to avoid connection failures.")
        }

        return try {
            val source = config.createDataSource()
            source.connection.use { connection ->
                connection.prepareStatement("SELECT 1").use { statement ->
                    statement.execute()
                }
            }

            val database = Database.connect(source)
            hikariDataSource = source
            exposedDatabase = database

            logger.info(
                "DatabaseFactory connected using {} with managed tables: {}",
                jdbcUrl.substringBefore('?'),
                managedTables.joinToString { it.tableName },
            )

            database
        } catch (cause: Throwable) {
            logger.error(
                "Failed to connect to PostgreSQL. Check DATABASE_URL formatting, Neon reachability, and sslmode=require in the connection string.",
                cause,
            )
            throw cause
        }
    }
}

internal fun Application.initializeDatabaseFactory(config: DatabaseConfig) {
    DatabaseFactory.init(config)
}
