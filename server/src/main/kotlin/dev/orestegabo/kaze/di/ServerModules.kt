package dev.orestegabo.kaze.di

import dev.orestegabo.kaze.auth.AuthRepository
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.AppleOAuthProvider
import dev.orestegabo.kaze.auth.ExternalTokenVerifier
import dev.orestegabo.kaze.auth.FacebookOAuthProvider
import dev.orestegabo.kaze.auth.GoogleOAuthProvider
import dev.orestegabo.kaze.auth.JdbcAuthRepository
import dev.orestegabo.kaze.auth.JwtConfig
import dev.orestegabo.kaze.auth.OAuthStateFactory
import dev.orestegabo.kaze.auth.SocialOAuthProvider
import dev.orestegabo.kaze.auth.SocialOAuthProviders
import dev.orestegabo.kaze.application.AssistantService
import dev.orestegabo.kaze.application.ExperienceQueryService
import dev.orestegabo.kaze.application.GuestStayService
import dev.orestegabo.kaze.application.HotelQueryService
import dev.orestegabo.kaze.application.MapQueryService
import dev.orestegabo.kaze.application.ServerDependencies
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.infrastructure.AmenityKnowledgeRepository
import dev.orestegabo.kaze.infrastructure.GuestRepository
import dev.orestegabo.kaze.infrastructure.InMemoryExperienceRepository
import dev.orestegabo.kaze.infrastructure.InMemoryHotelRepository
import dev.orestegabo.kaze.infrastructure.InMemoryMapRepository
import dev.orestegabo.kaze.infrastructure.InMemoryStayRepository
import dev.orestegabo.kaze.infrastructure.DatabaseConfig
import dev.orestegabo.kaze.infrastructure.createDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import javax.sql.DataSource

internal fun serverModule(
    databaseConfig: DatabaseConfig,
    jwtConfig: JwtConfig,
) = module {
    single { databaseConfig }
    single { jwtConfig }
    single<DataSource> { get<DatabaseConfig>().createDataSource() }
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get())
            }
        }
    }

    single<AuthRepository> { JdbcAuthRepository(get()) }
    single { ExternalTokenVerifier() }
    single { OAuthStateFactory() }
    single<Set<SocialOAuthProvider>> {
        setOf(
            GoogleOAuthProvider(get<JwtConfig>().socialAuth.google, get(), get()),
            AppleOAuthProvider(get<JwtConfig>().socialAuth.apple, get(), get()),
            FacebookOAuthProvider(get<JwtConfig>().socialAuth.facebook, get(), get()),
        )
    }
    single { SocialOAuthProviders(get()) }
    single {
        AuthService(
            repositoryProvider = { get<AuthRepository>() },
            jwtConfig = get(),
            tokenVerifier = get(),
            oauthStateFactory = get(),
            socialProviders = get(),
        )
    }

    single<HotelRepository> { InMemoryHotelRepository() }
    single<ExperienceRepository> { InMemoryExperienceRepository() }
    single<MapRepository> { InMemoryMapRepository() }
    single { GuestRepository() }
    single { InMemoryStayRepository() }
    single { AmenityKnowledgeRepository() }

    single { HotelQueryService(get()) }
    single { GuestStayService(get(), get()) }
    single { ExperienceQueryService(get()) }
    single { MapQueryService(get()) }
    single { AssistantService(get()) }

    single {
        ServerDependencies(
            hotelService = get(),
            guestStayService = get(),
            experienceService = get(),
            mapService = get(),
            assistantService = get(),
        )
    }
}
