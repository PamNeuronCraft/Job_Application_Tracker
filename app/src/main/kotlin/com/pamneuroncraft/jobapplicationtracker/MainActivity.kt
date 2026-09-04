package com.pamneuroncraft.jobapplicationtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val sharedText = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        val sharedUrl = sharedText?.let { text ->
            val urlRegex = """https?://\S+""".toRegex()
            urlRegex.find(text)?.value ?: text
        }

        val shortcut = intent.getStringExtra("shortcut")

        enableEdgeToEdge()
        setContent {
            App(
                initialUrl = sharedUrl,
                initialShortcut = shortcut
            )
        }
    }
}
