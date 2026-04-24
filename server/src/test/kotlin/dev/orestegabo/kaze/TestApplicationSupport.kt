package dev.orestegabo.kaze

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

internal fun kazeTestApplication(
    vararg extraConfig: Pair<String, String>,
    block: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    val configEntries = buildList {
        add(
            "kaze.database.url" to (
                System.getenv("DATABASE_URL")
                    ?: "jdbc:postgresql://localhost:5432/kaze"
                ),
        )
        add(
            "kaze.database.user" to (
                System.getenv("DATABASE_USER")
                    ?: System.getenv("KAZE_DB_USER")
                    ?: "postgres"
                ),
        )
        add(
            "kaze.database.password" to (
                System.getenv("DATABASE_PASSWORD")
                    ?: System.getenv("KAZE_DB_PASSWORD")
                    ?: "postgres"
                ),
        )
        add("kaze.database.schema.mode" to "create-drop")
        add("kaze.database.seed.mode" to "dev")
        addAll(extraConfig)
    }

    environment {
        config = MapApplicationConfig(*configEntries.toTypedArray())
    }
    application { module() }
    block()
}
