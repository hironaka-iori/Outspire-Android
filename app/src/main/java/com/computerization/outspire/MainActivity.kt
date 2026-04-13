package com.computerization.outspire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.computerization.outspire.designsystem.OutspireTheme
import com.computerization.outspire.navigation.OutspireRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OutspireTheme {
                OutspireRoot()
            }
        }
    }
}
