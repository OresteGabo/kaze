package dev.orestegabo.kaze.auth

import java.lang.reflect.Proxy
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AuthServiceAccountLinkingTest {

    @Test
    fun social_only_user_can_add_password_and_sign_in_as_the_same_user() {
        var storedUser = googleOnlyUser()
        var passwordLoginWasAdded = false
        val repository = authRepositoryProxy { methodName, args ->
            when (methodName) {
                "findById" -> storedUser.takeIf { it.user.id == args?.get(0) }
                "findByEmail" -> storedUser.takeIf { it.user.email == args?.get(0) }
                "setPasswordLogin" -> {
                    passwordLoginWasAdded = true
                    storedUser = storedUser.copy(passwordHash = args?.get(1) as String)
                    storedUser
                }
                "createRefreshToken" -> StoredRefreshToken(
                    id = "refresh-1",
                    userId = storedUser.user.id,
                    tokenHash = args?.get(1) as String,
                    familyId = args[2] as String,
                    expiresAt = args[5] as Instant,
                )
                else -> unsupported(methodName)
            }
        }
        val service = AuthService(repositoryProvider = { repository }, jwtConfig = testJwtConfig())

        val updatedUser = service.setPassword(
            userId = storedUser.user.id,
            request = AuthSetPasswordRequest(newPassword = "new-password"),
        )
        val signedIn = service.signin(
            AuthSigninRequest(email = storedUser.user.email, password = "new-password"),
        )

        assertEquals("google-user-1", updatedUser.id)
        assertEquals("google-user-1", signedIn.user.id)
        assertEquals("person@example.com", signedIn.user.email)
        assertEquals(true, passwordLoginWasAdded)
        assertNotNull(storedUser.passwordHash)
    }

    @Test
    fun creating_a_second_account_for_a_social_only_email_is_rejected_with_guidance() {
        val storedUser = googleOnlyUser()
        val repository = authRepositoryProxy { methodName, _ ->
            when (methodName) {
                "findSignupConflicts" -> SignupConflicts(
                    emailExists = true,
                    usernameExists = false,
                    phoneNumberExists = false,
                )
                "findByEmail" -> storedUser
                else -> unsupported(methodName)
            }
        }
        val service = AuthService(repositoryProvider = { repository }, jwtConfig = testJwtConfig())

        val problem = assertFailsWith<AuthProblemException> {
            service.signup(AuthSignupRequest(email = storedUser.user.email, password = "new-password"))
        }

        assertEquals("password_login_not_configured", problem.code)
    }
}

private fun googleOnlyUser(): StoredAuthUser =
    StoredAuthUser(
        user = AuthUser(
            id = "google-user-1",
            email = "person@example.com",
            displayName = "Example Person",
            roles = setOf(AuthRole.CUSTOMER),
        ),
        passwordHash = null,
    )

private fun authRepositoryProxy(
    handler: (methodName: String, args: Array<out Any?>?) -> Any?,
): AuthRepository =
    Proxy.newProxyInstance(
        AuthRepository::class.java.classLoader,
        arrayOf(AuthRepository::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "toString" -> "AuthRepositoryProxy"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            else -> handler(method.name, args)
        }
    } as AuthRepository

private fun unsupported(methodName: String): Nothing =
    error("Unexpected AuthRepository call: $methodName")

private fun testJwtConfig(): JwtConfig =
    JwtConfig(
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm",
        secret = "test-secret-that-is-at-least-32-characters-long",
        accessTokenTtlSeconds = 600,
        refreshTokenTtlSeconds = 3_600,
        oneTimeLoginTokenTtlSeconds = 60,
        requireJwtForApi = true,
        googleClientIds = emptySet(),
        appleClientIds = emptySet(),
        socialAuth = SocialAuthConfig(
            appDeepLinkRedirect = "kaze://auth/callback",
            google = OAuthProviderConfig("", "", "", "", "", emptyList()),
            apple = AppleOAuthProviderConfig("", "", "", "", "", "", "", emptyList()),
            facebook = OAuthProviderConfig("", "", "", "", "", emptyList()),
        ),
    )
