package dev.orestegabo.kaze.presentation.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.platform.PlatformServices
import dev.orestegabo.kaze.platform.PlatformServicesProvider
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
    remember { KazeDependencies.demo() }
