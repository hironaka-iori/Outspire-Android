package dev.outspire.android.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialRecorderTest {
    private lateinit var recorder: AndroidCredentialRecorder

    @Before
    fun setUp() {
        recorder = AndroidCredentialRecorder(ApplicationProvider.getApplicationContext<Context>())
        recorder.clear()
    }

    @After
    fun tearDown() = recorder.clear()

    @Test
    fun encryptedCredentialsCanBeRecordedAndRemoved() {
        val credentials = RecordedCredentials("s20248401", "a secure password")

        recorder.save(credentials)

        assertEquals(credentials, recorder.load())
        recorder.clear()
        assertNull(recorder.load())
    }
}
