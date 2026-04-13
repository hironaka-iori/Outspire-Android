package com.computerization.outspire.data.remote

import com.computerization.outspire.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom

private const val IOS_UA_STRING =
    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
        "(KHTML, like Gecko) Mobile/15E148 Safari/604.1"

object TsimsClient {

    fun build(
        engine: HttpClientEngine,
        cookieStorage: PersistentCookieJar,
    ): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
            })
        }
        install(HttpCookies) { storage = cookieStorage }
        install(Logging) {
            level = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
        }
        defaultRequest {
            val base = URLBuilder().takeFrom(BuildConfig.TSIMS_BASE_URL)
            url.protocol = base.protocol
            url.host = base.host
            url.port = base.port
            header("X-Requested-With", "XMLHttpRequest")
            header(HttpHeaders.Accept, "application/json, text/javascript, */*; q=0.01")
            header(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7")
            header(HttpHeaders.UserAgent, IOS_UA_STRING)
            header(HttpHeaders.Referrer, "${BuildConfig.TSIMS_BASE_URL}/")
            header(HttpHeaders.Origin, BuildConfig.TSIMS_BASE_URL)
        }
        expectSuccess = false
        followRedirects = false
        HttpResponseValidator {
            validateResponse { resp: HttpResponse ->
                val status = resp.status
                if (status == HttpStatusCode.Found || status == HttpStatusCode.Unauthorized) {
                    throw UnauthorizedException(status.value)
                }
            }
        }
    }
}
