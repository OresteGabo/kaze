package dev.orestegabo.kaze.di

import dev.orestegabo.kaze.auth.AuthRepository
import dev.orestegabo.kaze.auth.AuthService
import dev.orestegabo.kaze.auth.JdbcAuthRepository
import dev.orestegabo.kaze.auth.JwtConfig
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
import org.koin.dsl.module
import javax.sql.DataSource

internal fun serverModule(
    databaseConfig: DatabaseConfig,
    jwtConfig: JwtConfig,
) = module {
    single { databaseConfig }
    single { jwtConfig }
    single<DataSource> { get<DatabaseConfig>().createDataSource() }

    single<AuthRepository> { JdbcAuthRepository(get()) }
    single { AuthService(repositoryProvider = { get<AuthRepository>() }, jwtConfig = get()) }

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
