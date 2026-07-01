package dev.orestegabo.kaze.presentation.auth

import kotlin.test.Test
import kotlin.test.assertTrue

class AuthGatewayMessageTest {

    @Test
    fun social_only_email_signin_explains_how_to_add_password_login() {
        val problem = AuthGatewayProblemException(
            statusCode = 409,
            problemCode = "password_login_not_configured",
            message = "Password login is not configured.",
        )

        assertTrue(problem.toAuthMessage().contains("social-sign-in account"))
        assertTrue(problem.toSignupMessage().contains("Sign in with Google"))
        assertTrue(problem.toSignupMessage().contains("Settings > Account"))
    }
}
