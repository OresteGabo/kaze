package dev.orestegabo.kaze.services.email

class EmailDispatcher(private val providers: List<EmailProvider>) {

    suspend fun sendWithFailover(to: String, subject: String, body: String): Boolean {
        for (provider in providers) {
            println("Attempting ${provider.name}...")

            when (val result = provider.send(to, subject, body)) {
                is EmailResult.Success -> return true
                is EmailResult.LimitReached -> {
                    println("${provider.name} limit hit. Falling back...")
                    continue
                }
                is EmailResult.Failure -> {
                    println("${provider.name} failed: ${result.error}")
                    continue
                }
            }
        }
        return false // All failed
    }
}