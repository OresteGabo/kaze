plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "dev.orestegabo.kaze"
version = "1.0.0"
application {
    mainClass.set("dev.orestegabo.kaze.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.hikari)
    implementation(libs.koin.ktor)
    implementation(libs.koin.loggerSlf4j)
    implementation(libs.java.jwt)
    implementation(libs.jbcrypt)
    implementation(libs.jwks.rsa)
    implementation(libs.logback)
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javaTime)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.serverAutoHeadResponse)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverCachingHeaders)
    implementation(libs.ktor.serverConditionalHeaders)
    implementation(libs.ktor.serverCompression)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverDefaultHeaders)
    implementation(libs.ktor.serverForwardedHeader)
    implementation(libs.ktor.serverRateLimit)
    implementation(libs.ktor.serverRequestValidation)
    implementation(libs.ktor.serverSwagger)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serializationKotlinxJson)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
