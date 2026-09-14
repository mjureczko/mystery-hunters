package pl.marianjureczko.mysteryhunters

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import pl.marianjureczko.mysteryhunters.ui.ComposeRoot
import pl.marianjureczko.mysteryhunters.ui.Screen
import pl.marianjureczko.mysteryhunters.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Screen.init(this)
        setContent {
            AppTheme {
                ComposeRoot()
            }
        }
    }
}
