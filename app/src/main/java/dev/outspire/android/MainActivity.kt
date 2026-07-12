package dev.outspire.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.outspire.android.data.repository.ServiceLocator
import dev.outspire.android.designsystem.OutspireTheme
import dev.outspire.android.navigation.OutspireRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }
        enableEdgeToEdge()
        setContent {
            OutspireTheme {
                OutspireRoot(repository = ServiceLocator.repository)
            }
        }
    }
}
