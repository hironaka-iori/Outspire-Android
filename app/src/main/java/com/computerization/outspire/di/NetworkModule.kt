package com.computerization.outspire.di

import android.content.Context
import com.computerization.outspire.data.local.SecureCredentialStore
import com.computerization.outspire.data.remote.PersistentCookieJar
import com.computerization.outspire.data.remote.TsimsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSecureCredentialStore(@ApplicationContext context: Context): SecureCredentialStore =
        SecureCredentialStore(context)

    @Provides
    @Singleton
    fun provideCookieJar(store: SecureCredentialStore): PersistentCookieJar =
        PersistentCookieJar(store)

    @Provides
    @Singleton
    fun provideHttpClient(cookieJar: PersistentCookieJar): HttpClient =
        TsimsClient.build(OkHttp.create(), cookieJar)
}
