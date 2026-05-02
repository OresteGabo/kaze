package dev.orestegabo.kaze.presentation.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.platform.PlatformServices
import dev.orestegabo.kaze.platform.PlatformServicesProvider
import dev.orestegabo.kaze.presentation.api.createKazeApiRepositories
import dev.orestegabo.kaze.presentation.auth.AuthGateway
import dev.orestegabo.kaze.presentation.auth.ExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.KazeAuthGateway
import dev.orestegabo.kaze.presentation.auth.NativeSocialAuthLauncher
import dev.orestegabo.kaze.presentation.auth.createAuthHttpClient
import dev.orestegabo.kaze.presentation.auth.createExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.createNativeSocialAuthLauncher
import dev.orestegabo.kaze.presentation.auth.defaultAuthApiBaseUrl
import dev.orestegabo.kaze.presentation.auth.defaultDeviceLabel
import dev.orestegabo.kaze.presentation.demo.repository.DemoExperienceRepository
import dev.orestegabo.kaze.presentation.demo.repository.DemoHotelRepository
import dev.orestegabo.kaze.presentation.demo.repository.DemoMapRepository
import dev.orestegabo.kaze.presentation.demo.repository.DemoStayRepository
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import dev.orestegabo.kaze.usecase.ObserveHotelContextUseCase
import dev.orestegabo.kaze.usecase.SubmitLateCheckoutUseCase

internal data class KazeDependencies(
    val hotelId: String,
    val mapId: String,
    val hotelRepository: HotelRepository,
    val stayRepository: StayRepository,
    val experienceRepository: ExperienceRepository,
    val mapRepository: MapRepository,
    val observeHotelContext: ObserveHotelContextUseCase,
    val submitLateCheckout: SubmitLateCheckoutUseCase,
    val platformServices: PlatformServices,
    val authGateway: AuthGateway,
    val externalUrlLauncher: ExternalUrlLauncher,
    val nativeSocialAuthLauncher: NativeSocialAuthLauncher,
) {
    companion object {
        private const val DEFAULT_LAUNCH_HOTEL_ID = "rw-kgl-marriott"
        private const val DEFAULT_LAUNCH_MAP_ID = "map_marriott_main"

        fun production(
            apiBaseUrl: String = defaultAuthApiBaseUrl(),
        ): KazeDependencies {
            val repositories = createKazeApiRepositories(apiBaseUrl)
            val platformServices = PlatformServicesProvider.create()
            return KazeDependencies(
                hotelId = DEFAULT_LAUNCH_HOTEL_ID,
                mapId = DEFAULT_LAUNCH_MAP_ID,
                hotelRepository = repositories.hotelRepository,
                stayRepository = repositories.stayRepository,
                experienceRepository = repositories.experienceRepository,
                mapRepository = repositories.mapRepository,
                observeHotelContext = ObserveHotelContextUseCase(repositories.hotelRepository),
                submitLateCheckout = SubmitLateCheckoutUseCase(repositories.stayRepository),
                platformServices = platformServices,
                authGateway = KazeAuthGateway(
                    client = createAuthHttpClient(),
                    baseUrl = apiBaseUrl,
                    deviceId = "kaze-device",
                    deviceLabel = defaultDeviceLabel(),
                ),
                externalUrlLauncher = createExternalUrlLauncher(),
                nativeSocialAuthLauncher = createNativeSocialAuthLauncher(),
            )
        }

        fun demo(): KazeDependencies {
            val hotelRepository = DemoHotelRepository()
            val stayRepository = DemoStayRepository()
            val experienceRepository = DemoExperienceRepository()
            val mapRepository = DemoMapRepository()
            val platformServices = PlatformServicesProvider.create()
            return KazeDependencies(
                hotelId = sampleHotel.id,
                mapId = "temporary-svg-venue",
                hotelRepository = hotelRepository,
                stayRepository = stayRepository,
                experienceRepository = experienceRepository,
                mapRepository = mapRepository,
                observeHotelContext = ObserveHotelContextUseCase(hotelRepository),
                submitLateCheckout = SubmitLateCheckoutUseCase(stayRepository),
                platformServices = platformServices,
                authGateway = KazeAuthGateway(
                    client = createAuthHttpClient(),
                    baseUrl = defaultAuthApiBaseUrl(),
                    deviceId = "kaze-device",
                    deviceLabel = defaultDeviceLabel(),
                ),
                externalUrlLauncher = createExternalUrlLauncher(),
                nativeSocialAuthLauncher = createNativeSocialAuthLauncher(),
            )
        }
    }
}

@Composable
internal fun rememberKazeDependencies(): KazeDependencies =
    remember { KazeDependencies.production() }
